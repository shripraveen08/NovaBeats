package com.raga.music.data.remote.jiosaavn

import com.raga.music.data.local.entities.SongEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JioSaavnRepository @Inject constructor(
    private val apiService: JioSaavnApiService
) {
    
    suspend fun searchSongs(query: String, limit: Int = 20): List<SongEntity> {
        return try {
            val response = apiService.searchSongs(query, limit)
            response.results.map { it.toSongEntity() }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getSongDetails(songId: String): SongEntity? {
        return try {
            val response = apiService.getSongDetails(songId)
            response.song?.toSongEntity()
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun getAlbumSongs(albumId: String, limit: Int = 50): List<SongEntity> {
        return try {
            val response = apiService.getAlbumSongs(albumId, limit)
            response.songs.map { it.toSongEntity() }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getPlaylistSongs(playlistId: String, limit: Int = 50): List<SongEntity> {
        return try {
            val response = apiService.getPlaylistSongs(playlistId, limit)
            response.songs.map { it.toSongEntity() }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getTrendingBollywoodSongs(limit: Int = 20): List<SongEntity> {
        return try {
            val response = apiService.getTrendingSongs(limit = limit)
            // Check if response is valid and has results
            if (response.status == "success" && response.results.isNotEmpty()) {
                response.results.map { it.toSongEntity() }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getLatestHindiSongs(limit: Int = 20): List<SongEntity> {
        return searchSongs("latest hindi", limit)
    }
    
    suspend fun getRomanticSongs(limit: Int = 20): List<SongEntity> {
        return searchSongs("romantic hindi", limit)
    }
    
    suspend fun getPartySongs(limit: Int = 20): List<SongEntity> {
        return searchSongs("party hindi", limit)
    }
    
    suspend fun getSadSongs(limit: Int = 20): List<SongEntity> {
        return searchSongs("sad hindi", limit)
    }
}
