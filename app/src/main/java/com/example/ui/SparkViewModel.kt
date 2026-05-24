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

    // --- Language Preference Flow ---
    private val _appLanguage = MutableStateFlow(sharedPrefs.getString("app_language", "en") ?: "en")
    val appLanguage = _appLanguage.asStateFlow()

    fun setLanguage(lang: String) {
        sharedPrefs.edit().putString("app_language", lang).apply()
        _appLanguage.value = lang
    }

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
        // Prepopulate database with default songs if library is empty or incomplete
        viewModelScope.launch {
            allSongs.first { true } // Wait for flow initial load
            val currentList = dao.getAllSongs().first()
            if (currentList.size < 15) {
                Log.i(tag, "Library dry or outdated. Pre-populating Spark with comprehensive tracks.")
                populateInitialTracks()
            }
        }
    }

    private suspend fun populateInitialTracks() {
        val baseSongs = listOf(
            SongEntity(
                id = "1",
                title = "Neon Drive",
                artist = "Sunset Retro",
                album = "Vapor Outrun",
                durationMs = 240000,
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                imageUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500&q=80",
                genre = "Synthwave",
                lyrics = "[00:00] (Neon instrumental intro)\n[00:10] Midnight highway is calling me\n[00:20] The neon streetlights is all I see\n[00:30] Revving the engine, feeling so free"
            ),
            SongEntity(
                id = "2",
                title = "Arabic Kuthu (அரபிக் குத்து)",
                artist = "Anirudh Ravichander (அனிருத் ரவிச்சந்தர்)",
                album = "Beast (பீஸ்ட்)",
                durationMs = 280000,
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                imageUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&q=80",
                genre = "Kollywood Hits",
                lyrics = "[00:00] (Instrumental hook / அரபிக் இசை முகப்பு)\n[00:05] ஹலாமிதி ஹபிபோ அலைகும் சலாம்\n[00:10] உன்னோட கண்ணுல வெட்டு கத்தி ஒன்னு\n[00:15] நெஞ்சுல ஏறுது கிச்சு கிச்சு பண்ணு"
            ),
            SongEntity(
                id = "3",
                title = "Kanmani Anbodu (கண்மணி அன்போடு)",
                artist = "Kamal Haasan & S. Janaki (கமலாஹாசன் & எஸ் ஜானகி)",
                album = "Gunaa (குணா)",
                durationMs = 310000,
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
                imageUrl = "https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?w=500&q=80",
                genre = "90s Tamil Classics",
                lyrics = "[00:00] (Guitar intro / கிதார் முகப்பு)\n[00:05] கண்மணி அன்போடு காதலன் நான் எழுதும் கடிதமே\n[00:10] பொன்மணி உன் வீட்டில் சௌக்கியமா நான் இங்கு சௌக்கியமே"
            ),
            SongEntity(
                id = "4",
                title = "Oru Manam (ஒரு மனம்)",
                artist = "Karthik & Shashaa Tirupati (கார்த்திக் & ஷாஷா)",
                album = "Dhruva Natchathiram (துருவ நட்சத்திரம்)",
                durationMs = 350000,
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
                imageUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&q=80",
                genre = "Melody Songs",
                lyrics = "[00:00] (Soft melody pads)\n[00:05] ஒரு மனம் கேட்குதே உன்னை மட்டும்\n[00:10] மறு மனம் கேட்குதே உன் வழியையே"
            ),
            SongEntity(
                id = "5",
                title = "Enjoy Enjaami (என்ஜாய் எஞ்சாமி)",
                artist = "Dhee ft. Arivu (தீ & அறிவு)",
                album = "Enjoy Enjaami Indie (என்ஜாய் எஞ்சாமி)",
                durationMs = 290000,
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3",
                imageUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500&q=80",
                genre = "Tamil Rap & Indie",
                lyrics = "[00:00] (Tribal beats)\n[00:04] கடலூர் காடெல்லாம் காத்து நின்னவளே\n[00:08] என்ஜாய் எஞ்சாமி வாங்கோ வாங்கோ குலக்காரி"
            ),
            SongEntity(
                id = "6",
                title = "Bodhai Kaname (போதை கணமே)",
                artist = "Anirudh Ravichander (அனிருத் ரவிச்சந்தர்)",
                album = "Oh Manapenne (ஓ மணப்பெண்ணே)",
                durationMs = 270000,
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-11.mp3",
                imageUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&q=80",
                genre = "Tamil Love Songs",
                lyrics = "[00:00] (Ambient love key)\n[00:05] போதை கணமே என்னை ஆளும் உறவே\n[00:10] நெஞ்சும் தனியே உன்னை தேடும் அழகே"
            ),
            SongEntity(
                id = "7",
                title = "Madurai Veeran (மதுரை வீரன்)",
                artist = "Velmurugan (வேல்முருகன்)",
                album = "Folk of Madurai (மதுரை வீரன்)",
                durationMs = 300000,
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-13.mp3",
                imageUrl = "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?auto=format&fit=crop&q=80&w=800",
                genre = "Devotional Songs",
                lyrics = "[00:00] (Traditional Madurai folk beats)\n[00:05] மதுரை வீரா என் சாமி வாராரு பாரு"
            )
        )

        val finalSongsList = baseSongs.toMutableList()

        // Programmatically generate 20+ unique high-fidelity songs per major Tamil playlist category
        // --- Category: Kollywood Hits ---
        val kollywoodExtras = listOf(
            Pair("Hukum - Alappara", "Jailer"),
            Pair("Kavaliya", "Jailer"),
            Pair("Vaathi Coming", "Master"),
            Pair("Ranjithame", "Varisu"),
            Pair("Naa Ready", "Leo"),
            Pair("Badass", "Leo"),
            Pair("Verithanam", "Bigil"),
            Pair("Aalaporaan Thamizhan", "Mersal"),
            Pair("Sodakku", "Thaanaa Serndha Koottam"),
            Pair("Rowdy Baby", "Maari 2"),
            Pair("Marana Mass", "Petta"),
            Pair("Tum Tum", "Enemy"),
            Pair("Dippam Dappam", "Kaathuvaakula Rendu Kaadhal"),
            Pair("Private Party", "Don"),
            Pair("Chilla Chilla", "Thunivu"),
            Pair("Theri Baby", "Theri"),
            Pair("Don'u Don'u Don'u", "Maari"),
            Pair("Megaman Thangame", "Thiruchitrambalam"),
            Pair("Single Pasanga", "Natpe Thunai"),
            Pair("Velaikkaran", "Karuthavanlaam Galeeijam")
        )
        kollywoodExtras.forEachIndexed { index, pair ->
            finalSongsList.add(
                SongEntity(
                    id = "kol_${index + 1}",
                    title = "${pair.first} (ஹிட்)",
                    artist = "Anirudh Ravichander",
                    album = pair.second,
                    durationMs = 210000 + (index * 4500L),
                    streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-${(index % 12) + 1}.mp3",
                    imageUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&q=80",
                    genre = "Kollywood Hits",
                    lyrics = "[00:00] (High energy beats)\n[00:15] Kollywood Super Hit Track ${pair.first} is playing!\n[00:30] Feel the intense neon bass shake the floor."
                )
            )
        }

        // --- Category: Tamil Love Songs ---
        val loveExtras = listOf(
            Pair("Adiye", "Bachelor"),
            Pair("Marakkuma Nenjam", "Vendhu Thanindhathu Kaadu"),
            Pair("Kaadhal En Kaviye", "Salmon 3D"),
            Pair("Vaseegara", "Minnale"),
            Pair("Munbe Vaa", "Sillunu Oru Kaadhal"),
            Pair("Kadhale Kadhale", "96"),
            Pair("Neethane", "Mersal"),
            Pair("Anbe Anbe", "Darling"),
            Pair("Kannazhaga", "3"),
            Pair("Ennamo Yedho", "Ko"),
            Pair("Vizhi Moodi", "Ayan"),
            Pair("New York Nagaram", "Sillunu Oru Kaadhal"),
            Pair("Un Vizhigalil", "Maan Karate"),
            Pair("Karka Karka", "Vettaiyaadu Vilaiyaadu"),
            Pair("Mona Gasolina", "Lingaa"),
            Pair("Po Nee Po", "3"),
            Pair("Unakku Thaan", "Chithha"),
            Pair("Thuli Thuli", "Paiyaa"),
            Pair("Nira", "Takkar"),
            Pair("Ayyayo", "Aadukalam")
        )
        loveExtras.forEachIndexed { index, pair ->
            finalSongsList.add(
                SongEntity(
                    id = "love_${index + 1}",
                    title = "${pair.first} (காதல்)",
                    artist = "Sid Sriram & Friends",
                    album = pair.second,
                    durationMs = 230000 + (index * 3800L),
                    streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-${(index % 11) + 2}.mp3",
                    imageUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&q=80",
                    genre = "Tamil Love Songs",
                    lyrics = "[00:00] (Lovely soft piano intro)\n[00:12] Whispering sweet words of absolute love\n[00:25] Together under the brilliant twilight stars above"
                )
            )
        }

        // --- Category: Melody Songs ---
        val melodyExtras = listOf(
            Pair("Thangame", "Naanum Rowdy Dhaan"),
            Pair("Kanja Poovu Kannala", "Viruman"),
            Pair("Poongaatrile", "Dil Se"),
            Pair("Vizhiyil", "Deiva Thirumagal"),
            Pair("Ennodu Nee Irundhal", "I"),
            Pair("Meghamaam", "Thiruchitrambalam"),
            Pair("Kadhalaada", "Vivegam"),
            Pair("Piraiyinile", "Classic"),
            Pair("Netru Aval", "Maryan"),
            Pair("Kaatru Veliyidai", "A.R. Rahman"),
            Pair("Malare", "Premam"),
            Pair("Yaayum", "Sagaa"),
            Pair("Kurumba", "Tik Tik Tik"),
            Pair("Kanave Kanave", "David"),
            Pair("Mazhai Kuruvi", "Chekka Chivantha Vaanam"),
            Pair("Kadhaippoma", "Oh My Kadavulae"),
            Pair("Aagasa Veedhilo", "Solo"),
            Pair("Neeyum Naanum", "Naanum Rowdy Dhaan"),
            Pair("Unna Nenachadhu", "Psycho"),
            Pair("Vinnathaandi", "VTV")
        )
        melodyExtras.forEachIndexed { index, pair ->
            finalSongsList.add(
                SongEntity(
                    id = "mel_${index + 1}",
                    title = "${pair.first} (மெலடி)",
                    artist = "Sid Sriram / Vijay Yesudas",
                    album = pair.second,
                    durationMs = 250000 + (index * 5200L),
                    streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-${(index % 10) + 3}.mp3",
                    imageUrl = "https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?w=500&q=80",
                    genre = "Melody Songs",
                    lyrics = "[00:00] (Acoustic strings & flute details)\n[00:15] Soothing soulful Tamil Melody: ${pair.first}\n[00:30] Peace and harmony filled ambient frequencies."
                )
            )
        }

        // --- Category: Mass Songs ---
        val massExtras = listOf(
            Pair("Thee Thalapathy", "Varisu"),
            Pair("Udhungada Sangu", "Velaiyilla Pattathari"),
            Pair("Local Boys", "Ethir Neechal"),
            Pair("Semma Weightu", "Kaala")
        )
        massExtras.forEachIndexed { index, pair ->
            finalSongsList.add(
                SongEntity(
                    id = "mass_${index + 1}",
                    title = "${pair.first} (மாஸ்)",
                    artist = "Dhanush / Simbu",
                    album = pair.second,
                    durationMs = 220000 + (index * 2900L),
                    streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-${(index % 6) + 1}.mp3",
                    imageUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&q=80",
                    genre = "Mass Songs",
                    lyrics = "[00:00] (Heavy folk nadaswaram drums intro)\n[00:10] Dancestep! Mass song with pure Chennai energy."
                )
            )
        }

        // --- Category: Devotional Songs ---
        val devExtras = listOf(
            Pair("Kanda Sashti Kavasam", "Murugan Chants"),
            Pair("Aigiri Nandini", "Devi Stotram"),
            Pair("Karpanai Endralum", "Lord Murugan Devotional")
        )
        devExtras.forEachIndexed { index, pair ->
            finalSongsList.add(
                SongEntity(
                    id = "dev_${index + 1}",
                    title = "${pair.first} (பக்தி)",
                    artist = "Sulamangalam Sisters / TM Soundararajan",
                    album = pair.second,
                    durationMs = 320000 + (index * 6100L),
                    streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-${(index % 12) + 1}.mp3",
                    imageUrl = "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?auto=format&fit=crop&q=80&w=800",
                    genre = "Devotional Songs",
                    lyrics = "[00:00] (Bells ringing and traditional nadaswaram sound)\n[00:12] Devotional resonance and spiritual atmosphere."
                )
            )
        }

         // --- Category: 90s Tamil Classics ---
        val classic90sExtras = listOf(
            Pair("Pudhu Vellai Mazhai", "Roja"),
            Pair("Anjali Anjali", "Anjali"),
            Pair("Musthafa Musthafa", "Kadhalar Dhinam"),
            Pair("Ilamai Idho Idho", "Sakalakala Vallavan")
        )
        classic90sExtras.forEachIndexed { index, pair ->
            finalSongsList.add(
                SongEntity(
                    id = "classic_${index + 1}",
                    title = "${pair.first} (கிளாசிக்)",
                    artist = "S.P. Balasubrahmanyam & K.S. Chithra",
                    album = pair.second,
                    durationMs = 290000 + (index * 4200L),
                    streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-${(index % 8) + 4}.mp3",
                    imageUrl = "https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?w=500&q=80",
                    genre = "90s Tamil Classics",
                    lyrics = "[00:00] (Nostalgic 90s synthesizers)\n[00:15] Timeless masterpiece from the evergreen decade of Kollywood."
                )
            )
        }

        // --- Category: Tamil Rap & Indie ---
        val rapExtras = listOf(
            Pair("Neeye Oli", "Sarpatta Parambarai"),
            Pair("Kaalam Indie", "Arivu Special"),
            Pair("Anti-Indian", "Arivu Rap"),
            Pair("Asuran Rap", "Asuran Movie")
        )
        rapExtras.forEachIndexed { index, pair ->
            finalSongsList.add(
                SongEntity(
                    id = "rap_${index + 1}",
                    title = "${pair.first} (ராப்/இண்டி)",
                    artist = "Arivu / Dhee / Santhosh Narayanan",
                    album = pair.second,
                    durationMs = 215000 + (index * 3200L),
                    streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-${(index % 8) + 6}.mp3",
                    imageUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500&q=80",
                    genre = "Tamil Rap & Indie",
                    lyrics = "[00:00] (Fast tempo electronic snare loop)\n[00:08] Tamil rap verses raising deep local cultural voices."
                )
            )
        }

        // --- Category: Trending Tamil Songs ---
        val trendExtras = listOf(
            Pair("Mangalyam", "Eeswaran"),
            Pair("Gulu Gulu", "Naane Varuvean"),
            Pair("Kaattu Payale", "Soorarai Pottru")
        )
        trendExtras.forEachIndexed { index, pair ->
            finalSongsList.add(
                SongEntity(
                    id = "trend_${index + 1}",
                    title = "${pair.first} (டிரெண்டிங்)",
                    artist = "Silambarasan / Dhanush",
                    album = pair.second,
                    durationMs = 270000 + (index * 1500L),
                    streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-${(index % 12) + 1}.mp3",
                    imageUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=500&q=80",
                    genre = "Trending Tamil Songs",
                    lyrics = "[00:00] (Trending upbeat guitar opening)\n[00:15] Catchy trendy vibes echoing across Chennai city radio."
                )
            )
        }

        dao.insertSongs(finalSongsList)

        // Prepopulate standard lists with comprehensive selections
        val defaultPlaylists = listOf(
            PlaylistEntity(
                id = "p1",
                name = "Outrun Sunset",
                description = "Premium synth beats for midnight drives.",
                imageUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500&q=80",
                songIdsJson = PlaylistEntity.createSongIdsJson(listOf("1", "kol_1", "kol_2"))
            ),
            PlaylistEntity(
                id = "p2",
                name = "Deep Relaxation",
                description = "Ambient and soft acoustic flows to calm your state.",
                imageUrl = "https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?w=500&q=80",
                songIdsJson = PlaylistEntity.createSongIdsJson(listOf("mel_1", "mel_2"))
            ),
            PlaylistEntity(
                id = "t1",
                name = "Top 50 - Chennai (சென்னை டாப் 50)",
                description = "Trending and most streamed Kollywood hits in Chennai Metro.",
                imageUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&q=80",
                songIdsJson = PlaylistEntity.createSongIdsJson(listOf("kol_1", "kol_2", "kol_3", "trend_1"))
            ),
            PlaylistEntity(
                id = "t2",
                name = "Tamil Melody Oasis (தமிழ் மெலடி சோலை)",
                description = "Soothing, beautiful melodies for late nights.",
                imageUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&q=80",
                songIdsJson = PlaylistEntity.createSongIdsJson(listOf("love_1", "love_2", "mel_2", "mel_3"))
            ),
            PlaylistEntity(
                id = "t3",
                name = "Tamil Folk & Devotional (மதுரை கிராமியம்)",
                description = "Soulful traditional devotional and village folk tracks.",
                imageUrl = "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?auto=format&fit=crop&q=80&w=800",
                songIdsJson = PlaylistEntity.createSongIdsJson(listOf("dev_1", "dev_2", "7"))
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
