package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val streamUrl: String,
    val imageUrl: String,
    val genre: String,
    val lyrics: String, // Stringified sync lyrics or full text
    val isLiked: Boolean = false,
    val isDownloaded: Boolean = false,
    val localPath: String? = null,
    val playCount: Int = 0,
    val isCustomUploaded: Boolean = false // Admin added
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val imageUrl: String? = null,
    val songIdsJson: String = "[]" // Serialized list of song IDs
) {
    fun getSongIds(): List<String> {
        return try {
            val moshi = Moshi.Builder().build()
            val listType = Types.newParameterizedType(List::class.java, String::class.java)
            val adapter = moshi.adapter<List<String>>(listType)
            adapter.fromJson(songIdsJson) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        fun createSongIdsJson(ids: List<String>): String {
            val moshi = Moshi.Builder().build()
            val listType = Types.newParameterizedType(List::class.java, String::class.java)
            val adapter = moshi.adapter<List<String>>(listType)
            return adapter.toJson(ids)
        }
    }
}

@Entity(tableName = "listening_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val songId: String,
    val timestamp: Long = System.currentTimeMillis()
)
