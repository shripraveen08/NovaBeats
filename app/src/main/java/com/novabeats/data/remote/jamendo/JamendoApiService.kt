package com.novabeats.data.remote.jamendo

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

// Jamendo is 100% free — Creative Commons music. No copyright issues.
// API Docs: https://developer.jamendo.com/v3.0
// Free tier: 5000 calls/day, no key restrictions for non-commercial apps

interface JamendoApiService {

    @GET("tracks/")
    suspend fun searchTracks(
        @Query("client_id") clientId: String = JAMENDO_CLIENT_ID,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("search") query: String,
        @Query("include") include: String = "musicinfo",
        @Query("audioformat") audioFormat: String = "mp32"
    ): JamendoTracksResponse

    @GET("tracks/")
    suspend fun getFeaturedTracks(
        @Query("client_id") clientId: String = JAMENDO_CLIENT_ID,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 30,
        @Query("order") order: String = "popularity_week",
        @Query("include") include: String = "musicinfo",
        @Query("audioformat") audioFormat: String = "mp32"
    ): JamendoTracksResponse

    @GET("tracks/")
    suspend fun getTracksByMood(
        @Query("client_id") clientId: String = JAMENDO_CLIENT_ID,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 20,
        @Query("tags") tags: String,       // e.g. "chill", "focus", "workout"
        @Query("include") include: String = "musicinfo",
        @Query("audioformat") audioFormat: String = "mp32"
    ): JamendoTracksResponse

    @GET("playlists/tracks/")
    suspend fun getPlaylistTracks(
        @Query("client_id") clientId: String = JAMENDO_CLIENT_ID,
        @Query("format") format: String = "json",
        @Query("id") playlistId: String,
        @Query("audioformat") audioFormat: String = "mp32"
    ): JamendoPlaylistResponse

    @GET("artists/tracks/")
    suspend fun getArtistTracks(
        @Query("client_id") clientId: String = JAMENDO_CLIENT_ID,
        @Query("format") format: String = "json",
        @Query("id") artistId: String,
        @Query("limit") limit: Int = 20,
        @Query("audioformat") audioFormat: String = "mp32"
    ): JamendoTracksResponse

    companion object {
        const val BASE_URL = "https://api.jamendo.com/v3.0/"
        // Register free at: https://devportal.jamendo.com
        const val JAMENDO_CLIENT_ID = "YOUR_JAMENDO_CLIENT_ID"
    }
}

// ─── Response Models ──────────────────────────────────────────────────────────

data class JamendoTracksResponse(
    @SerializedName("results") val results: List<JamendoTrack> = emptyList(),
    @SerializedName("headers") val headers: JamendoHeaders? = null
)

data class JamendoPlaylistResponse(
    @SerializedName("results") val results: List<JamendoPlaylist> = emptyList()
)

data class JamendoHeaders(
    @SerializedName("status") val status: String = "",
    @SerializedName("code") val code: Int = 0,
    @SerializedName("results_count") val resultsCount: Int = 0
)

data class JamendoTrack(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("duration") val duration: Int = 0,   // seconds
    @SerializedName("artist_name") val artistName: String = "",
    @SerializedName("album_name") val albumName: String = "",
    @SerializedName("album_image") val albumImage: String = "",
    @SerializedName("audio") val audio: String = "",     // direct MP3 stream URL
    @SerializedName("audiodownload") val audioDownload: String = "",
    @SerializedName("license_ccurl") val licenseUrl: String = "",
    @SerializedName("image") val image: String = ""
)

data class JamendoPlaylist(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("tracks") val tracks: List<JamendoTrack> = emptyList()
)

// ─── Mapper ───────────────────────────────────────────────────────────────────
fun JamendoTrack.toSongEntity() =
    com.novabeats.data.local.entities.SongEntity(
        id = "jamendo_$id",
        title = name,
        artist = artistName,
        album = albumName,
        albumArtUrl = albumImage.ifEmpty { image },
        duration = duration * 1000L,
        source = "jamendo",
        streamUrl = audio
    )
