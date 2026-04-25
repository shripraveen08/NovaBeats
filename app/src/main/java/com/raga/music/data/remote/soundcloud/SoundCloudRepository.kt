package com.raga.music.data.remote.soundcloud

import com.raga.music.data.local.entities.SongEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundCloudRepository @Inject constructor(
    private val apiService: SoundCloudApiService
) {
    
    suspend fun searchSongs(query: String, limit: Int = 20): List<SongEntity> {
        return try {
            val response = apiService.searchTracks(query, limit)
            response.tracks.map { it.toSongEntity() }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getTrackDetails(trackId: String): SongEntity? {
        return try {
            val track = apiService.getTrackDetails(trackId)
            track.toSongEntity()
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun getTrendingBollywoodSongs(limit: Int = 20): List<SongEntity> {
        return try {
            val response = apiService.getTrendingTracks(limit = limit)
            response.tracks.map { it.toSongEntity() }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getLatestSongs(limit: Int = 20): List<SongEntity> {
        return searchSongs("latest", limit)
    }
    
    suspend fun getBollywoodSongs(limit: Int = 20): List<SongEntity> {
        return searchSongs("bollywood", limit)
    }
    
    suspend fun getPartySongs(limit: Int = 20): List<SongEntity> {
        return searchSongs("party remix", limit)
    }
    
    suspend fun getSadSongs(limit: Int = 20): List<SongEntity> {
        return searchSongs("sad romantic", limit)
    }
    
    suspend fun getFocusSongs(limit: Int = 20): List<SongEntity> {
        return searchSongs("focus study", limit)
    }
    
    suspend fun getWorkoutSongs(limit: Int = 20): List<SongEntity> {
        return searchSongs("workout motivation", limit)
    }
}
