package com.novabeats.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.novabeats.data.local.dao.*
import com.novabeats.data.local.entities.*

@Database(
    entities = [
        SongEntity::class,
        PlaylistEntity::class,
        PlaylistSongEntity::class,
        RecentlyPlayedEntity::class,
        DownloadEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NovaBeatDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun recentlyPlayedDao(): RecentlyPlayedDao
    abstract fun downloadDao(): DownloadDao
}
