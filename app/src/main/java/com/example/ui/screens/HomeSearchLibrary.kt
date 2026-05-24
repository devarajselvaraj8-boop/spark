package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import kotlinx.coroutines.delay
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.PlaylistEntity
import com.example.data.SongEntity
import com.example.ui.SparkViewModel
import com.example.ui.components.GlassCard
import com.example.ui.components.NeonButton
import com.example.ui.components.NeonText
import com.example.ui.theme.*

// --- Navigation Enum inside Composable ---
enum class SparkTab { HOME, SEARCH, LIBRARY, DOWNLOADS, SETTINGS, ADMIN }

@Composable
fun MainLayoutScreen(
    viewModel: SparkViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(SparkTab.HOME) }
    var selectedPlaylistId by remember { mutableStateOf<String?>(null) }
    var selectedArtistName by remember { mutableStateOf<String?>(null) }
    var selectedAlbumName by remember { mutableStateOf<String?>(null) }
    var showFullPlayer by remember { mutableStateOf(false) }

    val currentPlaybackSong by viewModel.playbackManager.currentSong.collectAsState()
    val isPlaying by viewModel.playbackManager.isPlaying.collectAsState()

    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepDarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            // Main Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Nested Screen Navigation Handling
                when {
                    selectedPlaylistId != null -> {
                        PlaylistDetailsScreen(
                            playlistId = selectedPlaylistId!!,
                            viewModel = viewModel,
                            onBack = { selectedPlaylistId = null }
                        )
                    }
                    selectedArtistName != null -> {
                        ArtistPageScreen(
                            artistName = selectedArtistName!!,
                            viewModel = viewModel,
                            onBack = { selectedArtistName = null }
                        )
                    }
                    selectedAlbumName != null -> {
                        AlbumPageScreen(
                            albumName = selectedAlbumName!!,
                            viewModel = viewModel,
                            onBack = { selectedAlbumName = null }
                        )
                    }
                    else -> {
                        when (activeTab) {
                            SparkTab.HOME -> HomeScreen(
                                viewModel = viewModel,
                                onPlaylistSelect = { selectedPlaylistId = it },
                                onArtistSelect = { selectedArtistName = it },
                                onAlbumSelect = { selectedAlbumName = it }
                            )
                            SparkTab.SEARCH -> SearchScreen(
                                viewModel = viewModel,
                                onPlaylistSelect = { selectedPlaylistId = it },
                                onArtistSelect = { selectedArtistName = it }
                            )
                            SparkTab.LIBRARY -> LibraryScreen(
                                viewModel = viewModel,
                                onPlaylistSelect = { selectedPlaylistId = it }
                            )
                            SparkTab.DOWNLOADS -> DownloadsScreen(
                                viewModel = viewModel
                            )
                            SparkTab.SETTINGS -> SettingsScreen(
                                viewModel = viewModel
                            )
                            SparkTab.ADMIN -> AdminDashboardScreen(
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }

            // Bottom Mini Player
            if (currentPlaybackSong != null) {
                MiniPlayer(
                    song = currentPlaybackSong!!,
                    isPlaying = isPlaying,
                    onPlayPause = { viewModel.playbackManager.togglePlayPause() },
                    onSkipNext = { viewModel.playbackManager.next() },
                    onExpand = { showFullPlayer = true }
                )
            }

            // Standard Bottom M3 Navigation Bar
            NavigationBar(
                containerColor = CustomSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    Triple(SparkTab.HOME, "Home", Icons.Default.MusicNote),
                    Triple(SparkTab.SEARCH, "Search", Icons.Default.Search),
                    Triple(SparkTab.LIBRARY, "Library", Icons.Default.Favorite),
                    Triple(SparkTab.DOWNLOADS, "Downloads", Icons.Default.Download),
                    Triple(SparkTab.SETTINGS, "Settings", Icons.Default.Settings),
                    Triple(SparkTab.ADMIN, "Admin", Icons.Default.AdminPanelSettings)
                ).forEach { (tab, label, icon) ->
                    val selected = activeTab == tab && selectedPlaylistId == null && selectedArtistName == null && selectedAlbumName == null
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            selectedPlaylistId = null
                            selectedArtistName = null
                            selectedAlbumName = null
                            activeTab = tab
                        },
                        icon = { Icon(imageVector = icon, contentDescription = label) },
                        label = { Text(text = label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DeepDarkBackground,
                            selectedTextColor = CyberCyan,
                            indicatorColor = CyberCyan,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
                    )
                }
            }
        }

        // Animated Full Music Player Sheet
        AnimatedVisibility(
            visible = showFullPlayer,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            FullMusicPlayerScreen(
                viewModel = viewModel,
                onMinimize = { showFullPlayer = false }
            )
        }
    }
}

// --- Home Screen ---
@Composable
fun HomeScreen(
    viewModel: SparkViewModel,
    onPlaylistSelect: (String) -> Unit,
    onArtistSelect: (String) -> Unit,
    onAlbumSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val songs by viewModel.allSongs.collectAsState()
    val playlists by viewModel.allPlaylists.collectAsState()
    val isGenerating by viewModel.aiPlaylistsGenerating.collectAsState()

    var aiPrompt by remember { mutableStateOf("") }
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 48.dp)
    ) {
        // Top Welcome Branding
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Geometric Balance Spark icon
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(CyberCyan, NeonPurple)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = "Logo",
                            tint = DeepDarkBackground,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "HELLO,",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        NeonText(
                            text = viewModel.currentUserName.collectAsState().value ?: "Spark Member",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            glowColor = CyberCyan
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF374151), Color(0xFF111827))
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                        .clickable {
                            Toast.makeText(context, "Listening minutes: ${songs.sumOf { it.playCount } * 4}", Toast.LENGTH_SHORT).show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = "User", tint = TextPrimary, modifier = Modifier.size(20.dp))
                }
            }
        }

        // Featured Release Banner (Geometric Balance)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .clip(RoundedCornerShape(24.dp))
                    .clickable {
                        val neonDrive = songs.find { it.id == "1" } ?: songs.firstOrNull()
                        if (neonDrive != null) {
                            viewModel.playbackManager.playSong(neonDrive, songs)
                            Toast.makeText(context, "Streaming: Neon Drive", Toast.LENGTH_SHORT).show()
                        }
                    }
            ) {
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?auto=format&fit=crop&q=80&w=800",
                    contentDescription = "New Release Cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xCC1E3A8A), // From-blue-900/80
                                    Color(0x99581C87)  // To-purple-900/60
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "NEW RELEASE",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Electric Dreams",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            lineHeight = 32.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "Lumina feat. Kora • Album",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val neonDrive = songs.find { it.id == "1" } ?: songs.firstOrNull()
                                if (neonDrive != null) {
                                    viewModel.playbackManager.playSong(neonDrive, songs)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                text = "Play Now",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.3f))
                                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                                .clickable {
                                    val neonDrive = songs.find { it.id == "1" }
                                    if (neonDrive != null) {
                                        viewModel.toggleLikeSong(neonDrive)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FavoriteBorder,
                                contentDescription = "Like New Release",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Spark AI DJ Panel (MANDATORY GEMINI AI FEAT)
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(24.dp))
                        Text(
                            text = "Spark AI Playlist DJ",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = "Type any vibe, mood or activity (e.g. \"late night retro neon cyber roadtrip\") and let Gemini customize an immediate matching stream.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    OutlinedTextField(
                        value = aiPrompt,
                        onValueChange = { aiPrompt = it },
                        placeholder = { Text("How are you feeling right now?", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedLabelColor = CyberCyan
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("ai_prompt_input")
                    )

                    Button(
                        onClick = {
                            if (aiPrompt.isNotBlank()) {
                                viewModel.generateAiMoodPlaylist(aiPrompt) { curatedPlaylist ->
                                    onPlaylistSelect(curatedPlaylist.id)
                                    Toast.makeText(context, "Playlist curated successfully!", Toast.LENGTH_SHORT).show()
                                    aiPrompt = ""
                                }
                            } else {
                                Toast.makeText(context, "Please enter a mood query first", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !isGenerating,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = DeepDarkBackground),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp).testTag("ai_curate_button")
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = DeepDarkBackground, strokeWidth = 2.dp)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.ElectricBolt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text(" CURATE AI PLAYLIST", fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }
        }

        // Trending Moods (From Geometric Balance Theme)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "Trending Moods", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF2563EB), Color(0xFF06B6D4))
                                )
                            )
                            .clickable {
                                viewModel.generateAiMoodPlaylist("Chill") { generated ->
                                    onPlaylistSelect(generated.id)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Chill",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF9333EA), Color(0xFFEC4899))
                                )
                            )
                            .clickable {
                                viewModel.generateAiMoodPlaylist("Energy Cyberpunk") { generated ->
                                    onPlaylistSelect(generated.id)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Energy",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFEA580C), Color(0xFFEAB308))
                                )
                            )
                            .clickable {
                                viewModel.generateAiMoodPlaylist("Deep Focus Study") { generated ->
                                    onPlaylistSelect(generated.id)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Focus",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Playlists Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Personalised Playlists", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(playlists) { playlist ->
                        PlaylistGridCard(playlist = playlist, onClick = { onPlaylistSelect(playlist.id) })
                    }
                }
            }
        }

        // Trending Songs List
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Trending Now", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "See All", color = CyberCyan, fontSize = 12.sp, modifier = Modifier.clickable {})
            }
        }

        // List Songs
        if (songs.isEmpty()) {
            item {
                Text("Your library is loading...", color = TextSecondary)
            }
        } else {
            items(songs) { song ->
                SongRowItem(
                    song = song,
                    onPlay = { viewModel.playbackManager.playSong(song, songs) },
                    onLike = { viewModel.toggleLikeSong(song) },
                    onDownload = {
                        if (song.isDownloaded) {
                            viewModel.deleteDownloadedSong(song)
                        } else {
                            viewModel.downloadSong(song)
                        }
                    },
                    progress = viewModel.downloadProgresses.collectAsState().value[song.id]
                )
            }
        }

        // Featured Artists
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "Featured Artists", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(songs.map { it.artist }.distinct()) { artist ->
                        ArtistRoundCard(artist = artist, onClick = { onArtistSelect(artist) })
                    }
                }
            }
        }
    }
}

// --- Search Screen ---
@Composable
fun SearchScreen(
    viewModel: SparkViewModel,
    onPlaylistSelect: (String) -> Unit,
    onArtistSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    val songs by viewModel.allSongs.collectAsState()
    
    // Voice Search Trigger Mock
    var isListeningByVoice by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val filteredSongs = songs.filter {
        it.title.contains(query, ignoreCase = true) ||
        it.artist.contains(query, ignoreCase = true) ||
        it.genre.contains(query, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Songs, artists, or genres...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = CyberCyan) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                ),
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("search_field_input")
            )

            // Voice Mic Button
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .clickable {
                        isListeningByVoice = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Search",
                    tint = CyberCyan
                )
            }
        }

        if (isListeningByVoice) {
            VoiceListeningMock(
                onHeard = { heard ->
                    query = heard
                    isListeningByVoice = false
                },
                onCancel = { isListeningByVoice = false }
            )
        }

        Text(
            text = if (query.isEmpty()) "Browse All Categories" else "Search Results",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredSongs) { song ->
                SongRowItem(
                    song = song,
                    onPlay = { viewModel.playbackManager.playSong(song, filteredSongs) },
                    onLike = { viewModel.toggleLikeSong(song) },
                    onDownload = {
                        if (song.isDownloaded) viewModel.deleteDownloadedSong(song) else viewModel.downloadSong(song)
                    },
                    progress = viewModel.downloadProgresses.collectAsState().value[song.id]
                )
            }
            if (filteredSongs.isEmpty()) {
                item {
                    Text("No matched tracks found.", color = TextSecondary)
                }
            }
        }
    }
}

// --- Voice Search Mock Dialog ---
@Composable
fun VoiceListeningMock(
    onHeard: (String) -> Unit,
    onCancel: () -> Unit
) {
    val searchTerms = listOf("Neon Drive", "Sunset", "Ambient Chill", "Midnight", "Acoustic Whispers", "Techno")
    val heardOne = remember { searchTerms.random() }

    LaunchedEffect(Unit) {
        delay(2000)
        onHeard(heardOne)
    }

    AlertDialog(
        onDismissRequest = onCancel,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Cancel", color = CyberMagenta) }
        },
        title = { Text(" Listening for Sound matching Shazam...", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(color = CyberCyan, modifier = Modifier.size(56.dp))
                Text("We're listening to your hum or surrounding tracks...", color = TextSecondary, textAlign = TextAlign.Center)
            }
        },
        containerColor = CustomSurface
    )
}

// --- Library Screen ---
@Composable
fun LibraryScreen(
    viewModel: SparkViewModel,
    onPlaylistSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val playlists by viewModel.allPlaylists.collectAsState()
    val songs by viewModel.allSongs.collectAsState()
    val history by viewModel.listeningHistory.collectAsState()

    var showPlaylistDialog by remember { mutableStateOf(false) }
    var newPlName by remember { mutableStateOf("") }
    var newPlDesc by remember { mutableStateOf("") }

    val likedSongs = songs.filter { it.isLiked }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(bottom = 48.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Your Music Vault", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                IconButton(onClick = { showPlaylistDialog = true }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Playlist", tint = CyberCyan)
                }
            }
        }

        // Liked Songs card row shortcut
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // Create temporary playlist for Liked Songs to details screen or alert
                        viewModel.createPlaylist("Favorites Folder", "Your liked songs")
                    }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.linearGradient(listOf(CyberMagenta, NeonPurple))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(28.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Favorites Folder", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("${likedSongs.size} Songs saved offline", color = TextSecondary, fontSize = 12.sp)
                    }
                    Icon(imageVector = Icons.Default.ArrowForwardIos, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                }
            }
        }

        item {
            Text(text = "Custom Playlists", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(playlists) { playlist ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPlaylistSelect(playlist.id) }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = playlist.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = playlist.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = playlist.description, color = TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                IconButton(onClick = { viewModel.deletePlaylist(playlist.id) }) {
                    Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextSecondary)
                }
            }
        }

        if (playlists.isEmpty()) {
            item {
                Text("Click the + icon in top right to create custom folders", color = TextSecondary)
            }
        }

        // Histroy
        if (history.isNotEmpty()) {
            item {
                Text(text = "Recently Played Tracks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            items(history.take(5)) { hist ->
                val song = songs.find { it.id == hist.songId }
                if (song != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.playbackManager.playSong(song, songs) }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = song.imageUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(song.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(song.artist, color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }

    if (showPlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showPlaylistDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    if (newPlName.isNotBlank()) {
                        viewModel.createPlaylist(newPlName, newPlDesc)
                        showPlaylistDialog = false
                        newPlName = ""
                        newPlDesc = ""
                    }
                }) {
                    Text("CREATE", color = CyberCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPlaylistDialog = false }) { Text("Cancel", color = TextSecondary) }
            },
            title = { Text("Create Custom Playlist", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newPlName,
                        onValueChange = { newPlName = it },
                        label = { Text("Playlist Name") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan)
                    )
                    OutlinedTextField(
                        value = newPlDesc,
                        onValueChange = { newPlDesc = it },
                        label = { Text("Short Description") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberCyan)
                    )
                }
            },
            containerColor = CustomSurface
        )
    }
}

// --- Downloads Screen ---
@Composable
fun DownloadsScreen(
    viewModel: SparkViewModel,
    modifier: Modifier = Modifier
) {
    val songs by viewModel.allSongs.collectAsState()
    val downloaded = songs.filter { it.isDownloaded }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Downloaded Offline Media", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))

        // Device Memory Storage details
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total offline allocated: ${downloaded.size * 3.4} MB", fontWeight = FontWeight.Bold)
                    Text("Available storage size: 34 GB", fontSize = 11.sp, color = TextSecondary)
                }
                Icon(imageVector = Icons.Default.SdStorage, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(32.dp))
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(downloaded) { song ->
                SongRowItem(
                    song = song,
                    onPlay = { viewModel.playbackManager.playSong(song, downloaded) },
                    onLike = { viewModel.toggleLikeSong(song) },
                    onDownload = { viewModel.deleteDownloadedSong(song) },
                    progress = null
                )
            }
            if (downloaded.isEmpty()) {
                item {
                    Text("You don't have any downloaded tracks offline. Click the download icon on any song on Home flow to download.", color = TextSecondary)
                }
            }
        }
    }
}

// --- Playlist Details Screen ---
@Composable
fun PlaylistDetailsScreen(
    playlistId: String,
    viewModel: SparkViewModel,
    onBack: () -> Unit
) {
    val plist = viewModel.allPlaylists.collectAsState().value.find { it.id == playlistId }
    val allSongs = viewModel.allSongs.collectAsState().value

    if (plist == null) {
        onBack()
        return
    }

    val playlistSongs = allSongs.filter { plist.getSongIds().contains(it.id) }
    val otherSongs = allSongs.filter { !plist.getSongIds().contains(it.id) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 48.dp)
    ) {
        item {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AsyncImage(
                    model = plist.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Column {
                    Text(plist.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                    Text(plist.description, color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (playlistSongs.isNotEmpty()) {
                                viewModel.playbackManager.playSong(playlistSongs.first(), playlistSongs)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = DeepDarkBackground)
                    ) {
                        Text("PLAY ALL", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text(text = "Playlist Tracks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
        }

        items(playlistSongs) { song ->
            SongRowItem(
                song = song,
                onPlay = { viewModel.playbackManager.playSong(song, playlistSongs) },
                onLike = { viewModel.toggleLikeSong(song) },
                onDownload = {
                    if (song.isDownloaded) viewModel.deleteDownloadedSong(song) else viewModel.downloadSong(song)
                },
                progress = viewModel.downloadProgresses.collectAsState().value[song.id]
            )
        }

        if (playlistSongs.isEmpty()) {
            item {
                Text("Playlist is empty. Choose tracks from below to add.", color = TextSecondary)
            }
        }

        if (otherSongs.isNotEmpty()) {
            item {
                Text(text = "Add Songs to Folder", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
            }
            items(otherSongs) { song ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(song.title + " by " + song.artist)
                    IconButton(onClick = { viewModel.addSongToPlaylist(plist.id, song.id) }) {
                        Icon(imageVector = Icons.Default.AddCircleOutline, contentDescription = "Add", tint = CyberCyan)
                    }
                }
            }
        }
    }
}

// --- Artist Profile Screen ---
@Composable
fun ArtistPageScreen(
    artistName: String,
    viewModel: SparkViewModel,
    onBack: () -> Unit
) {
    val songs = viewModel.allSongs.collectAsState().value.filter { it.artist == artistName }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 48.dp)
    ) {
        item {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(CyberCyan),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = DeepDarkBackground, modifier = Modifier.size(50.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
                NeonText(text = artistName, style = MaterialTheme.typography.titleLarge, glowColor = CyberCyan)
                Text("Popular Verified Artist • 12,504 Monthly Streams", fontSize = 11.sp, color = TextSecondary)
            }
        }

        item {
            Text("Top Popular Tracks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(songs) { song ->
            SongRowItem(
                song = song,
                onPlay = { viewModel.playbackManager.playSong(song, songs) },
                onLike = { viewModel.toggleLikeSong(song) },
                onDownload = {
                    if (song.isDownloaded) viewModel.deleteDownloadedSong(song) else viewModel.downloadSong(song)
                },
                progress = viewModel.downloadProgresses.collectAsState().value[song.id]
            )
        }
    }
}

// --- Album Page Screen ---
@Composable
fun AlbumPageScreen(
    albumName: String,
    viewModel: SparkViewModel,
    onBack: () -> Unit
) {
    val songs = viewModel.allSongs.collectAsState().value.filter { it.album == albumName }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 48.dp)
    ) {
        item {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (songs.isNotEmpty()) {
                    AsyncImage(
                        model = songs.first().imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(albumName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (songs.isNotEmpty()) {
                    Text("Album by ${songs.first().artist}", color = TextSecondary, fontSize = 12.sp)
                }
            }
        }

        item {
            Text("Tracks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(songs) { song ->
            SongRowItem(
                song = song,
                onPlay = { viewModel.playbackManager.playSong(song, songs) },
                onLike = { viewModel.toggleLikeSong(song) },
                onDownload = {
                    if (song.isDownloaded) viewModel.deleteDownloadedSong(song) else viewModel.downloadSong(song)
                },
                progress = viewModel.downloadProgresses.collectAsState().value[song.id]
            )
        }
    }
}

// --- Settings Screen ---
@Composable
fun SettingsScreen(
    viewModel: SparkViewModel,
    modifier: Modifier = Modifier
) {
    var crossfadeSeconds by remember { mutableStateOf(5f) }
    var highQualityMode by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "App Spark Configurations", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Audio Quality", fontWeight = FontWeight.Bold, color = CyberCyan)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Simulate Lossless 320kbps", fontWeight = FontWeight.SemiBold)
                        Text("Higher data stream allocations for sound cards", fontSize = 11.sp, color = TextSecondary)
                    }
                    Switch(
                        checked = highQualityMode,
                        onCheckedChange = { highQualityMode = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberCyan)
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                Text("Crossfade Gaps", fontWeight = FontWeight.Bold, color = NeonPurple)
                Column {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Crossfade Duration")
                        Text("${crossfadeSeconds.toInt()}s")
                    }
                    Slider(
                        value = crossfadeSeconds,
                        onValueChange = { crossfadeSeconds = it },
                        valueRange = 0f..15f,
                        colors = SliderDefaults.colors(activeTrackColor = NeonPurple)
                    )
                }
            }
        }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Membership Profile", fontWeight = FontWeight.Bold, color = CyberMagenta)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(viewModel.currentUserEmail.collectAsState().value ?: "anonymous@spark.com")
                    Button(
                        onClick = { viewModel.logout() },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberMagenta)
                    ) {
                        Text("LOG OUT")
                    }
                }
            }
        }
    }
}

// --- Admin Panel Dashboard Screen ---
@Composable
fun AdminDashboardScreen(
    viewModel: SparkViewModel,
    modifier: Modifier = Modifier
) {
    var sTitle by remember { mutableStateOf("") }
    var sArtist by remember { mutableStateOf("") }
    var sAlbum by remember { mutableStateOf("") }
    var sGenre by remember { mutableStateOf("") }
    var sUrl by remember { mutableStateOf("") }
    var sCoverUrl by remember { mutableStateOf("") }

    val songs by viewModel.allSongs.collectAsState()
    val adminSongs = songs.filter { it.isCustomUploaded }
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 60.dp)
    ) {
        item {
            Text(text = "Creator Admin Panel", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 12.dp))
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Add Song Stream to Catalog", fontWeight = FontWeight.Bold, color = CyberCyan)

                    OutlinedTextField(value = sTitle, onValueChange = { sTitle = it }, label = { Text("Song Title") }, modifier = Modifier.fillMaxWidth().testTag("admin_title"))
                    OutlinedTextField(value = sArtist, onValueChange = { sArtist = it }, label = { Text("Artist Name") }, modifier = Modifier.fillMaxWidth().testTag("admin_artist"))
                    OutlinedTextField(value = sAlbum, onValueChange = { sAlbum = it }, label = { Text("Album Name") }, modifier = Modifier.fillMaxWidth().testTag("admin_album"))
                    OutlinedTextField(value = sGenre, onValueChange = { sGenre = it }, label = { Text("Genre label") }, modifier = Modifier.fillMaxWidth().testTag("admin_genre"))
                    OutlinedTextField(value = sUrl, onValueChange = { sUrl = it }, label = { Text("Stream MP3 URL (Optional)") }, modifier = Modifier.fillMaxWidth().testTag("admin_mp3_url"))
                    OutlinedTextField(value = sCoverUrl, onValueChange = { sCoverUrl = it }, label = { Text("Cover Image URL (Optional)") }, modifier = Modifier.fillMaxWidth().testTag("admin_cover_url"))

                    Button(
                        onClick = {
                            if (sTitle.isNotBlank() && sArtist.isNotBlank()) {
                                viewModel.adminUploadSong(sTitle, sArtist, sAlbum, sGenre, sUrl, sCoverUrl)
                                Toast.makeText(context, "Uploaded to local Spark Catalog!", Toast.LENGTH_SHORT).show()
                                sTitle = ""
                                sArtist = ""
                                sAlbum = ""
                                sGenre = ""
                                sUrl = ""
                                sCoverUrl = ""
                            } else {
                                Toast.makeText(context, "Fill title and artist first", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = DeepDarkBackground),
                        modifier = Modifier.fillMaxWidth().testTag("admin_upload_button")
                    ) {
                        Text("SAVE & COMPILE TO DATABASE", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        if (adminSongs.isNotEmpty()) {
            item {
                Text("Uploaded Tracks", fontWeight = FontWeight.Bold, color = Color.White)
            }
            items(adminSongs) { song ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(song.title, fontWeight = FontWeight.Bold)
                        Text(song.artist, color = TextSecondary, fontSize = 11.sp)
                    }
                    IconButton(onClick = { viewModel.adminDeleteSong(song.id) }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = CyberMagenta)
                    }
                }
            }
        }
    }
}

// --- Visual Cards Elements Composed ---

@Composable
fun PlaylistGridCard(
    playlist: PlaylistEntity,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(130.dp)
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = playlist.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(130.dp)
                .clip(RoundedCornerShape(12.dp))
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = playlist.name, maxLines = 1, fontWeight = FontWeight.Bold, fontSize = 13.sp, overflow = TextOverflow.Ellipsis)
        Text(text = playlist.description, maxLines = 1, color = TextSecondary, fontSize = 11.sp, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun ArtistRoundCard(
    artist: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(CyberCyan, NeonPurple))),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = DeepDarkBackground, modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = artist, maxLines = 1, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun SongRowItem(
    song: SongEntity,
    onPlay: () -> Unit,
    onLike: () -> Unit,
    onDownload: () -> Unit,
    progress: Float?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() }
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(6.dp))
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(text = song.title, maxLines = 1, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = song.artist, maxLines = 1, color = TextSecondary, fontSize = 12.sp)
        }

        // Action controls
        IconButton(onClick = onLike, modifier = Modifier.size(24.dp)) {
            Icon(
                imageVector = if (song.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = null,
                tint = if (song.isLiked) CyberMagenta else TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }

        IconButton(onClick = onDownload, modifier = Modifier.size(24.dp)) {
            if (progress != null && progress > 0.0f && progress < 1.0f) {
                CircularProgressIndicator(progress = { progress }, modifier = Modifier.size(16.dp), color = CyberCyan, strokeWidth = 2.dp)
            } else {
                Icon(
                    imageVector = if (song.isDownloaded) Icons.Default.DownloadDone else Icons.Default.Download,
                    contentDescription = null,
                    tint = if (song.isDownloaded) CyberCyan else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun MiniPlayer(
    song: SongEntity,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onExpand: () -> Unit
) {
    val barColor = when (song.genre.lowercase()) {
        "synthwave" -> CyberCyan
        "vaporwave" -> CyberMagenta
        "ambient" -> NeonPurple
        else -> CyberCyan
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable { onExpand() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        cornerRadius = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = song.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(6.dp))
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(text = song.title, maxLines = 1, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = song.artist, maxLines = 1, color = TextSecondary, fontSize = 11.sp)
                // Simulated linear progress bar on mini
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(2.dp)
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(top = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (isPlaying) 0.45f else 0.15f)
                            .height(2.dp)
                            .background(barColor)
                    )
                }
            }

            IconButton(onClick = onPlayPause, modifier = Modifier.testTag("mini_play_pause")) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = TextPrimary
                )
            }

            IconButton(onClick = onSkipNext, modifier = Modifier.testTag("mini_skip_next")) {
                Icon(imageVector = Icons.Default.SkipNext, contentDescription = null, tint = TextPrimary)
            }
        }
    }
}

// Extension to M3 typography helper for customizable label sizing safely
fun androidx.compose.material3.Typography.labelDeepSmall() = TextStyle(
    fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
    fontWeight = FontWeight.Bold,
    fontSize = 10.sp,
    letterSpacing = 1.25.sp
)
