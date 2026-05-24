package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

// --- Simple DTOs for Retrofit + Moshi ---
data class PartDto(val text: String? = null)
data class ContentDto(val parts: List<PartDto>)
data class ResponseFormatTextDto(val mimeType: String, val schema: Map<String, Any>? = null)
data class ResponseFormatDto(val text: ResponseFormatTextDto? = null)
data class GenerationConfigDto(val responseFormat: ResponseFormatDto? = null, val temperature: Float? = null)

data class GenerateContentRequestDto(
    val contents: List<ContentDto>,
    val systemInstruction: ContentDto? = null,
    val generationConfig: GenerationConfigDto? = null
)

data class CandidateDto(val content: ContentDto)
data class GenerateContentResponseDto(val candidates: List<CandidateDto>?)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequestDto
    ): GenerateContentResponseDto
}

object GeminiRetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

class GeminiManager {
    private val tag = "GeminiManager"

    suspend fun generateMoodPlaylist(mood: String, availableSongs: List<SongEntity>): List<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(tag, "Gemini API key is placeholder. Falling back to local search.")
            return@withContext availableSongs.filter { 
                it.genre.contains(mood, ignoreCase = true) || it.title.contains(mood, ignoreCase = true) 
            }.map { it.id }.ifEmpty { availableSongs.take(3).map { it.id } }
        }

        val songMetadataString = availableSongs.joinToString("\n") { "- ID: ${it.id}, Title: ${it.title}, Artist: ${it.artist}, Genre: ${it.genre}" }
        val prompt = """
            The user is feeling: "$mood".
            Here is the library of available tracks:
            $songMetadataString
            
            Return a JSON array of Song IDs from the library that BEST match the current state or mood of the user.
            Only return the raw JSON array string containing IDs from the provided list, for example: ["1", "4"]
        """.trimIndent()

        val request = GenerateContentRequestDto(
            contents = listOf(ContentDto(parts = listOf(PartDto(text = prompt)))),
            systemInstruction = ContentDto(parts = listOf(PartDto(text = "You are an expert AI Music Disc Jockey for Spark. You only output valid JSON arrays of strings matching provided song IDs."))),
            generationConfig = GenerationConfigDto(
                responseFormat = ResponseFormatDto(text = ResponseFormatTextDto(mimeType = "application/json")),
                temperature = 0.2f
            )
        )

        try {
            val response = GeminiRetrofitClient.service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (jsonText != null) {
                val moshi = Moshi.Builder().build()
                val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, String::class.java)
                val adapter = moshi.adapter<List<String>>(listType)
                val parsed = adapter.fromJson(jsonText) ?: emptyList()
                return@withContext parsed.filter { id -> availableSongs.any { it.id == id } }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error generating AI mood playlist: ${e.message}", e)
        }

        // Fallback
        return@withContext availableSongs.take(3).map { it.id }
    }

    suspend fun generateLyrics(songTitle: String, artist: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateLocalMockLyrics(songTitle)
        }

         val prompt = """
            Generate synced lyrics with timestamps for the song: "$songTitle" by $artist.
            Format each line exactly as: [mm:ss] Lyrics text
            For example:
            [00:00] (Instrumental Intro)
            [00:05] This is the first lyric
            [00:10] And here is the second
            Make it 6 to 10 lines of lyrics that fit the mood of the song. Do not return markdown, just raw timestamps and lines.
         """.trimIndent()

         val request = GenerateContentRequestDto(
             contents = listOf(ContentDto(parts = listOf(PartDto(text = prompt)))),
             systemInstruction = ContentDto(parts = listOf(PartDto(text = "You generate stylized audio synced lyrics formatted as '[mm:ss] text' line by line with no preambles."))),
             generationConfig = GenerationConfigDto(temperature = 0.5f)
         )

         try {
             val response = GeminiRetrofitClient.service.generateContent(apiKey, request)
             val lyricText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
             if (!lyricText.isNullOrBlank()) {
                 return@withContext lyricText.trim()
             }
         } catch (e: Exception) {
             Log.e(tag, "Error generating AI lyrics: ${e.message}", e)
         }

         return@withContext generateLocalMockLyrics(songTitle)
    }

    private fun generateLocalMockLyrics(title: String): String {
        return """
            [00:00] (Neon instrumental intro beats)
            [00:03] Feeling the pulse of the cyber street...
            [00:08] Riding the wave where the currents meet...
            [00:12] Sparking a flame in the deep blue night...
            [00:17] Under the screen-glow shining bright!
            [00:23] Let the rhythm flow through your veins...
            [00:29] Finding the peace in the summer rains...
            [00:35] (Dynamic synth drum solo)
            [00:41] Feeling alive in the Spark design...
            [00:46] Forever in loop and frozen in time...
        """.trimIndent()
    }
}
