package com.example.playback

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.util.Log
import com.example.data.MusicDao
import com.example.data.SongEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import java.net.URL

enum class RepeatMode { NONE, ONE, ALL }

class PlaybackManager(
    private val context: Context,
    private val musicDao: MusicDao
) {
    private val tag = "PlaybackManager"
    private var mediaPlayer: MediaPlayer? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var tickerJob: Job? = null
    private var sleepTimerJob: Job? = null

    // --- State Flows ---
    private val _currentSong = MutableStateFlow<SongEntity?>(null)
    val currentSong = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs = _durationMs.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed = _playbackSpeed.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.NONE)
    val repeatMode = _repeatMode.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled = _isShuffleEnabled.asStateFlow()

    private val _volume = MutableStateFlow(1.0f)
    val volume = _volume.asStateFlow()

    private val _queue = MutableStateFlow<List<SongEntity>>(emptyList())
    val queue = _queue.asStateFlow()

    private val _sleepTimerRemainingSeconds = MutableStateFlow<Int?>(null)
    val sleepTimerRemainingSeconds = _sleepTimerRemainingSeconds.asStateFlow()

    // Simulated EQ States (in percentages 0-100)
    val bassState = MutableStateFlow(50)
    val midState = MutableStateFlow(50)
    val trebleState = MutableStateFlow(50)
    val vocalBooster = MutableStateFlow(false)

    private var currentIndex = -1

    init {
        initializeMediaPlayer()
    }

    private fun initializeMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setOnPreparedListener { mp ->
                _durationMs.value = mp.duration.toLong()
                mp.start()
                _isPlaying.value = true
                applyPlaybackParams()
                startTicker()
            }
            setOnCompletionListener {
                handleSongCompletion()
            }
            setOnErrorListener { _, what, extra ->
                Log.e(tag, "MediaPlayer Error: what=$what, extra=$extra")
                _isPlaying.value = false
                false
            }
        }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = coroutineScope.launch {
            while (isActive) {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        _currentPositionMs.value = mp.currentPosition.toLong()
                    }
                }
                delay(500)
            }
        }
    }

    private fun handleSongCompletion() {
        coroutineScope.launch {
            when (_repeatMode.value) {
                RepeatMode.ONE -> {
                    seekTo(0)
                    play()
                }
                RepeatMode.ALL -> {
                    next()
                }
                RepeatMode.NONE -> {
                    if (currentIndex < _queue.value.size - 1) {
                        next()
                    } else {
                        _isPlaying.value = false
                        _currentPositionMs.value = 0
                        tickerJob?.cancel()
                    }
                }
            }
        }
    }

    fun playSong(song: SongEntity, newQueue: List<SongEntity> = emptyList()) {
        if (newQueue.isNotEmpty()) {
            _queue.value = newQueue
            currentIndex = newQueue.indexOfFirst { it.id == song.id }
        } else if (!_queue.value.contains(song)) {
            _queue.value = _queue.value + song
            currentIndex = _queue.value.indexOfFirst { it.id == song.id }
        } else {
            currentIndex = _queue.value.indexOfFirst { it.id == song.id }
        }

        _currentSong.value = song
        _currentPositionMs.value = 0L
        _durationMs.value = song.durationMs

        // Update Listening History
        coroutineScope.launch {
            musicDao.insertHistory(com.example.data.HistoryEntity(songId = song.id))
            // Increment local play count
            musicDao.updateSong(song.copy(playCount = song.playCount + 1))
        }

        try {
            ensureMediaPlayerExists()
            mediaPlayer?.run {
                reset()
                // If song was downloaded, play from the local path. Otherwise, stream it.
                val dataSource = if (song.isDownloaded && !song.localPath.isNullOrEmpty()) {
                    val localFile = File(song.localPath)
                    if (localFile.exists()) {
                        Log.i(tag, "Playing offline downloaded track from: ${song.localPath}")
                        Uri.fromFile(localFile).toString()
                    } else {
                        Log.w(tag, "Local path not found, streaming instead: ${song.streamUrl}")
                        song.streamUrl
                    }
                } else {
                    Log.i(tag, "Streaming from online source: ${song.streamUrl}")
                    song.streamUrl
                }
                
                setDataSource(context, Uri.parse(dataSource))
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to start playback: ${e.message}", e)
        }
    }

    private fun ensureMediaPlayerExists() {
        if (mediaPlayer == null) {
            initializeMediaPlayer()
        }
    }

    fun togglePlayPause() {
        val mp = mediaPlayer ?: return
        if (mp.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun play() {
        ensureMediaPlayerExists()
        mediaPlayer?.let { mp ->
            try {
                mp.start()
                _isPlaying.value = true
                startTicker()
            } catch (e: Exception) {
                Log.e(tag, "Failed to resume: ${e.message}")
            }
        }
    }

    fun pause() {
        mediaPlayer?.let { mp ->
            try {
                if (mp.isPlaying) {
                    mp.pause()
                }
                _isPlaying.value = false
                tickerJob?.cancel()
            } catch (e: Exception) {
                Log.e(tag, "Failed to pause: ${e.message}")
            }
        }
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.let { mp ->
            try {
                mp.seekTo(positionMs.toInt())
                _currentPositionMs.value = positionMs
            } catch (e: Exception) {
                Log.e(tag, "Failed to seek: ${e.message}")
            }
        }
    }

    fun next() {
        val q = _queue.value
        if (q.isEmpty()) return

        if (_isShuffleEnabled.value) {
            currentIndex = (0 until q.size).random()
        } else {
            currentIndex = (currentIndex + 1) % q.size
        }

        playSong(q[currentIndex])
    }

    fun previous() {
        val q = _queue.value
        if (q.isEmpty()) return

        currentIndex = if (currentIndex - 1 < 0) {
            q.size - 1
        } else {
            currentIndex - 1
        }

        playSong(q[currentIndex])
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        applyPlaybackParams()
    }

    private fun applyPlaybackParams() {
        val mp = mediaPlayer ?: return
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && mp.isPlaying) {
            try {
                val params = mp.playbackParams ?: PlaybackParams()
                params.speed = _playbackSpeed.value
                mp.playbackParams = params
            } catch (e: Exception) {
                Log.e(tag, "Error setting playback params: ${e.message}")
            }
        }
    }

    fun toggleShuffle() {
        _isShuffleEnabled.value = !_isShuffleEnabled.value
    }

    fun setRepeatMode(mode: RepeatMode) {
        _repeatMode.value = mode
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0.0f, 1.0f)
        _volume.value = clamped
        mediaPlayer?.setVolume(clamped, clamped)
    }

    // --- Dynamic Crossfade simulation ---
    fun executeCrossfade(targetSong: SongEntity, playlist: List<SongEntity>) {
        coroutineScope.launch {
            val originalVol = _volume.value
            // Fade-down
            for (i in 10 downTo 0) {
                setVolume(originalVol * (i / 10f))
                delay(150)
            }
            playSong(targetSong, playlist)
            // Fade-up
            for (i in 0..10) {
                setVolume(originalVol * (i / 10f))
                delay(150)
            }
            setVolume(originalVol)
        }
    }

    // --- Sleep Timer ---
    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        _sleepTimerRemainingSeconds.value = minutes * 60
        sleepTimerJob = coroutineScope.launch {
            while (isActive) {
                val rem = _sleepTimerRemainingSeconds.value
                if (rem == null || rem <= 0) {
                    pause()
                    _sleepTimerRemainingSeconds.value = null
                    break
                }
                delay(1000)
                _sleepTimerRemainingSeconds.value = rem - 1
            }
        }
        Log.i(tag, "Sleep timer set for $minutes minutes.")
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        _sleepTimerRemainingSeconds.value = null
    }

    // --- Interactive Song Download Simulation & Real Multi-threaded save ---
    fun downloadSong(
        song: SongEntity,
        onProgress: (Float) -> Unit,
        onComplete: (Boolean) -> Unit
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                onProgress(0.1f)
                val destFile = File(context.filesDir, "spark_offline_${song.id}.mp3")
                
                // Simulating download stream loading bar
                delay(400)
                onProgress(0.3f)
                
                val url = URL(song.streamUrl)
                val connection = url.openConnection()
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.connect()
                
                val inputStream = connection.getInputStream()
                val fileOutput = FileOutputStream(destFile)
                
                val buffer = ByteArray(4096)
                var bytesRead: Int
                var totalBytesRead = 0
                val fileLength = connection.contentLength
                
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    fileOutput.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    if (fileLength > 0) {
                        onProgress(0.3f + 0.6f * (totalBytesRead.toFloat() / fileLength.toFloat()))
                    }
                }
                
                fileOutput.close()
                inputStream.close()
                
                onProgress(1.0f)
                
                // Save locally
                val updatedSong = song.copy(
                    isDownloaded = true,
                    localPath = destFile.absolutePath
                )
                musicDao.insertSong(updatedSong)
                
                // If it's currently active, update the active song state of playback manager so details match
                if (_currentSong.value?.id == song.id) {
                    _currentSong.value = updatedSong
                }
                
                withContext(Dispatchers.Main) {
                    onComplete(true)
                }
            } catch (e: Exception) {
                Log.e(tag, "Offline track download error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onComplete(false)
                }
            }
        }
    }

    fun removeDownload(song: SongEntity) {
        coroutineScope.launch(Dispatchers.IO) {
            val path = song.localPath
            if (!path.isNullOrEmpty()) {
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                }
            }
            val updatedSong = song.copy(
                isDownloaded = false,
                localPath = null
            )
            musicDao.insertSong(updatedSong)
            if (_currentSong.value?.id == song.id) {
                _currentSong.value = updatedSong
            }
        }
    }

    fun release() {
        tickerJob?.cancel()
        sleepTimerJob?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
        coroutineScope.cancel()
    }
}
