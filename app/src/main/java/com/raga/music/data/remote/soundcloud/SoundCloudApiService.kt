package com.raga.music.data.remote.soundcloud

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path

// SoundCloud API - Free music streaming
// Based on public SoundCloud API endpoints
// Requires client_id for some endpoints, but search works without authentication

interface SoundCloudApiService {

    @GET("search/tracks")
    suspend fun searchTracks(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20,
        @Query("client_id") clientId: String = CLIENT_ID
    ): SoundCloudSearchResponse

    @GET("tracks/{track_id}")
    suspend fun getTrackDetails(
        @Path("track_id") trackId: String,
        @Query("client_id") clientId: String = CLIENT_ID
    ): SoundCloudTrack

    @GET("search/tracks")
    suspend fun getTrendingTracks(
        @Query("q") query: String = "bollywood",
        @Query("limit") limit: Int = 20,
        @Query("client_id") clientId: String = CLIENT_ID
    ): SoundCloudSearchResponse

    companion object {
        // Using public SoundCloud API
        const val BASE_URL = "https://api.soundcloud.com/"
        
        // Public client ID (for development use)
        // In production, you should register your own app
        const val CLIENT_ID = "LBCcHmRB8VSOkCsGAZsWO0I2j06HX1aD"
        
        // Alternative base URLs if needed:
        // const val BASE_URL = "https://api-v2.soundcloud.com/"
    }
}

// ─── Response Models ──────────────────────────────────────────────────────────

data class SoundCloudSearchResponse(
    @SerializedName("collection") val tracks: List<SoundCloudTrack> = emptyList(),
    @SerializedName("next_href") val nextHref: String? = null,
    @SerializedName("query_ordinal") val queryOrdinal: Int? = null
)

data class SoundCloudTrack(
    @SerializedName("id") val id: Long = 0L,
    @SerializedName("title") val title: String = "",
    @SerializedName("description") val description: String? = null,
    @SerializedName("duration") val duration: Long = 0L, // milliseconds
    @SerializedName("genre") val genre: String? = null,
    @SerializedName("tag_list") val tagList: String? = null,
    @SerializedName("streamable") val streamable: Boolean = false,
    @SerializedName("downloadable") val downloadable: Boolean = false,
    @SerializedName("stream_url") val streamUrl: String = "",
    @SerializedName("download_url") val downloadUrl: String? = null,
    @SerializedName("permalink_url") val permalinkUrl: String = "",
    @SerializedName("artwork_url") val artworkUrl: String? = null,
    @SerializedName("waveform_url") val waveformUrl: String? = null,
    @SerializedName("user") val user: SoundCloudUser? = null,
    @SerializedName("playback_count") val playbackCount: Int? = null,
    @SerializedName("favoritings_count") val favoritingsCount: Int? = null,
    @SerializedName("comment_count") val commentCount: Int? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("license") val license: String? = null,
    @SerializedName("uri") val uri: String = ""
)

data class SoundCloudUser(
    @SerializedName("id") val id: Long = 0L,
    @SerializedName("username") val username: String = "",
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null
)

// ─── Mapper ───────────────────────────────────────────────────────────────────

fun SoundCloudTrack.toSongEntity() =
    com.raga.music.data.local.entities.SongEntity(
        id = "soundcloud_$id",
        title = title,
        artist = user?.username ?: "Unknown Artist",
        album = genre ?: "SoundCloud",
        albumArtUrl = artworkUrl?.replace("large", "t500x500") ?: "",
        duration = duration,
        source = "soundcloud",
        streamUrl = "$streamUrl?client_id=${SoundCloudApiService.CLIENT_ID}"
    )
