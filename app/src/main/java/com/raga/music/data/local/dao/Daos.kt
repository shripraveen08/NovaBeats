package com.raga.music.data.local.dao

import androidx.room.*
import com.raga.music.data.local.entities.*
import kotlinx.coroutines.flow.Flow

// ─── Song DAO ─────────────────────────────────────────────────────────────────
@Dao
interface SongDao {

    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getSongById(id: String): SongEntity?

    @Query("SELECT * FROM songs WHERE isLiked = 1 ORDER BY title ASC")
    fun getLikedSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE isDownloaded = 1 ORDER BY title ASC")
    fun getDownloadedSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE source = :source ORDER BY title ASC")
    fun getSongsBySource(source: String): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%'")
    fun searchSongs(query: String): Flow<List<SongEntity>>

    @Upsert
    suspend fun upsertSong(song: SongEntity)

    @Upsert
    suspend fun upsertSongs(songs: List<SongEntity>)

    @Query("UPDATE songs SET isLiked = :liked WHERE id = :id")
    suspend fun setLiked(id: String, liked: Boolean)

    @Query("UPDATE songs SET isDownloaded = 1, localPath = :path WHERE id = :id")
    suspend fun markDownloaded(id: String, path: String)

    @Query("UPDATE songs SET playCount = playCount + 1 WHERE id = :id")
    suspend fun incrementPlayCount(id: String)

    @Delete
    suspend fun deleteSong(song: SongEntity)
}

// ─── Playlist DAO ─────────────────────────────────────────────────────────────
@Dao
interface PlaylistDao {

    @Query("SELECT * FROM playlists ORDER BY updatedAt DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getPlaylistById(id: String): PlaylistEntity?

    @Upsert
    suspend fun upsertPlaylist(playlist: PlaylistEntity)

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN playlist_songs ps ON s.id = ps.songId
        WHERE ps.playlistId = :playlistId
        ORDER BY ps.position ASC
    """)
    fun getSongsInPlaylist(playlistId: String): Flow<List<SongEntity>>

    @Upsert
    suspend fun addSongToPlaylist(crossRef: PlaylistSongEntity)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: String, songId: String)
}

// ─── Recently Played DAO ──────────────────────────────────────────────────────
@Dao
interface RecentlyPlayedDao {

    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN recently_played rp ON s.id = rp.songId
        ORDER BY rp.playedAt DESC
        LIMIT 50
    """)
    fun getRecentlyPlayed(): Flow<List<SongEntity>>

    @Upsert
    suspend fun upsertRecent(entry: RecentlyPlayedEntity)

    @Query("DELETE FROM recently_played WHERE songId NOT IN (SELECT songId FROM recently_played ORDER BY playedAt DESC LIMIT 50)")
    suspend fun trimOldEntries()
}

// ─── Download DAO ─────────────────────────────────────────────────────────────
@Dao
interface DownloadDao {

    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = 'pending' OR status = 'downloading'")
    fun getActiveDownloads(): Flow<List<DownloadEntity>>

    @Upsert
    suspend fun upsertDownload(download: DownloadEntity)

    @Query("UPDATE downloads SET status = :status, progress = :progress WHERE songId = :songId")
    suspend fun updateProgress(songId: String, status: String, progress: Int)

    @Query("DELETE FROM downloads WHERE songId = :songId")
    suspend fun deleteDownload(songId: String)
}
