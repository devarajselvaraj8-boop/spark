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
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items

// --- Navigation Enum inside Composable ---
enum class SparkTab { HOME, SEARCH, ARTISTS, LIBRARY, DOWNLOADS, SETTINGS, ADMIN }

// --- Artist Profile Data Class ---
data class ArtistProfile(
    val id: String,
    val name: String,
    val tamilName: String,
    val imageUrl: String,
    val bio: String,
    val tamilBio: String,
    val monthlyListeners: String,
    val followers: String,
    val albums: List<String> = emptyList(),
    val relatedArtists: List<String> = emptyList(),
    val trendingSongs: List<String> = emptyList()
)

val legendaryArtistsList = listOf(
    ArtistProfile(
        id = "ani",
        name = "Anirudh Ravichander",
        tamilName = "அனிருத் ரவிச்சந்தர்",
        imageUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&q=80",
        bio = "Anirudh Ravichander is one of India's most popular young composers and playback singers. Known for high-energy mass beats, rock fusions, and viral global anthems.",
        tamilBio = "அனிருத் ரவிச்சந்தர் இந்தியாவின் மிகவும் புகழ்பெற்ற இளம் இசையமைப்பாளர் மற்றும் பின்னணி பாடகர் ஆவார். அவர் அனல் பறக்கும் மாஸ் பீட்ஸ் மற்றும் வைரல் ஹிட் பாடல்களுக்கு பெயர் பெற்றவர்.",
        monthlyListeners = "18.5M Monthly Streams",
        followers = "12,403,224 Followers",
        albums = listOf("Beast", "Jailer", "Master", "Leo"),
        relatedArtists = listOf("Yuvan Shankar Raja", "A.R. Rahman"),
        trendingSongs = listOf("Arabic Kuthu", "Hukum - Alappara", "Naa Ready", "Badass")
    ),
    ArtistProfile(
        id = "arr",
        name = "A.R. Rahman",
        tamilName = "ஏ.ஆர். ரஹ்மான்",
        imageUrl = "https://images.unsplash.com/photo-1511192336575-5a79af67a629?w=500&q=80",
        bio = "Allahrakha Rahman is an Oscar, Grammy, and BAFTA-winning legendary composer, record producer, singer, and songwriter. He revolutionized Indian film music.",
        tamilBio = "ஏ.ஆர். ரஹ்மான் ஆஸ்கார், கிராமி விருதுகளை வென்ற உலகப் புகழ்பெற்ற இசையமைப்பாளர். அவர் இந்தியத் திரை இசையில் புதிய புரட்சியை ஏற்படுத்தியவர்.",
        monthlyListeners = "24.2M Monthly Streams",
        followers = "18,904,221 Followers",
        albums = listOf("Roja", "Mersal", "Maryan", "VTV"),
        relatedArtists = listOf("Ilaiyaraaja", "Sid Sriram"),
        trendingSongs = listOf("Pudhu Vellai Mazhai", "Musthafa Musthafa", "Aalaporaan Thamizhan")
    ),
    ArtistProfile(
        id = "sid",
        name = "Sid Sriram",
        tamilName = "சித் ஸ்ரீராம்",
        imageUrl = "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?w=500&q=80",
        bio = "Sid Sriram is an Indian-American playback singer and songwriter. Combining soulful Carnatic elements with contemporary R&B and soul singing styles.",
        tamilBio = "சித் ஸ்ரீராம் ஒரு புகழ்பெற்ற கர்நாடக இசை மற்றும் ஆர்&பி பின்னணி பாடகர் ஆவார். அவர் ஆன்மீக மெலடிகளையும் புத்துணர்ச்சியூட்டும் குரலையும் கொண்டவர்.",
        monthlyListeners = "9.8M Monthly Streams",
        followers = "5,112,042 Followers",
        albums = listOf("Bachelor", "Oh Manapenne", "Vendhu Thanindhathu Kaadu"),
        relatedArtists = listOf("A.R. Rahman", "Yuvan Shankar Raja"),
        trendingSongs = listOf("Adiye", "Bodhai Kaname", "Marakkuma Nenjam")
    ),
    ArtistProfile(
        id = "yuv",
        name = "Yuvan Shankar Raja",
        tamilName = "யுவன் சங்கர் ராஜா",
        imageUrl = "https://images.unsplash.com/photo-1514320291840-2e0a9bf2a9ae?w=500&q=80",
        bio = "Yuvan Shankar Raja is an iconic composer, singer, and songwriter known as the 'King of BGM'. He popularized hip-hop and techno-beats in Kollywood.",
        tamilBio = "யுவன் சங்கர் ராஜா பின்னணி இசையின் மன்னன் என்று போற்றப்படும் மிகச்சிறந்த இசையமைப்பாளர். அவர் தமிழ் திரையிசையில் ஹிப்-ஹாப் மற்றும் மேற்கத்திய இசையை பரவலாக்கினார்.",
        monthlyListeners = "11.1M Monthly Streams",
        followers = "7,812,490 Followers",
        albums = listOf("Paiyaa", "Maari 2", "Natpe Thunai"),
        relatedArtists = listOf("Ilaiyaraaja", "Anirudh Ravichander"),
        trendingSongs = listOf("Rowdy Baby", "Thuli Thuli", "Single Pasanga")
    ),
    ArtistProfile(
        id = "ila",
        name = "Ilaiyaraaja",
        tamilName = "இளையராஜா",
        imageUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500&q=80",
        bio = "Ilaiyaraaja is a legendary composer, singer, and lyricist honored with the Padma Vibhushan. Regarded as one of India's greatest music maestros.",
        tamilBio = "இளையராஜா இந்தியாவின் இணையற்ற இசைஞானி மற்றும் பத்ம விபூஷண் விருது பெற்ற மாபெரும் மாஸ்ட்ரோ ஆவார். அவர் தமிழ் இசையின் ஆன்மாவாகக் கருதப்படுகிறார்.",
        monthlyListeners = "15.2M Monthly Streams",
        followers = "11,201,348 Followers",
        albums = listOf("Gunaa", "Classic Hits", "Anjali"),
        relatedArtists = listOf("A.R. Rahman", "Yuvan Shankar Raja"),
        trendingSongs = listOf("Kanmani Anbodu", "Anjali Anjali")
    )
)

// --- Tamil Translation Helper Dictionary ---
fun translate(text: String, isTamil: Boolean): String {
    if (!isTamil) return text
    return when (text.trim()) {
        "Home" -> "முகப்பு"
        "Search" -> "தேடல்"
        "Library" -> "நூலகம்"
        "Downloads" -> "பதிவிறக்கங்கள்"
        "Settings" -> "அமைப்புகள்"
        "Admin" -> "நிர்வாகி"
        "Artists" -> "கலைஞர்கள்"
        "Search music legends..." -> "கலைஞர்களைத் தேடுங்கள்..."
        "HELLO," -> "வணக்கம்,"
        "Trending Moods" -> "பிரபலமான மனநிலைகள்"
        "Personalised Playlists" -> "உங்களுக்கான பிளேலிஸ்ட்கள்"
        "Trending Now" -> "இப்போது பிரபலமாக இருப்பவை"
        "See All" -> "முழுவதும் காண்க"
        "Featured Artists" -> "பிரபல கலைஞர்கள்"
        "Your Music Vault" -> "உங்கள் இசை பெட்டகம்"
        "Custom Playlists" -> "தனிப்பயன் பிளேலிஸ்ட்கள்"
        "Recently Played Tracks" -> "சமீபத்தில் கேட்டவை"
        "Audio Quality" -> "ஆடியோ தரம்"
        "Crossfade Gaps" -> "பாடல்களின் இடைவெளி"
        "Membership Profile" -> "உறுப்பினர் கணக்கு"
        "Log Out" -> "வெளியேறு"
        "New Release" -> "புதிய வெளியீடு"
        "Play Now" -> "இயக்குக"
        "Spark AI Playlist DJ" -> "ஸ்பார்க் AI டிஜே"
        "Type any vibe, mood or activity (e.g. \"late night retro neon cyber roadtrip\") and let Gemini customize an immediate matching stream." -> "உங்களுக்கு பிடித்த மனநிலையை தட்டச்சு செய்யவும் (உதாரணமாக: \"அனிருத் மெலடி பாடல்கள்\") பின்னர் ஜெமினி AI உங்களுக்கான பாடல்களை இசைக்கும்."
        "How are you feeling right now?" -> "உங்களின் தற்போதைய மனநிலை என்ன?"
        "Please enter a mood query first" -> "தயவுசெய்து மனநிலையை உள்ளிடவும்"
        "Playlist curated successfully!" -> "பிளேலிஸ்ட் வெற்றிகரமாக உருவாக்கப்பட்டது!"
        "Browse All Categories" -> "அனைத்து பிரிவுகள்"
        "Search Results" -> "தேடல் முடிவுகள்"
        "Songs, artists, or genres..." -> "பாடல்கள், கலைஞர்கள் அல்லது ரகங்கள்..."
        "Your library is loading..." -> "உங்கள் பாடல்கள் ஏற்றப்படுகின்றன..."
        "Click the + icon in top right to create custom folders" -> "புதிய பிளேலிஸ்ட் உருவாக்க மேலே உள்ள + குறியீட்டை அழுத்தவும்"
        "Total offline allocated" -> "மொத்த பதிவிறக்கம்"
        "Available storage size" -> "கிடைக்கும் நினைவக அளவு"
        "PLAY ALL" -> "அனைத்தும் இயக்கு"
        "Playlist Tracks" -> "பிளேலிஸ்ட்டில் உள்ள பாடல்கள்"
        "Add Songs to Folder" -> "பாடல்களைச் சேர்க்கவும்"
        "Top Popular Tracks" -> "பிரபலமான பாடல்கள்"
        "Tracks" -> "பாடல்கள்"
        "App Spark Configurations" -> "பயன்பாட்டு அமைப்புகள்"
        "Audio Setup" -> "ஒலி அமைப்புகள்"
        "Simulate Lossless 320kbps" -> "உயர்தர 320kbps ஒலி"
        "Higher data stream allocations for sound cards" -> "சிறந்த ஒலி அலைவரிசையைப் பெறுங்கள்"
        "Crossfade Duration" -> "பாடல்களிடையேயான மாற்றம்"
        "HD Audio Equalized" -> "HD ஒலி சமநிலைப்படுத்தி"
        "Sleep Timer Countdown" -> "ஸ்லீப் டைமர் கவுண்ட்டவுன்"
        "Creator Admin Panel" -> "படைப்பாளி நிர்வாகி பேனல்"
        "Add Song Stream to Catalog" -> "புதிய பாடலைச் சேர்க்கவும்"
        "Song Title" -> "பாடல் பெயர் (Song Title)"
        "Artist Name" -> "கலைஞர் பெயர் (Artist Name)"
        "Album Name" -> "படம் / ஆல்பம் (Album Name)"
        "Genre label" -> "பாடல் வகை (Genre)"
        "Stream MP3 URL (Optional)" -> "ஆடியோ URL (MP3 URL - Optional)"
        "Cover Image URL (Optional)" -> "கவர் படம் URL (Cover URL - Optional)"
        "SAVE & COMPILE TO DATABASE" -> "தரவுத்தளத்தில் சேமிக்கவும்"
        "Uploaded Tracks" -> "பதிவேற்றப்பட்ட பாடல்கள்"
        "Favorites Folder" -> "விருப்பமானவை"
        "Your liked songs" -> "நீங்கள் விரும்பிய பாடல்கள்"
        "Songs saved offline" -> "பாடல்கள் சேமிக்கப்பட்டுள்ளன"
        "Downloaded Offline Media" -> "பதிவிறக்கம் செய்யப்பட்டவை"
        "You don't have any downloaded tracks offline. Click the download icon on any song on Home flow to download." -> "உங்களிடம் பதிவிறக்கம் செய்யப்பட்ட பாடல்கள் எதுவும் இல்லை."
        else -> text
    }
}

@Composable
fun MainLayoutScreen(
    viewModel: SparkViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(SparkTab.HOME) }
    var selectedPlaylistId by remember { mutableStateOf<String?>(null) }
    var selectedArtistName by remember { mutableStateOf<String?>(null) }
    var selectedAlbumName by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showFullPlayer by remember { mutableStateOf(false) }

    val currentPlaybackSong by viewModel.playbackManager.currentSong.collectAsState()
    val isPlaying by viewModel.playbackManager.isPlaying.collectAsState()
    
    val appLanguage by viewModel.appLanguage.collectAsState()
    val isTamil = appLanguage == "ta"

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
                    selectedCategory != null -> {
                        CategoryDetailsScreen(
                            categoryName = selectedCategory!!,
                            viewModel = viewModel,
                            onBack = { selectedCategory = null },
                            isTamil = isTamil
                        )
                    }
                    selectedArtistName != null -> {
                        ArtistPageScreen(
                            artistName = selectedArtistName!!,
                            viewModel = viewModel,
                            onBack = { selectedArtistName = null },
                            onArtistClick = { selectedArtistName = it },
                            onAlbumSelect = { selectedAlbumName = it },
                            isTamil = isTamil
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
                                onAlbumSelect = { selectedAlbumName = it },
                                onCategorySelect = { selectedCategory = it },
                                isTamil = isTamil
                            )
                            SparkTab.SEARCH -> SearchScreen(
                                viewModel = viewModel,
                                onPlaylistSelect = { selectedPlaylistId = it },
                                onArtistSelect = { selectedArtistName = it },
                                isTamil = isTamil
                            )
                            SparkTab.ARTISTS -> ArtistsTabScreen(
                                viewModel = viewModel,
                                onArtistSelect = { selectedArtistName = it },
                                isTamil = isTamil
                            )
                            SparkTab.LIBRARY -> LibraryScreen(
                                viewModel = viewModel,
                                onPlaylistSelect = { selectedPlaylistId = it },
                                isTamil = isTamil
                            )
                            SparkTab.DOWNLOADS -> DownloadsScreen(
                                viewModel = viewModel,
                                isTamil = isTamil
                            )
                            SparkTab.SETTINGS -> SettingsScreen(
                                viewModel = viewModel,
                                isTamil = isTamil
                            )
                            SparkTab.ADMIN -> AdminDashboardScreen(
                                viewModel = viewModel,
                                isTamil = isTamil
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
                    Triple(SparkTab.HOME, translate("Home", isTamil), Icons.Default.MusicNote),
                    Triple(SparkTab.SEARCH, translate("Search", isTamil), Icons.Default.Search),
                    Triple(SparkTab.ARTISTS, translate("Artists", isTamil), Icons.Default.Person),
                    Triple(SparkTab.LIBRARY, translate("Library", isTamil), Icons.Default.Favorite),
                    Triple(SparkTab.DOWNLOADS, translate("Downloads", isTamil), Icons.Default.Download),
                    Triple(SparkTab.SETTINGS, translate("Settings", isTamil), Icons.Default.Settings),
                    Triple(SparkTab.ADMIN, translate("Admin", isTamil), Icons.Default.AdminPanelSettings)
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
    onCategorySelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    isTamil: Boolean = false
) {
    val songs by viewModel.allSongs.collectAsState()
    val playlists by viewModel.allPlaylists.collectAsState()
    val isGenerating by viewModel.aiPlaylistsGenerating.collectAsState()
    val activeSong by viewModel.playbackManager.currentSong.collectAsState()
    
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
                            text = translate("HELLO,", isTamil),
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
                            text = translate("Spark AI Playlist DJ", isTamil),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = translate("Type any vibe, mood or activity (e.g. \"late night retro neon cyber roadtrip\") and let Gemini customize an immediate matching stream.", isTamil),
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    OutlinedTextField(
                        value = aiPrompt,
                        onValueChange = { aiPrompt = it },
                        placeholder = { Text(translate("How are you feeling right now?", isTamil), color = TextSecondary) },
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
                                    Toast.makeText(context, translate("Playlist curated successfully!", isTamil), Toast.LENGTH_SHORT).show()
                                    aiPrompt = ""
                                }
                            } else {
                                Toast.makeText(context, translate("Please enter a mood query first", isTamil), Toast.LENGTH_SHORT).show()
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
                                Text(" " + translate("CURATE AI PLAYLIST", isTamil), fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }
        }

        // --- EXCLUSIVE TAMIL ENTERTAINMENT & LOCALIZED REGIONAL SECTIONS ---
        item {
            LocalizedTamilFestivalsBanner(isTamil = isTamil)
        }

        item {
            TamilMusicSection(
                viewModel = viewModel,
                isTamil = isTamil,
                onPlaylistSelect = onPlaylistSelect,
                onCategorySelect = onCategorySelect
            )
        }

        item {
            RegionalVibeSelector(
                isTamil = isTamil,
                onSelectVibe = onCategorySelect
            )
        }

        // Trending Moods (From Geometric Balance Theme)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = translate("Trending Moods", isTamil), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                Text(text = translate("Personalised Playlists", isTamil), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                Text(text = translate("Trending Now", isTamil), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = translate("See All", isTamil), color = CyberCyan, fontSize = 12.sp, modifier = Modifier.clickable {})
            }
        }

        // List Songs
        if (songs.isEmpty()) {
            item {
                Text(translate("Your library is loading...", isTamil), color = TextSecondary)
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
                    progress = viewModel.downloadProgresses.collectAsState().value[song.id],
                    isActive = activeSong?.id == song.id
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
    modifier: Modifier = Modifier,
    isTamil: Boolean = false
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

    val matchingArtists = if (query.isBlank()) emptyList() else legendaryArtistsList.filter { artist ->
        artist.name.contains(query, ignoreCase = true) ||
        artist.tamilName.contains(query, ignoreCase = true) ||
        (query.lowercase() == "rahman" && artist.name.contains("A.R. Rahman")) ||
        (query.lowercase() == "ar rahman" && artist.name.contains("A.R. Rahman")) ||
        (query.lowercase() == "ani" && artist.name.contains("Anirudh")) ||
        (query.lowercase() == "yuvan" && artist.name.contains("Yuvan")) ||
        (query.lowercase() == "raja" && (artist.name.contains("Ilaiyaraaja") || artist.name.contains("Yuvan"))) ||
        (query.contains("ரகுமான்") && artist.name.contains("A.R. Rahman")) ||
        (query.contains("அனிருத்") && artist.name.contains("Anirudh")) ||
        (query.contains("சித்") && artist.name.contains("Sid")) ||
        (query.contains("இளையராஜா") && artist.name.contains("இளையராஜா")) ||
        (query.contains("யுவன்") && artist.name.contains("யுவன்"))
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
                placeholder = { Text(translate("Songs, artists, or genres...", isTamil)) },
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
                onCancel = { isListeningByVoice = false },
                isTamil = isTamil
            )
        }

        if (query.isEmpty()) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (isTamil) "பரிந்துரைக்கப்பட்ட கலைஞர்கள் 🌟" else "Suggested Artists 🌟",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(legendaryArtistsList) { artist ->
                        val artistLabel = if (isTamil) artist.tamilName else artist.name
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(90.dp)
                                .clickable { onArtistSelect(artist.name) }
                        ) {
                            AsyncImage(
                                model = artist.imageUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(75.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, CyberCyan, CircleShape)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = artistLabel,
                                maxLines = 1,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                textAlign = TextAlign.Center,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = if (isTamil) "பிரபலமான ரகங்கள் 🎵" else "Popular Genres 🎵",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                val genres = listOf("Kollywood", "Love Melody", "Mass Beats", "90s Classic", "Rap & Folk", "Spiritual Vibe")
                val genresTamil = listOf("கோலிவுட்", "காதல் மெலடி", "மாஸ் பீட்ஸ்", "90களின் கிளாசிக்", "ராப் & நாட்டுப்புறம்", "பக்தி அலைகள்")
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    items(genres.size) { index ->
                        val genreEngName = genres[index]
                        val genreLabel = if (isTamil) genresTamil[index] else genreEngName
                        
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .clickable { query = genreEngName }
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = genreLabel,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberCyan,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // When query is entered
            if (matchingArtists.isNotEmpty()) {
                Text(
                    text = if (isTamil) "பொருந்திய கலைஞர்கள்" else "Suggested Artists",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(matchingArtists) { artist ->
                        val artistLabel = if (isTamil) artist.tamilName else artist.name
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .clickable { onArtistSelect(artist.name) }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AsyncImage(
                                model = artist.imageUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                            )
                            Text(
                                text = artistLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
            
            Text(
                text = translate("Search Results", isTamil),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            val activeSong by viewModel.playbackManager.currentSong.collectAsState()
            
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
                        progress = viewModel.downloadProgresses.collectAsState().value[song.id],
                        isActive = activeSong?.id == song.id
                    )
                }
                if (filteredSongs.isEmpty()) {
                    item {
                        Text(translate("No matched tracks found.", isTamil), color = TextSecondary)
                    }
                }
            }
        }
    }
}

// --- Voice Search Mock Dialog ---
@Composable
fun VoiceListeningMock(
    onHeard: (String) -> Unit,
    onCancel: () -> Unit,
    isTamil: Boolean = false
) {
    val searchTerms = if (isTamil) {
        listOf("அரபிக் குத்து", "கண்மணி அன்போடு", "ஒரு மனம்", "என்ஜாய் எஞ்சாமி", "மாங்கல்யம் தந்துநானே", "போதை கணமே", "மதுரை வீரன்")
    } else {
        listOf("Neon Drive", "Sunset", "Ambient Chill", "Midnight", "Acoustic Whispers", "Techno")
    }
    val heardOne = remember { searchTerms.random() }

    LaunchedEffect(Unit) {
        delay(2500)
        onHeard(heardOne)
    }

    AlertDialog(
        onDismissRequest = onCancel,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onCancel) { Text(if (isTamil) "ரத்துசெய்" else "Cancel", color = CyberMagenta) }
        },
        title = { Text(if (isTamil) "வார்த்தைப் பரிந்துரைகளைக் கேட்கிறது... 🎙️" else "Listening for Voice Search or Chant... 🎙️", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(color = CyberCyan, modifier = Modifier.size(56.dp))
                Text(
                    text = if (isTamil) "தமிழ் ஒலிப்பதிவு மற்றும் தேடல் வார்த்தைகளைக் கூறவும் (எ.கா: \"அனிருத் பாடல்கள்\")" 
                           else "Speak or hum any Tamil transliteration query or song details (e.g. \"Anirudh songs\")...", 
                    color = TextSecondary, 
                    textAlign = TextAlign.Center
                )
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
    modifier: Modifier = Modifier,
    isTamil: Boolean = false
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
                Text(text = translate("Your Music Vault", isTamil), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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
    modifier: Modifier = Modifier,
    isTamil: Boolean = false
) {
    val songs by viewModel.allSongs.collectAsState()
    val downloaded = songs.filter { it.isDownloaded }
    val activeSong by viewModel.playbackManager.currentSong.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = translate("Downloaded Offline Media", isTamil), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))

        // Device Memory Storage details
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isTamil) "பதிவிறக்கம் செய்யப்பட்ட அளவு: ${String.format("%.1f", downloaded.size * 3.4)} MB" else "Total offline allocated: ${downloaded.size * 3.4} MB", 
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isTamil) "கிடைக்கக்கூடிய நினைவகம்: 34 GB" else "Available storage size: 34 GB", 
                        fontSize = 11.sp, 
                        color = TextSecondary
                    )
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
                    progress = null,
                    isActive = activeSong?.id == song.id
                )
            }
            if (downloaded.isEmpty()) {
                item {
                    Text(
                        text = if (isTamil) "உங்களிடம் பதிவிறக்கம் செய்யப்பட்ட பாடல்கள் ஏதும் இல்லை. பாடல்களை பதிவிறக்கம் செய்ய முகப்புத் திரையில் உள்ள பாடல்களுக்கு அருகில் இருக்கும் குறியீட்டை அழுத்தவும்." 
                               else "You don't have any downloaded tracks offline. Click the download icon on any song on Home flow to download.", 
                        color = TextSecondary
                    )
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
    val activeSong by viewModel.playbackManager.currentSong.collectAsState()

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
                progress = viewModel.downloadProgresses.collectAsState().value[song.id],
                isActive = activeSong?.id == song.id
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

// --- Category Details Screen ---
@Composable
fun CategoryDetailsScreen(
    categoryName: String,
    viewModel: SparkViewModel,
    onBack: () -> Unit,
    isTamil: Boolean
) {
    val allSongs = viewModel.allSongs.collectAsState().value
    val activeSong by viewModel.playbackManager.currentSong.collectAsState()
    val categorySongs = allSongs.filter { song ->
        song.genre.equals(categoryName, ignoreCase = true) || 
        (categoryName == "Kollywood Hits" && (song.genre == "Kollywood Hits" || song.genre == "Mass Songs" || song.genre == "Trending Tamil Songs")) ||
        (categoryName == "Mass Songs" && (song.genre == "Mass Songs" || song.genre == "Kollywood Hits")) ||
        (categoryName == "90s Tamil Classics" && song.genre == "90s Tamil Classics") ||
        (categoryName == "Tamil Love Songs" && song.genre == "Tamil Love Songs") ||
        (categoryName == "Melody Songs" && song.genre == "Melody Songs") ||
        (categoryName == "Devotional Songs" && song.genre == "Devotional Songs") ||
        (categoryName == "Tamil Rap & Indie" && song.genre == "Tamil Rap & Indie") ||
        (categoryName == "Trending Tamil Songs" && (song.genre == "Trending Tamil Songs" || song.genre == "Kollywood Hits")) ||
        (categoryName.contains("Chennai", ignoreCase = true) && (song.genre == "Kollywood Hits" || song.genre == "Trending Tamil Songs")) ||
        (categoryName.contains("Coimbatore", ignoreCase = true) && (song.genre == "Melody Songs" || song.genre == "Tamil Love Songs")) ||
        (categoryName.contains("Thanjavur", ignoreCase = true) && (song.genre == "90s Tamil Classics")) ||
        (categoryName.contains("Madurai", ignoreCase = true) && (song.genre == "Devotional Songs" || song.genre == "Tamil Rap & Indie"))
    }

    val displayTitle = when (categoryName) {
        "Kollywood Hits" -> if (isTamil) "கோலிவுட் ஹிட்டுகள்" else "Kollywood Hits"
        "Tamil Love Songs" -> if (isTamil) "காதல் மெலடிகள்" else "Tamil Love Songs"
        "Melody Songs" -> if (isTamil) "இனிமையான மெலடிகள்" else "Melody Songs"
        "Mass Songs" -> if (isTamil) "அனல் பறக்கும் மாஸ் பாடல்கள்" else "Mass Songs"
        "Devotional Songs" -> if (isTamil) "ஆன்மீக பக்தி அலைகள்" else "Devotional Songs"
        "90s Tamil Classics" -> if (isTamil) "90ஸ் எவர்கிரீன் கிளாசிக்ஸ்" else "90s Tamil Classics"
        "Trending Tamil Songs" -> if (isTamil) "சமீபத்திய ட்ரெண்டிங்" else "Trending Tamil Songs"
        "Tamil Rap & Indie" -> if (isTamil) "தமிழ் ராப் மற்றும் இண்டி" else "Tamil Rap & Indie"
        "Chennai" -> if (isTamil) "மெட்ராஸ் கெத்து பீட்ஸ்" else "Vibrant Chennai Beats"
        "Coimbatore" -> if (isTamil) "கோவை சாரல் மெலடிஸ்" else "Soothing Kovai Breeze"
        "Thanjavur" -> if (isTamil) "தஞ்சை பாரம்பரிய கிளாசிக்ஸ்" else "Historic Thanjavur Heritage"
        "Madurai" -> if (isTamil) "மதுரை வீரன் நாட்டுப்புறம்" else "Madurai Folk & Sacred Beats"
        else -> categoryName
    }

    var searchQuery by remember { mutableStateOf("") }
    val filteredSongs = categorySongs.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
        it.artist.contains(searchQuery, ignoreCase = true) ||
        it.album.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 60.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = TextPrimary
                )
            }
        }

        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isTamil) "${categorySongs.size} பாடல்கள் சேர்க்கப்பட்டுள்ளன" else "Collection contains ${categorySongs.size} dynamic tracks",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                if (categorySongs.isNotEmpty()) {
                                    viewModel.playbackManager.playSong(categorySongs.first(), categorySongs)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = DeepDarkBackground)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isTamil) "அனைத்தையும் இயக்கு" else "PLAY ALL", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(if (isTamil) "பாடல்கள் தேடு..." else "Search collection...", color = TextSecondary) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                    focusedLabelColor = CyberCyan
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            )
        }

        if (filteredSongs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (isTamil) "வடிகட்டப்பட்ட பாடல்கள் எதுவும் இல்லை" else "No matching songs in this section.", color = TextSecondary)
                }
            }
        }

        items(filteredSongs) { song ->
            SongRowItem(
                song = song,
                onPlay = { viewModel.playbackManager.playSong(song, categorySongs) },
                onLike = { viewModel.toggleLikeSong(song) },
                onDownload = {
                    if (song.isDownloaded) viewModel.deleteDownloadedSong(song) else viewModel.downloadSong(song)
                },
                progress = viewModel.downloadProgresses.collectAsState().value[song.id],
                isActive = activeSong?.id == song.id
            )
        }
    }
}

// --- Artist Profile Screen ---
@Composable
fun ArtistPageScreen(
    artistName: String,
    viewModel: SparkViewModel,
    onBack: () -> Unit,
    onArtistClick: (String) -> Unit = {},
    onAlbumSelect: (String) -> Unit = {},
    isTamil: Boolean = false
) {
    val allSongs by viewModel.allSongs.collectAsState()
    val activeSong by viewModel.playbackManager.currentSong.collectAsState()
    
    // Find matching profile
    val profile = legendaryArtistsList.find { it.name.equals(artistName, ignoreCase = true) }
    
    // Song filtering with robust substring and album mapping support
    val artistSongs = allSongs.filter { song ->
        song.artist.contains(artistName, ignoreCase = true) ||
        (artistName == "A.R. Rahman" && (song.artist.contains("Rahman", ignoreCase = true) || song.album == "Roja" || song.album == "VTV" || song.album == "Maryan" || song.album == "Mersal")) ||
        (artistName == "Anirudh Ravichander" && (song.artist.contains("Anirudh", ignoreCase = true) || song.album == "Beast" || song.album == "Jailer" || song.album == "Leo" || song.album == "Master" || song.album == "Don" || song.album == "Thiruchitrambalam")) ||
        (artistName == "Sid Sriram" && (song.artist.contains("Sid Sriram", ignoreCase = true) || song.album == "Bachelor" || song.album == "Oh Manapenne" || song.album == "Vendhu Thanindhathu Kaadu")) ||
        (artistName == "Yuvan Shankar Raja" && (song.artist.contains("Yuvan", ignoreCase = true) || song.album == "Paiyaa" || song.album == "Maari 2" || song.album == "Natpe Thunai")) ||
        (artistName == "Ilaiyaraaja" && (song.artist.contains("Ilaiyaraaja", ignoreCase = true) || song.album == "Gunaa" || song.album == "Anjali" || song.title.contains("Kanmani Anbodu") || song.title.contains("Anjali Anjali")))
    }

    // Follower list state simulated locally
    var isFollowing by remember(artistName) { mutableStateOf(false) }
    val context = LocalContext.current

    // Extract dynamic followers count
    val baseFollowers = profile?.followers ?: "2,350,119 Followers"
    // Extract monthly listeners
    val listeners = profile?.monthlyListeners ?: "125,000 Monthly Streams"

    val headerImage = profile?.imageUrl ?: "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&q=80"
    val displayName = if (isTamil && profile != null) profile.tamilName else artistName
    val bioText = if (isTamil && profile != null) profile.tamilBio else (profile?.bio ?: "Verified premium partner artist on Spark Tamil Music stream.")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Hero Image Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                AsyncImage(
                    model = headerImage,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Bottom fade overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    DeepDarkBackground.copy(alpha = 0.5f),
                                    DeepDarkBackground
                                )
                            )
                        )
                )
                
                // Back Button & Follow Button overlay
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, start = 12.dp, end = 12.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    
                    Button(
                        onClick = {
                            isFollowing = !isFollowing
                            val toastMsg = if (isFollowing) {
                                if (isTamil) "இப்போது நீங்கள் $displayName ஐப் பின்பற்றுகிறீர்கள்! 💖" else "Following $displayName!"
                            } else {
                                if (isTamil) "பின்பற்றுவது நிறுத்தப்பட்டது" else "Unfollowed $displayName"
                            }
                            Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFollowing) Color.White.copy(alpha = 0.15f) else CyberCyan
                        ),
                        modifier = Modifier.border(
                            if (isFollowing) 1.dp else 0.dp,
                            if (isFollowing) Color.White.copy(alpha = 0.3f) else Color.Transparent,
                            RoundedCornerShape(20.dp)
                        )
                    ) {
                        Text(
                            text = if (isFollowing) (if (isTamil) "பின்பற்றப்படுகிறது" else "Following") else (if (isTamil) "பின்பற்று" else "Follow"),
                            color = if (isFollowing) TextPrimary else DeepDarkBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                // Profile Bottom Metadata info
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified Partner",
                            tint = CyberCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isTamil) "சரிபார்க்கப்பட்ட கலைஞர்" else "VERIFIED ARTIST",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    NeonText(
                        text = displayName,
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                        glowColor = CyberCyan
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$listeners  •  $baseFollowers",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Play All / Shuffle buttons bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (artistSongs.isNotEmpty()) {
                            viewModel.playbackManager.playSong(artistSongs.first(), artistSongs)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = DeepDarkBackground)
                        Text(if (isTamil) "அனைத்தையும் இயக்கு" else "PLAY ALL", color = DeepDarkBackground, fontWeight = FontWeight.Black)
                    }
                }

                Button(
                    onClick = {
                        if (artistSongs.isNotEmpty()) {
                            viewModel.playbackManager.toggleShuffle()
                            val shuffled = artistSongs.shuffled()
                            viewModel.playbackManager.playSong(shuffled.first(), shuffled)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Shuffle, contentDescription = null, tint = TextPrimary)
                        Text(if (isTamil) "கலக்கு" else "SHUFFLE PLAY", color = TextPrimary, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        // Artist Biography Card
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isTamil) "சுயசரிதை 📖" else "Biography 📖",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = bioText,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = TextPrimary
                    )
                }
            }
        }

        // Popular Songs heading
        item {
            Text(
                text = if (isTamil) "பிரபலமான பாடல்கள் 🔥" else "Popular Tracks 🔥",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Songs render
        if (artistSongs.isEmpty()) {
            item {
                Text(
                    text = if (isTamil) "பாடல்கள் எதுவும் இல்லை." else "No popular tracks found.",
                    color = TextSecondary,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            items(artistSongs) { song ->
                SongRowItem(
                    song = song,
                    onPlay = { viewModel.playbackManager.playSong(song, artistSongs) },
                    onLike = { viewModel.toggleLikeSong(song) },
                    onDownload = {
                        if (song.isDownloaded) viewModel.deleteDownloadedSong(song) else viewModel.downloadSong(song)
                    },
                    progress = viewModel.downloadProgresses.collectAsState().value[song.id],
                    isActive = activeSong?.id == song.id
                )
            }
        }

        // Horizontal Albums List from this artist
        val uniqueAlbums = artistSongs.map { it.album }.distinct()
        if (uniqueAlbums.isNotEmpty()) {
            item {
                Text(
                    text = if (isTamil) "ஆல்பங்கள் 💿" else "Albums & Movies 💿",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uniqueAlbums) { album ->
                        val albumSong = artistSongs.firstOrNull { it.album == album }
                        val albumCover = albumSong?.imageUrl ?: headerImage
                        
                        Column(
                            modifier = Modifier
                                .width(110.dp)
                                .clickable { onAlbumSelect(album) },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AsyncImage(
                                model = albumCover,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = album,
                                maxLines = 1,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Related Artists
        val related = profile?.relatedArtists ?: emptyList()
        if (related.isNotEmpty()) {
            item {
                Text(
                    text = if (isTamil) "தொடர்புடைய கலைஞர்கள் 💫" else "Fans Also Like 💫",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(related) { relName ->
                        val relProfile = legendaryArtistsList.find { it.name == relName }
                        val relImage = relProfile?.imageUrl ?: headerImage
                        val relLabel = if (isTamil && relProfile != null) relProfile.tamilName else relName
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(80.dp)
                                .clickable { onArtistClick(relName) }
                        ) {
                            AsyncImage(
                                model = relImage,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, CyberCyan, CircleShape)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = relLabel,
                                maxLines = 1,
                                fontSize = 11.sp,
                                color = TextPrimary,
                                textAlign = TextAlign.Center,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Artists Tab Explorer Screen ---
@Composable
fun ArtistsTabScreen(
    viewModel: SparkViewModel,
    onArtistSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    isTamil: Boolean = false
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredArtists = legendaryArtistsList.filter { artist ->
        artist.name.contains(searchQuery, ignoreCase = true) ||
        artist.tamilName.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (isTamil) "புகழ்பெற்ற கலைஞர்கள் 🌟" else "Legendary Tamil Artists 🌟",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
            modifier = Modifier.padding(top = 12.dp)
        )
        
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(if (isTamil) "கலைஞர்களைத் தேடுங்கள்..." else "Search music legends...") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = CyberCyan) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberCyan,
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("artist_search_input")
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 60.dp)
        ) {
            items(filteredArtists) { artist ->
                val artistLabel = if (isTamil) artist.tamilName else artist.name
                
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onArtistSelect(artist.name) }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        AsyncImage(
                            model = artist.imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .border(2.dp, Brush.linearGradient(listOf(CyberCyan, CyberMagenta)), CircleShape)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = artistLabel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            overflow = TextOverflow.Ellipsis,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isTamil) "சரிபார்க்கப்பட்டது ✓" else "Verified ✓",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CyberCyan
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = artist.monthlyListeners.split(" ").firstOrNull() ?: "",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
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
    val activeSong by viewModel.playbackManager.currentSong.collectAsState()

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
                progress = viewModel.downloadProgresses.collectAsState().value[song.id],
                isActive = activeSong?.id == song.id
            )
        }
    }
}

// --- Settings Screen ---
@Composable
fun SettingsScreen(
    viewModel: SparkViewModel,
    modifier: Modifier = Modifier,
    isTamil: Boolean = false
) {
    var crossfadeSeconds by remember { mutableStateOf(5f) }
    var highQualityMode by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = translate("App Spark Configurations", isTamil), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))

        // Language Preferences Selection Card
        val currentLang by viewModel.appLanguage.collectAsState()
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isTamil) "செயலி மொழி / App Language 🌐" else "App Language Preferences 🌐",
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan
                )
                Text(
                    text = if (isTamil) "பாடல்கள், வரிகள் மற்றும் இடைமுகக் கட்டுப்பாடுகளை மொழிபெயர்க்கவும்" else "Choose the language for music discovery, playlists & dynamic content.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // English Button
                    Button(
                        onClick = { viewModel.setLanguage("en") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentLang == "en") CyberCyan else Color.White.copy(alpha = 0.08f),
                            contentColor = if (currentLang == "en") DeepDarkBackground else TextPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lang_en_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("English", fontWeight = FontWeight.Bold)
                    }

                    // Tamil Button
                    Button(
                        onClick = { viewModel.setLanguage("ta") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentLang == "ta") CyberCyan else Color.White.copy(alpha = 0.08f),
                            contentColor = if (currentLang == "ta") DeepDarkBackground else TextPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lang_ta_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("தமிழ் (Tamil)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(translate("Audio Quality", isTamil), fontWeight = FontWeight.Bold, color = CyberCyan)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(if (isTamil) "உயர்தர ஆடியோவை உருவகப்படுத்தவும்" else "Simulate Lossless 320kbps", fontWeight = FontWeight.SemiBold)
                        Text(if (isTamil) "சவுண்ட் கார்டுகளுக்கான கூடுதல் தரவு ஒதுக்கீடு" else "Higher data stream allocations for sound cards", fontSize = 11.sp, color = TextSecondary)
                    }
                    Switch(
                        checked = highQualityMode,
                        onCheckedChange = { highQualityMode = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberCyan)
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                Text(translate("Crossfade Gaps", isTamil), fontWeight = FontWeight.Bold, color = NeonPurple)
                Column {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(if (isTamil) "பாடல்களுக்கு இடையே மாற்றம்" else "Crossfade Duration")
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
                Text(translate("Membership Profile", isTamil), fontWeight = FontWeight.Bold, color = CyberMagenta)
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
                        Text(if (isTamil) "வெளியேறு" else "LOG OUT")
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
    modifier: Modifier = Modifier,
    isTamil: Boolean = false
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
            Text(text = translate("Creator Admin Panel", isTamil), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 12.dp))
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(translate("Add Song Stream to Catalog", isTamil), fontWeight = FontWeight.Bold, color = CyberCyan)

                    OutlinedTextField(value = sTitle, onValueChange = { sTitle = it }, label = { Text(if (isTamil) "பாடல் பெயர்" else "Song Title") }, modifier = Modifier.fillMaxWidth().testTag("admin_title"))
                    OutlinedTextField(value = sArtist, onValueChange = { sArtist = it }, label = { Text(if (isTamil) "கலைஞர் பெயர்" else "Artist Name") }, modifier = Modifier.fillMaxWidth().testTag("admin_artist"))
                    OutlinedTextField(value = sAlbum, onValueChange = { sAlbum = it }, label = { Text(if (isTamil) "ஆல்பம் பெயர்" else "Album Name") }, modifier = Modifier.fillMaxWidth().testTag("admin_album"))
                    OutlinedTextField(value = sGenre, onValueChange = { sGenre = it }, label = { Text(if (isTamil) "வகை (Genre)" else "Genre label") }, modifier = Modifier.fillMaxWidth().testTag("admin_genre"))
                    OutlinedTextField(value = sUrl, onValueChange = { sUrl = it }, label = { Text(if (isTamil) "எம்பி3 பிரவாக இணைப்பு (விருப்பத்தேர்வு)" else "Stream MP3 URL (Optional)") }, modifier = Modifier.fillMaxWidth().testTag("admin_mp3_url"))
                    OutlinedTextField(value = sCoverUrl, onValueChange = { sCoverUrl = it }, label = { Text(if (isTamil) "அட்டைப் படம் இணைப்பு" else "Cover Image URL (Optional)") }, modifier = Modifier.fillMaxWidth().testTag("admin_cover_url"))

                    Button(
                        onClick = {
                            if (sTitle.isNotBlank() && sArtist.isNotBlank()) {
                                viewModel.adminUploadSong(sTitle, sArtist, sAlbum, sGenre, sUrl, sCoverUrl)
                                Toast.makeText(context, if (isTamil) "ஸ்பார்க் தரவுத்தளத்தில் பதிவேற்றப்பட்டது!" else "Uploaded to local Spark Catalog!", Toast.LENGTH_SHORT).show()
                                sTitle = ""
                                sArtist = ""
                                sAlbum = ""
                                sGenre = ""
                                sUrl = ""
                                sCoverUrl = ""
                            } else {
                                Toast.makeText(context, if (isTamil) "பாடல் தலைப்பு மற்றும் கலைஞர் பெயரை உள்ளிடவும்" else "Fill title and artist first", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = DeepDarkBackground),
                        modifier = Modifier.fillMaxWidth().testTag("admin_upload_button")
                    ) {
                        Text(if (isTamil) "ஆவணத்தைச் சேமித்து உள்ளிடு" else "SAVE & COMPILE TO DATABASE", fontWeight = FontWeight.ExtraBold)
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
    imageUrl: String? = null,
    onClick: () -> Unit
) {
    val finalImageUrl = imageUrl ?: legendaryArtistsList.find { it.name.equals(artist, ignoreCase = true) || it.tamilName.equals(artist, ignoreCase = true) }?.imageUrl
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable { onClick() }
    ) {
        if (!finalImageUrl.isNullOrBlank()) {
            AsyncImage(
                model = finalImageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, Brush.linearGradient(listOf(CyberCyan, CyberMagenta)), CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(CyberCyan, NeonPurple))),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = DeepDarkBackground, modifier = Modifier.size(32.dp))
            }
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
    progress: Float?,
    isActive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) Color.White.copy(alpha = 0.05f) else Color.Transparent)
            .clickable { onPlay() }
            .padding(vertical = 6.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(50.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = song.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(6.dp))
            )
            if (isActive) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Playing",
                        tint = CyberCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                maxLines = 1,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (isActive) CyberCyan else TextPrimary,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                maxLines = 1,
                color = if (isActive) CyberCyan.copy(alpha = 0.7f) else TextSecondary,
                fontSize = 12.sp,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Action controls
        IconButton(onClick = onLike, modifier = Modifier.size(24.dp).testTag("like_${song.id}")) {
            Icon(
                imageVector = if (song.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = null,
                tint = if (song.isLiked) CyberMagenta else TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }

        IconButton(onClick = onDownload, modifier = Modifier.size(24.dp).testTag("download_${song.id}")) {
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

// --- TAMIL CORNER ADAPTIVE COMPOSABLES ---

@Composable
fun LocalizedTamilFestivalsBanner(isTamil: Boolean) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        cornerRadius = 20.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?auto=format&fit=crop&q=80&w=800",
                contentDescription = "Festival Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(CyberMagenta)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("FESTIVAL SPECIAL", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = if (isTamil) "பொங்கல் இசை திருவிழா 🌾" else "Pongal Harvest Music Festival 🌾",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isTamil) "மார்கழி இசை சீசன் மற்றும் கிராமியக் கொண்டாட்டங்கள்" else "Celebrating Margazhi Music Season & Traditional Village Folk Beats!",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun TamilMusicSection(
    viewModel: SparkViewModel,
    isTamil: Boolean,
    onPlaylistSelect: (String) -> Unit,
    onCategorySelect: (String) -> Unit
) {
    val categories = listOf(
        Triple("Kollywood Hits", "கோலிவுட் ஹிட்ஸ்", Color(0xFFE11D48)),
        Triple("Tamil Love Songs", "காதல் பாடல்கள்", Color(0xFFDB2777)),
        Triple("Melody Songs", "மெலடி பாடல்கள்", Color(0xFF2563EB)),
        Triple("Mass Songs", "மாஸ் பாடல்கள்", Color(0xFFD97706)),
        Triple("Devotional Songs", "பக்தி பாடல்கள்", Color(0xFF059669)),
        Triple("90s Tamil Classics", "90ஸ் கிளாசிக்ஸ்", Color(0xFF7C3AED)),
        Triple("Trending Tamil Songs", "ட்ரெண்டிங் தமிழ்", Color(0xFF0891B2)),
        Triple("Tamil Rap & Indie", "தமிழ் ராப் & இண்டி", Color(0xFF4B5563))
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = if (isTamil) "தமிழ் இசைச் சோலை 🌸" else "Tamil Music Corner 🌸",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = TextPrimary
                )
            }
            Text(
                text = if (isTamil) "அனைத்தும் காண்க" else "View All",
                color = CyberCyan,
                fontSize = 11.sp,
                modifier = Modifier.clickable {}
            )
        }

        // Chips Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { (engName, tamName, tagColor) ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(tagColor.copy(alpha = 0.15f))
                        .border(1.dp, tagColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable { onCategorySelect(engName) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (isTamil) tamName else engName,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun RegionalVibeSelector(isTamil: Boolean, onSelectVibe: (String) -> Unit) {
    var selectedRegion by remember { mutableStateOf("Madurai") }
    
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = if (isTamil) "தமிழ்நாடு பிராந்தியப் பரிந்துரைகள் 📍" else "Tamil Nadu Regional Recommendation 📍",
                fontWeight = FontWeight.Black,
                color = CyberCyan,
                fontSize = 14.sp
            )
            
            Text(
                text = if (isTamil) "உங்கள் மாவட்டத்தைத் தேர்வுசெய்து உள்ளூர் நாட்டுப்புற, பக்தி மற்றும் பாரம்பரிய இசையைக் கேளுங்கள்" 
                       else "Explore highly localized folk (கிராமிய பாடல்), devotional, and classical melodies based on your chosen Tamil Nadu cultural hubs.",
                fontSize = 11.sp,
                color = TextSecondary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Madurai", "Chennai", "Coimbatore", "Thanjavur").forEach { loc ->
                    val isSelected = selectedRegion == loc
                    val label = when(loc) {
                        "Madurai" -> if (isTamil) "மதுரை" else "Madurai"
                        "Chennai" -> if (isTamil) "சென்னை" else "Chennai"
                        "Coimbatore" -> if (isTamil) "கோவை" else "Coimbatore"
                        "Thanjavur" -> if (isTamil) "தஞ்சாவூர்" else "Thanjavur"
                        else -> loc
                    }
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) CyberCyan else Color.White.copy(alpha = 0.05f))
                            .clickable { selectedRegion = loc }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSelected) DeepDarkBackground else TextPrimary
                        )
                    }
                }
            }

            // Simulated recommendation block
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.03f))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(NeonPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    val (songName, artistName) = when(selectedRegion) {
                        "Madurai" -> Pair(
                            if (isTamil) "மதுரை வீரன் (மதுரை மண்ணின் கிராமியப் பாடல்)" else "Madurai Veeran (Madurai Folk)", 
                            if (isTamil) "வேல்முருகன் - கிராமியப் பாடல்கள்" else "Velmurugan Folk"
                        )
                        "Chennai" -> Pair("Arabic Kuthu (City Mass Beat)", "Anirudh Ravichander")
                        "Coimbatore" -> Pair(
                            if (isTamil) "போதை கணமே (கோயம்புத்தூர் கேஃபே மெலடி)" else "Bodhai Kaname (Coimbatore Melody)", 
                            "Anirudh Ravichander"
                        )
                        "Thanjavur" -> Pair(
                            if (isTamil) "கண்மணி அன்போடு (தஞ்சாவூர் பக்தி/பாரம்பரிய மார்கழி)" else "Kanmani Anbodu (90s Classic)", 
                            "Kamal Haasan - S. Janaki"
                        )
                        else -> Pair("Madurai Veeran", "Velmurugan")
                    }
                    Text(songName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(artistName, color = TextSecondary, fontSize = 10.sp)
                }
                Button(
                    onClick = { onSelectVibe(selectedRegion) },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberMagenta),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(if (isTamil) "கேள்" else "LISTEN", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

fun playTamilGenreOrSong(genre: String, viewModel: SparkViewModel, context: android.content.Context) {
    val songs = viewModel.allSongs.value
    val match = when {
        genre.contains("Mass") || genre.contains("Kollywood") || genre.contains("Chennai") -> songs.find { it.id == "7" }
        genre.contains("90s") || genre.contains("Classics") || genre.contains("Thanjavur") -> songs.find { it.id == "8" }
        genre.contains("Melody") || genre.contains("Coimbatore") -> songs.find { it.id == "9" }
        genre.contains("Indie") || genre.contains("Rap") -> songs.find { it.id == "10" }
        genre.contains("Trending") -> songs.find { it.id == "11" }
        genre.contains("Love") -> songs.find { it.id == "12" }
        genre.contains("Devotional") || genre.contains("Folk") || genre.contains("Madurai") -> songs.find { it.id == "13" }
        else -> songs.find { it.id == "7" }
    }
    if (match != null) {
        viewModel.playbackManager.playSong(match, songs)
        Toast.makeText(context, "Streaming: ${match.title}", Toast.LENGTH_SHORT).show()
    } else {
        val anyTamil = songs.find { it.id == "7" } ?: songs.firstOrNull()
        if (anyTamil != null) {
            viewModel.playbackManager.playSong(anyTamil, songs)
        }
    }
}
