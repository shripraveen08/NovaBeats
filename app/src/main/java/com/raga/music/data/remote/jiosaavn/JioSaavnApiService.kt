package com.raga.music.data.remote.jiosaavn

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

// JioSaavn API - Free Bollywood music streaming
// Based on: https://github.com/cyberboysumanjay/JioSaavnAPI
// No API key required - completely free
// Provides high-quality (320kbps) Bollywood songs

interface JioSaavnApiService {

    @GET("search/")
    suspend fun searchSongs(
        @Query("query") query: String,
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 1
    ): JioSaavnSearchResponse

    @GET("song/")
    suspend fun getSongDetails(
        @Query("id") songId: String,
        @Query("lyrics") includeLyrics: Boolean = false
    ): JioSaavnSongResponse

    @GET("album/")
    suspend fun getAlbumSongs(
        @Query("id") albumId: String,
        @Query("limit") limit: Int = 50
    ): JioSaavnAlbumResponse

    @GET("playlist/")
    suspend fun getPlaylistSongs(
        @Query("id") playlistId: String,
        @Query("limit") limit: Int = 50
    ): JioSaavnPlaylistResponse

    @GET("search/")
    suspend fun getTrendingSongs(
        @Query("query") query: String = "latest hindi",
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 1
    ): JioSaavnSearchResponse

    companion object {
        // Using public API endpoints from JioSaavnAPI project
        const val BASE_URL = "https://jiosaavnapi-bhuvaneshwaran1.vercel.app/"
        // Alternative endpoints if needed:
        // const val BASE_URL = "https://jiosaavn-api.vercel.app/"
        // const val BASE_URL = "https://jiosaavn-api-2.vercel.app/"
    }
}

// ─── Response Models ──────────────────────────────────────────────────────────

data class JioSaavnSearchResponse(
    @SerializedName("results") val results: List<JioSaavnSong> = emptyList(),
    @SerializedName("status") val status: String = "",
    @SerializedName("total") val total: Int = 0
)

data class JioSaavnSongResponse(
    @SerializedName("results") val song: JioSaavnSong? = null,
    @SerializedName("status") val status: String = ""
)

data class JioSaavnAlbumResponse(
    @SerializedName("results") val songs: List<JioSaavnSong> = emptyList(),
    @SerializedName("name") val albumName: String = "",
    @SerializedName("image") val albumImage: String = "",
    @SerializedName("status") val status: String = ""
)

data class JioSaavnPlaylistResponse(
    @SerializedName("results") val songs: List<JioSaavnSong> = emptyList(),
    @SerializedName("name") val playlistName: String = "",
    @SerializedName("image") val playlistImage: String = "",
    @SerializedName("status") val status: String = ""
)

data class JioSaavnSong(
    @SerializedName("id") val id: String = "",
    @SerializedName("songid") val songId: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("singers") val singers: String = "",
    @SerializedName("album") val album: String = "",
    @SerializedName("album_url") val albumUrl: String = "",
    @SerializedName("duration") val duration: String = "",
    @SerializedName("image_url") val imageUrl: String = "",
    @SerializedName("url") val streamUrl: String = "",
    @SerializedName("download_url") val downloadUrl: String = "",
    @SerializedName("language") val language: String = "",
    @SerializedName("year") val year: String = "",
    @SerializedName("label") val label: String = "",
    @SerializedName("lyrics") val lyrics: String = "",
    @SerializedName("e_songid") val encryptedSongId: String = "",
    @SerializedName("perma_url") val permaUrl: String = "",
    @SerializedName("tiny_url") val tinyUrl: String = "",
    @SerializedName("twitter_url") val twitterUrl: String = "",
    @SerializedName("label_url") val labelUrl: String = "",
    @SerializedName("map") val artistMap: String = "",
    @SerializedName("has_rbt") val hasRbt: String = "",
    @SerializedName("autoplay") val autoplay: String = "",
    @SerializedName("starred") val starred: String = "",
    @SerializedName("liked") val liked: String = "",
    @SerializedName("origin") val origin: String = "",
    @SerializedName("origin_val") val originVal: String = "",
    @SerializedName("page") val page: Int = 0,
    @SerializedName("pass_album_ctx") val passAlbumCtx: String = "",
    @SerializedName("publish_to_fb") val publishToFb: Boolean = false,
    @SerializedName("starring") val starring: String = "",
    @SerializedName("music") val music: String = "",
    @SerializedName("streaming_source") val streamingSource: String? = null
)

// ─── Mapper ───────────────────────────────────────────────────────────────────

fun JioSaavnSong.toSongEntity() =
    com.raga.music.data.local.entities.SongEntity(
        id = "jiosaavn_$songId",
        title = title,
        artist = singers,
        album = album,
        albumArtUrl = imageUrl,
        duration = try {
            // Convert duration string (seconds) to milliseconds
            duration.toLong() * 1000L
        } catch (e: Exception) {
            0L
        },
        source = "jiosaavn",
        streamUrl = streamUrl
    )
