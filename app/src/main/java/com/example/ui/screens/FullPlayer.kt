package com.example.ui.screens

import android.widget.Space
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.SongEntity
import com.example.playback.PlaybackManager
import com.example.playback.RepeatMode
import com.example.ui.SparkViewModel
import com.example.ui.components.AnimatedWaveformVisualizer
import com.example.ui.components.GlassCard
import com.example.ui.components.NeonText
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.floor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullMusicPlayerScreen(
    viewModel: SparkViewModel,
    onMinimize: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pm = viewModel.playbackManager
    val song by pm.currentSong.collectAsState()
    val isPlaying by pm.isPlaying.collectAsState()
    val rawPosition by pm.currentPositionMs.collectAsState()
    val duration by pm.durationMs.collectAsState()
    val speed by pm.playbackSpeed.collectAsState()
    val repeatMode by pm.repeatMode.collectAsState()
    val isShuffle by pm.isShuffleEnabled.collectAsState()
    val volume by pm.volume.collectAsState()
    val sleepTimer by pm.sleepTimerRemainingSeconds.collectAsState()

    val lyricsGenerating by viewModel.aiLyricsGenerating.collectAsState()
    val currentLang by viewModel.appLanguage.collectAsState()
    val isTamil = currentLang == "ta"

    var showEqSheet by remember { mutableStateOf(false) }
    var showSleepTimerSheet by remember { mutableStateOf(false) }
    var showSpeedSheet by remember { mutableStateOf(false) }

    // Sliding drag position helper
    var sliderPositionOverride by remember { mutableStateOf<Float?>(null) }
    val displayedPosition = sliderPositionOverride?.toLong() ?: rawPosition

    // Rotation angle for disk
    val infiniteTransition = rememberInfiniteTransition("spinning_album")
    val rotationAngle by if (isPlaying) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(12000, easing = LinearEasing),
                repeatMode = androidx.compose.animation.core.RepeatMode.Restart
            ),
            label = "spinning_angle"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    if (song == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepDarkBackground),
            contentAlignment = Alignment.Center
        ) {
            Text("No song active in queue", color = TextSecondary)
        }
        return
    }

    val activeSong = song!!

    // Dynamic background brush matching active song's genre / mood
    val topGradientColor = when (activeSong.genre.lowercase()) {
        "synthwave" -> CyberCyan
        "vaporwave" -> CyberMagenta
        "ambient" -> NeonPurple
        "cyberpunk" -> CyberMagenta
        else -> CyberCyan
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            topGradientColor.copy(alpha = 0.25f),
                            DeepDarkBackground,
                            DeepDarkBackground
                        )
                    )
                )
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onMinimize) {
                        Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Minimize", tint = TextPrimary, modifier = Modifier.size(28.dp))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "NOW STREAMING",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = activeSong.album,
                            maxLines = 1,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary
                        )
                    }
                    IconButton(onClick = { viewModel.toggleLikeSong(activeSong) }) {
                        Icon(
                            imageVector = if (activeSong.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like Song",
                            tint = if (activeSong.isLiked) CyberMagenta else TextPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // Album Artwork Spinner
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .clip(CircleShape)
                        .background(Color.Black)
                        .border(4.dp, Brush.linearGradient(listOf(CyberCyan, NeonPurple)), CircleShape)
                        .rotate(rotationAngle)
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = activeSong.imageUrl,
                        contentDescription = "Album Cover Artwork",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize(0.85f)
                            .clip(CircleShape)
                    )
                    // Disk spindle hole block
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(DeepDarkBackground)
                            .border(2.dp, Color.Black, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Song Meta Text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NeonText(
                        text = activeSong.title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        ),
                        glowColor = topGradientColor
                    )
                    Text(
                        text = activeSong.artist,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Waveform Indicator while active
                AnimatedWaveformVisualizer(
                    isPlaying = isPlaying,
                    barCount = 20,
                    color = topGradientColor,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(40.dp)
                )

                // Seek Progress Control Row
                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = displayedPosition.toFloat(),
                        onValueChange = { sliderPositionOverride = it },
                        onValueChangeFinished = {
                            sliderPositionOverride?.let {
                                pm.seekTo(it.toLong())
                                sliderPositionOverride = null
                            }
                        },
                        valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                        colors = SliderDefaults.colors(
                            activeTrackColor = topGradientColor,
                            inactiveTrackColor = Color.White.copy(alpha = 0.15f),
                            thumbColor = topGradientColor
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("music_slider")
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = formatDuration(displayedPosition), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(text = formatDuration(duration), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }

                // Primary Playback Panel Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { pm.toggleShuffle() },
                        modifier = Modifier.testTag("shuffle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (isShuffle) CyberCyan else TextSecondary
                        )
                    }

                    IconButton(
                        onClick = { pm.previous() },
                        modifier = Modifier.testTag("prev_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = TextPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(topGradientColor)
                            .clickable { pm.togglePlayPause() }
                            .testTag("play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = DeepDarkBackground,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    IconButton(
                        onClick = { pm.next() },
                        modifier = Modifier.testTag("next_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = TextPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val nextMode = when (repeatMode) {
                                RepeatMode.NONE -> RepeatMode.ONE
                                RepeatMode.ONE -> RepeatMode.ALL
                                RepeatMode.ALL -> RepeatMode.NONE
                            }
                            pm.setRepeatMode(nextMode)
                        },
                        modifier = Modifier.testTag("repeat_button")
                    ) {
                        val icon = when (repeatMode) {
                            RepeatMode.NONE -> Icons.Default.Repeat
                            RepeatMode.ONE -> Icons.Default.RepeatOne
                            RepeatMode.ALL -> Icons.Default.Repeat
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = "Repeat",
                            tint = if (repeatMode != RepeatMode.NONE) CyberCyan else TextSecondary
                        )
                    }
                }

                // Volume slider
                Row(
                    modifier = Modifier.fillMaxWidth(0.9f).padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.VolumeDown, contentDescription = null, tint = TextSecondary)
                    Slider(
                        value = volume,
                        onValueChange = { pm.setVolume(it) },
                        colors = SliderDefaults.colors(
                            activeTrackColor = textColorForAccent(topGradientColor),
                            thumbColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = TextSecondary)
                }

                // Secondary Settings row icons (EQ, Speed, Timer)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showEqSheet = true }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Equalizer, contentDescription = "EQ", tint = CyberCyan)
                            Text(" EQ", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    IconButton(onClick = { showSleepTimerSheet = true }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (sleepTimer != null) Icons.Default.Timer else Icons.Outlined.Timer,
                                contentDescription = "Sleep Timer",
                                tint = if (sleepTimer != null) CyberMagenta else TextSecondary
                            )
                            if (sleepTimer != null) {
                                Text(
                                    text = " ${sleepTimer!! / 60}m",
                                    color = CyberMagenta,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(" Timer", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }

                    IconButton(onClick = { showSpeedSheet = true }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Speed, contentDescription = "Speed", tint = NeonPurple)
                            Text(" ${speed}x", color = NeonPurple, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Synced Live Lyrics Glass Block
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                                Text(if (isTamil) " ஓடிக்கொண்டிருக்கும் வரிகள்" else " Synced Live Lyrics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            
                            IconButton(
                                onClick = { viewModel.generateAiLyricsForActiveSong() },
                                enabled = !lyricsGenerating,
                                modifier = Modifier.size(24.dp)
                            ) {
                                if (lyricsGenerating) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = CyberCyan, strokeWidth = 2.dp)
                                  } else {
                                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Regenerate AI", tint = CyberCyan, modifier = Modifier.size(20.dp))
                                  }
                            }
                        }

                        val parsedLyrics = parseLyrics(activeSong.lyrics)
                        val activeIndex = parsedLyrics.indexOfLast { displayedPosition >= it.timestampMs }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)
                        ) {
                            if (parsedLyrics.isEmpty()) {
                                Text(if (isTamil) "விவரங்கள் எதுவும் இல்லை. வரிகளை உருவாக்க AI பொத்தானை அழுத்தவும்." else "No lyrics available. Generate lyrics with Spark AI model.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = Modifier.padding(vertical = 12.dp))
                            } else {
                                parsedLyrics.forEachIndexed { idx, line ->
                                    val isActive = idx == activeIndex
                                    val scale by animateFloatAsState(if (isActive) 1.05f else 0.95f)
                                    val color = if (isActive) CyberCyan else TextSecondary.copy(alpha = 0.6f)
                                    val fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Normal

                                    Text(
                                        text = line.text,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = fontWeight,
                                            fontSize = if (isActive) 18.sp else 15.sp,
                                            lineHeight = 22.sp
                                        ),
                                        color = color,
                                        modifier = Modifier
                                            .padding(vertical = 2.dp)
                                            .clickable { pm.seekTo(line.timestampMs) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Equalizer Bottom Dialog ---
    if (showEqSheet) {
        AlertDialog(
            onDismissRequest = { showEqSheet = false },
            confirmButton = {
                TextButton(onClick = { showEqSheet = false }) {
                    Text("DONE", color = CyberCyan)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Equalizer, contentDescription = null, tint = CyberCyan)
                    Text(" HD Audio Equalized", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                val bass by pm.bassState.collectAsState()
                val mid by pm.midState.collectAsState()
                val treble by pm.trebleState.collectAsState()
                val vocal by pm.vocalBooster.collectAsState()

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Adjust sliders to configure simulated Spark fidelity.", fontSize = 12.sp, color = TextSecondary)
                    
                    Column {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("BASS (Glow)", color = TextPrimary)
                            Text("${bass}%", color = CyberCyan)
                        }
                        Slider(
                            value = bass.toFloat(),
                            onValueChange = { pm.bassState.value = it.toInt() },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(activeTrackColor = CyberCyan)
                        )
                    }

                    Column {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("MID (Presence)", color = TextPrimary)
                            Text("${mid}%", color = NeonPurple)
                        }
                        Slider(
                            value = mid.toFloat(),
                            onValueChange = { pm.midState.value = it.toInt() },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(activeTrackColor = NeonPurple)
                        )
                    }

                    Column {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("TREBLE (Clarity)", color = TextPrimary)
                            Text("${treble}%", color = CyberMagenta)
                        }
                        Slider(
                            value = treble.toFloat(),
                            onValueChange = { pm.trebleState.value = it.toInt() },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(activeTrackColor = CyberMagenta)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Vocal Clear Booster", color = TextPrimary)
                        Switch(
                            checked = vocal,
                            onCheckedChange = { pm.vocalBooster.value = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyberCyan)
                        )
                    }
                }
            },
            containerColor = CustomSurface
        )
    }

    // --- Sleep Timer Chooser Sheet ---
    if (showSleepTimerSheet) {
        AlertDialog(
            onDismissRequest = { showSleepTimerSheet = false },
            confirmButton = {
                TextButton(onClick = { showSleepTimerSheet = false }) {
                    Text("Dismiss", color = TextSecondary)
                }
            },
            title = { Text("Sleep Timer Countdown", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select when playback automatically pauses:", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (sleepTimer != null) {
                        Button(
                            onClick = {
                                pm.cancelSleepTimer()
                                showSleepTimerSheet = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberMagenta),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancel Active Timer (${sleepTimer!! / 60}m Left)", color = TextPrimary)
                        }
                    }

                    listOf(5, 15, 30, 45, 60).forEach { mins ->
                        Button(
                            onClick = {
                                pm.startSleepTimer(mins)
                                showSleepTimerSheet = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("$mins Minutes", color = TextPrimary)
                        }
                    }
                }
            },
            containerColor = CustomSurface
        )
    }

    // --- Playback Speed chooser ---
    if (showSpeedSheet) {
        AlertDialog(
            onDismissRequest = { showSpeedSheet = false },
            confirmButton = {
                TextButton(onClick = { showSpeedSheet = false }) {
                    Text("DONE", color = CyberCyan)
                }
            },
            title = { Text("Adjust Playback Speed", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0.5f, 0.8f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { value ->
                        val selected = speed == value
                        Button(
                            onClick = {
                                pm.setPlaybackSpeed(value)
                                showSpeedSheet = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) NeonPurple else MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("${value}x Speed", color = if (selected) DeepDarkBackground else TextPrimary)
                        }
                    }
                }
            },
            containerColor = CustomSurface
        )
    }
}

// Helpers
fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

fun textColorForAccent(accent: Color): Color {
    return if (accent == CyberCyan) CyberCyan else if (accent == NeonPurple) NeonPurple else CyberMagenta
}

data class LyricLine(val timestampMs: Long, val text: String)

fun parseLyrics(lyricText: String): List<LyricLine> {
    val lines = lyricText.lines()
    val list = mutableListOf<LyricLine>()
    val regex = Regex("\\[(\\d+):(\\d+)\\](.*)")

    for (line in lines) {
        val match = regex.find(line)
        if (match != null) {
            val mins = match.groupValues[1].toLongOrNull() ?: 0L
            val secs = match.groupValues[2].toLongOrNull() ?: 0L
            val text = match.groupValues[3].trim()
            val totalMs = (mins * 60 + secs) * 1000
            list.add(LyricLine(totalMs, text))
        }
    }
    return list.sortedBy { it.timestampMs }
}
