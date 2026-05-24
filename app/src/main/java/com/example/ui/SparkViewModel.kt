package com.example.ui

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.playback.PlaybackManager
import com.example.playback.RepeatMode
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SparkViewModel(application: Application) : AndroidViewModel(application) {
    private val tag = "SparkViewModel"
    private val database = AppDatabase.getDatabase(application)
    private val dao = database.musicDao()
    val playbackManager = PlaybackManager(application, dao)
    private val geminiManager = GeminiManager()

    // --- Authentication State (Persisted in SharedPreferences inside ViewModel) ---
    private val sharedPrefs = application.getSharedPreferences("spark_auth_prefs", Context.MODE_PRIVATE)
    
    private val _currentUserEmail = MutableStateFlow<String?>(sharedPrefs.getString("user_email", null))
    val currentUserEmail = _currentUserEmail.asStateFlow()

    private val _currentUserName = MutableStateFlow<String?>(sharedPrefs.getString("user_name", null))
    val currentUserName = _currentUserName.asStateFlow()

    private val _isUserLoggedIn = MutableStateFlow(sharedPrefs.getBoolean("logged_in", false))
    val isUserLoggedIn = _isUserLoggedIn.asStateFlow()

    // --- Database Flows ---
    val allSongs: StateFlow<List<SongEntity>> = dao.getAllSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPlaylists: StateFlow<List<PlaylistEntity>> = dao.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val listeningHistory: StateFlow<List<HistoryEntity>> = dao.getListeningHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Download Tracking ---
    private val _downloadProgresses = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloadProgresses = _downloadProgresses.asStateFlow()

    // --- AI States ---
    private val _aiPlaylistsGenerating = MutableStateFlow(false)
    val aiPlaylistsGenerating = _aiPlaylistsGenerating.asStateFlow()

    private val _aiLyricsGenerating = MutableStateFlow(false)
    val aiLyricsGenerating = _aiLyricsGenerating.asStateFlow()

    private val _generatedLyrics = MutableStateFlow<String?>(null)
    val generatedLyrics = _generatedLyrics.asStateFlow()

    init {
        // Prepopulate database with default songs if library is empty
        viewModelScope.launch {
            allSongs.first { true } // Wait for flow initial load
            val currentList = dao.getAllSongs().first()
            if (currentList.isEmpty()) {
                Log.i(tag, "Library dry. Pre-populating Spark with standard tracks.")
                populateInitialTracks()
            }
        }
    }

    private suspend fun populateInitialTracks() {
        val defaultSongs = listOf(
            SongEntity(
                id = "1",
                title = "Neon Drive",
                artist = "Sunset Retro",
                album = "Vapor Outrun",
                durationMs = 372000,
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                imageUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500&q=80",
                genre = "Synthwave",
                lyrics = "[00:00] (Neon instrumental intro)\n[00:10] Midnight highway is calling me\n[00:20] The neon streetlights is all I see\n[00:30] Revving the engine, feeling so free\n[00:40] Outrun the grid, come along with me!"
            ),
            SongEntity(
                id = "2",
                title = "Midnight Cruise",
                artist = "Cosmic Dust",
                album = "Star Trails",
                durationMs = 423000,
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                imageUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=500&q=80",
                genre = "Vaporwave",
                lyrics = "[00:00] (Cosmic ambiance introduction)\n[00:15] Lost in the static of a retro dream\n[00:30] Things are never quite what they seem\n[00:45] Drifting down lanes of digital light\n[01:00] We'll cruise through the midnight starry night"
            ),
            SongEntity(
                id = "3",
                title = "Deep Space",
                artist = "Nebula Zone",
                album = "Vacuum of Space",
                durationMs = 302000,
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                imageUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=500&q=80",
                genre = "Ambient",
                lyrics = "[00:00] (Slow evolving synth pads)\n[00:30] No sound in the black void\n[01:00] Floating past an old asteroid\n[01:30] Cold lights twinkling far away\n[02:00] Lost in the beauty of endless delay"
            ),
            SongEntity(
                id = "4",
                title = "Cyber Club Pulse",
                artist = "Techno Nova",
                album = "Neon Grid 2099",
                durationMs = 318000,
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
                imageUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&q=80",
                genre = "Cyberpunk",
                lyrics = "[00:00] (Heavy synthesizer drums)\n[00:10] Plug in my cortex, feed me the sound\n[00:20] Grid-breaking rhythms shaking the ground\n[00:30] Augmented systems running in high\n[00:40] Under the mega-structure neon sky"
            ),
            SongEntity(
                id = "5",
                title = "Acoustic Whispers",
                artist = "Luna Breeze",
                album = "Summer Solitude",
                durationMs = 405000,
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3",
                imageUrl = "https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?w=500&q=80",
                genre = "Acoustic",
                lyrics = "[00:00] (Aesthetic acoustic fingerpicking)\n[00:12] Whispers under the cozy shade\n[00:24] Thinking 'bout all the plans we made\n[00:36] Fireflies dance in the summer air\n[00:48] Finding the joy without any care"
            ),
            SongEntity(
                id = "6",
                title = "Electric Sky",
                artist = "Orbit Rays",
                album = "Atmospheric Fluctuations",
                durationMs = 345000,
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-12.mp3",
                imageUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&q=80",
                genre = "Electronica",
                lyrics = "[00:00] (Sparkling glitch introductions)\n[00:15] Electric currents charge up the air\n[00:30] Static and lightning starting to flare\n[00:45] Ride on the storm-cloud, touch the machine\n[01:00] Purest energy you've ever seen"
            )
        )
        dao.insertSongs(defaultSongs)

        // Prepopulate a couple playlists
        val defaultPlaylists = listOf(
            PlaylistEntity(
                id = "p1",
                name = "Outrun Sunset",
                description = "Premium synth beats for midnight drives.",
                imageUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500&q=80",
                songIdsJson = PlaylistEntity.createSongIdsJson(listOf("1", "2", "4"))
            ),
            PlaylistEntity(
                id = "p2",
                name = "Deep Relaxation",
                description = "Ambient and soft acoustic flows to calm your state.",
                imageUrl = "https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?w=500&q=80",
                songIdsJson = PlaylistEntity.createSongIdsJson(listOf("3", "5"))
            )
        )
        defaultPlaylists.forEach { dao.insertPlaylist(it) }
    }

    // --- Authentication Business Actions ---
    fun registerOrLogin(email: String, name: String) {
        sharedPrefs.edit()
            .putString("user_email", email)
            .putString("user_name", name)
            .putBoolean("logged_in", true)
            .apply()

        _currentUserEmail.value = email
        _currentUserName.value = name
        _isUserLoggedIn.value = true
    }

    fun logout() {
        sharedPrefs.edit().clear().apply()
        _currentUserEmail.value = null
        _currentUserName.value = null
        _isUserLoggedIn.value = false
    }

    // --- Like / Favorite Song ---
    fun toggleLikeSong(song: SongEntity) {
        viewModelScope.launch {
            val updated = song.copy(isLiked = !song.isLiked)
            dao.updateSong(updated)
            if (playbackManager.currentSong.value?.id == song.id) {
                // Keep playback state synced
                playbackManager.playSong(updated, playbackManager.queue.value)
                playbackManager.pause() // Keep state matching actual pause state without auto-starting
            }
        }
    }

    // --- Offline Downloads Manager Actions ---
    fun downloadSong(song: SongEntity) {
        _downloadProgresses.value = _downloadProgresses.value + (song.id to 0.01f)
        playbackManager.downloadSong(
            song = song,
            onProgress = { progress ->
                _downloadProgresses.value = _downloadProgresses.value + (song.id to progress)
            },
            onComplete = { success ->
                val nextProgresses = _downloadProgresses.value.toMutableMap()
                nextProgresses.remove(song.id)
                _downloadProgresses.value = nextProgresses
            }
        )
    }

    fun deleteDownloadedSong(song: SongEntity) {
        playbackManager.removeDownload(song)
    }

    // --- Playlists Management Actions ---
    fun createPlaylist(name: String, description: String) {
        viewModelScope.launch {
            val id = "pl_${System.currentTimeMillis()}"
            val newPl = PlaylistEntity(
                id = id,
                name = name,
                description = description,
                imageUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&q=80"
            )
            dao.insertPlaylist(newPl)
        }
    }

    fun addSongToPlaylist(playlistId: String, songId: String) {
        viewModelScope.launch {
            val currentPl = dao.getPlaylistById(playlistId) ?: return@launch
            val ids = currentPl.getSongIds().toMutableList()
            if (!ids.contains(songId)) {
                ids.add(songId)
                val updatedPl = currentPl.copy(
                    songIdsJson = PlaylistEntity.createSongIdsJson(ids)
                )
                dao.insertPlaylist(updatedPl)
            }
        }
    }

    fun removeSongFromPlaylist(playlistId: String, songId: String) {
        viewModelScope.launch {
            val currentPl = dao.getPlaylistById(playlistId) ?: return@launch
            val ids = currentPl.getSongIds().toMutableList()
            if (ids.remove(songId)) {
                val updatedPl = currentPl.copy(
                    songIdsJson = PlaylistEntity.createSongIdsJson(ids)
                )
                dao.insertPlaylist(updatedPl)
            }
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            dao.deletePlaylistById(playlistId)
        }
    }

    // --- AI Smart Features Actions ---
    fun generateAiMoodPlaylist(mood: String, onComplete: (PlaylistEntity) -> Unit) {
        viewModelScope.launch {
            _aiPlaylistsGenerating.value = true
            try {
                val songsList = allSongs.value
                val matchedSongIds = geminiManager.generateMoodPlaylist(mood, songsList)
                
                val currentCount = allPlaylists.value.size
                val id = "ai_${System.currentTimeMillis()}"
                val aiPlaylist = PlaylistEntity(
                    id = id,
                    name = "AI Mood: ${mood.lowercase().replaceFirstChar { it.uppercase() }}",
                    description = "Personalized dynamic playlist curated by Spark AI for your mood.",
                    imageUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=500&q=80",
                    songIdsJson = PlaylistEntity.createSongIdsJson(matchedSongIds)
                )
                dao.insertPlaylist(aiPlaylist)
                onComplete(aiPlaylist)
            } catch (e: Exception) {
                Log.e(tag, "AI Mood generation failed", e)
            } finally {
                _aiPlaylistsGenerating.value = false
            }
        }
    }

    fun generateAiLyricsForActiveSong() {
        val song = playbackManager.currentSong.value ?: return
        viewModelScope.launch {
            _aiLyricsGenerating.value = true
            _generatedLyrics.value = null
            try {
                val newlyGenerated = geminiManager.generateLyrics(song.title, song.artist)
                _generatedLyrics.value = newlyGenerated
                
                // Update in database to save forever!
                val updatedSong = song.copy(lyrics = newlyGenerated)
                dao.updateSong(updatedSong)
                
                if (playbackManager.currentSong.value?.id == song.id) {
                    // Update active player's metadata
                    playbackManager.playSong(updatedSong, playbackManager.queue.value)
                    playbackManager.pause()
                }
            } catch (e: Exception) {
                Log.e(tag, "AI Lyrics generation failed", e)
            } finally {
                _aiLyricsGenerating.value = false
            }
        }
    }

    // --- Admin Dashboard Actions ---
    fun adminUploadSong(title: String, artist: String, album: String, genre: String, url: String, coverUrl: String) {
        viewModelScope.launch {
            val songId = "adm_${System.currentTimeMillis()}"
            val newSong = SongEntity(
                id = songId,
                title = title,
                artist = artist,
                album = album,
                durationMs = 285000,
                streamUrl = url.ifBlank { "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3" },
                imageUrl = coverUrl.ifBlank { "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&q=80" },
                genre = genre.ifBlank { "Unknown" },
                lyrics = "[00:00] (Instrumental intro)\n[00:15] Custom dynamic admin added track!",
                isCustomUploaded = true
            )
            dao.insertSong(newSong)
            Log.i(tag, "Admin successfully uploaded: $title by $artist.")
        }
    }

    fun adminDeleteSong(songId: String) {
        viewModelScope.launch {
            dao.deleteSongById(songId)
        }
    }

    override fun onCleared() {
        playbackManager.release()
        super.onCleared()
    }
}
