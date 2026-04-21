package com.novabeats.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// ─── Song ────────────────────────────────────────────────────────────────────
@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String = "",
    val albumArtUrl: String = "",
    val duration: Long = 0L,          // milliseconds
    val source: String,               // "youtube", "jamendo", "archive", "local"
    val streamUrl: String = "",
    val localPath: String = "",       // for downloaded / local files
    val isDownloaded: Boolean = false,
    val isLiked: Boolean = false,
    val playCount: Int = 0,
    val addedAt: Long = System.currentTimeMillis()
)

// ─── Playlist ─────────────────────────────────────────────────────────────────
@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String = "",
    val thumbnailUrl: String = "",
    val source: String = "local",     // "local", "youtube"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// ─── Playlist-Song cross-ref ──────────────────────────────────────────────────
@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId"]
)
data class PlaylistSongEntity(
    val playlistId: String,
    val songId: String,
    val position: Int = 0,
    val addedAt: Long = System.currentTimeMillis()
)

// ─── Recently Played ──────────────────────────────────────────────────────────
@Entity(tableName = "recently_played")
data class RecentlyPlayedEntity(
    @PrimaryKey val songId: String,
    val playedAt: Long = System.currentTimeMillis()
)

// ─── Download Queue ───────────────────────────────────────────────────────────
@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val songId: String,
    val title: String,
    val artist: String,
    val thumbnailUrl: String,
    val streamUrl: String,
    val status: String = "pending",   // pending, downloading, done, failed
    val progress: Int = 0,
    val localPath: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
