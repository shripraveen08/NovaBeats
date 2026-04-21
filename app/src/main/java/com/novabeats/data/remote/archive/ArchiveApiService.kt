package com.novabeats.data.remote.archive

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// Internet Archive — 100% free, public domain music.
// No API key required. No copyright issues.
// Docs: https://archive.org/developers/internetarchive/api.html

interface ArchiveApiService {

    @GET("advancedsearch.php")
    suspend fun searchMusic(
        @Query("q") query: String,
        @Query("fl[]") fields: List<String> = listOf("identifier","title","creator","year","description"),
        @Query("rows") rows: Int = 20,
        @Query("page") page: Int = 1,
        @Query("output") output: String = "json",
        @Query("mediatype") mediatype: String = "audio"
    ): ArchiveSearchResponse

    @GET("metadata/{identifier}")
    suspend fun getItemMetadata(
        @Path("identifier") identifier: String
    ): ArchiveItemResponse

    companion object {
        const val BASE_URL = "https://archive.org/"
        const val STREAM_BASE = "https://archive.org/download/"
    }
}

// ─── Response Models ──────────────────────────────────────────────────────────

data class ArchiveSearchResponse(
    @SerializedName("response") val response: ArchiveResponseBody? = null
)

data class ArchiveResponseBody(
    @SerializedName("docs") val docs: List<ArchiveDoc> = emptyList(),
    @SerializedName("numFound") val numFound: Int = 0
)

data class ArchiveDoc(
    @SerializedName("identifier") val identifier: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("creator") val creator: String = "",
    @SerializedName("year") val year: String = "",
    @SerializedName("description") val description: String = ""
)

data class ArchiveItemResponse(
    @SerializedName("metadata") val metadata: ArchiveMetadata? = null,
    @SerializedName("files") val files: List<ArchiveFile> = emptyList(),
    @SerializedName("server") val server: String = "",
    @SerializedName("dir") val dir: String = ""
)

data class ArchiveMetadata(
    @SerializedName("identifier") val identifier: List<String> = emptyList(),
    @SerializedName("title") val title: List<String> = emptyList(),
    @SerializedName("creator") val creator: List<String> = emptyList(),
    @SerializedName("album") val album: List<String> = emptyList(),
    @SerializedName("year") val year: List<String> = emptyList()
)

data class ArchiveFile(
    @SerializedName("name") val name: String = "",
    @SerializedName("format") val format: String = "",
    @SerializedName("length") val length: String = "",  // seconds as string
    @SerializedName("title") val title: String = "",
    @SerializedName("artist") val artist: String = "",
    @SerializedName("album") val album: String = ""
)

// ─── Helper ───────────────────────────────────────────────────────────────────
fun ArchiveFile.isAudio(): Boolean =
    format in listOf("VBR MP3", "128Kbps MP3", "64Kbps MP3", "MP3", "OGG Vorbis", "Flac")

fun ArchiveFile.streamUrl(identifier: String): String =
    "${ArchiveApiService.STREAM_BASE}$identifier/$name"

fun ArchiveItemResponse.toSongEntities(): List<com.novabeats.data.local.entities.SongEntity> {
    val id = metadata?.identifier?.firstOrNull() ?: return emptyList()
    val artist = metadata.creator.firstOrNull() ?: "Unknown Artist"
    val album = metadata.album.firstOrNull() ?: ""
    val thumbUrl = "https://archive.org/services/img/$id"

    return files
        .filter { it.isAudio() && it.name.endsWith(".mp3", ignoreCase = true) }
        .mapIndexed { index, file ->
            com.novabeats.data.local.entities.SongEntity(
                id = "archive_${id}_$index",
                title = file.title.ifEmpty { file.name.substringBeforeLast(".") },
                artist = file.artist.ifEmpty { artist },
                album = file.album.ifEmpty { album },
                albumArtUrl = thumbUrl,
                duration = (file.length.toDoubleOrNull() ?: 0.0).toLong() * 1000L,
                source = "archive",
                streamUrl = file.streamUrl(id)
            )
        }
}
