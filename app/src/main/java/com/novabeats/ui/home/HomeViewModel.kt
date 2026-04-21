package com.novabeats.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novabeats.data.local.dao.RecentlyPlayedDao
import com.novabeats.data.local.dao.SongDao
import com.novabeats.data.local.entities.SongEntity
import com.novabeats.data.remote.jamendo.JamendoApiService
import com.novabeats.data.remote.jamendo.toSongEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val recentSongs: List<SongEntity> = emptyList(),
    val featuredSongs: List<SongEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val songDao: SongDao,
    private val recentDao: RecentlyPlayedDao,
    private val jamendo: JamendoApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeRecent()
        loadFeatured()
    }

    private fun observeRecent() {
        recentDao.getRecentlyPlayed()
            .onEach { songs -> _uiState.update { it.copy(recentSongs = songs) } }
            .launchIn(viewModelScope)
    }

    private fun loadFeatured() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = jamendo.getFeaturedTracks()
                val songs = response.results.map { it.toSongEntity() }
                songDao.upsertSongs(songs)
                _uiState.update { it.copy(featuredSongs = songs, isLoading = false) }
            } catch (e: Exception) {
                // Fall back to local songs
                songDao.getAllSongs().first().let { local ->
                    _uiState.update { it.copy(featuredSongs = local, isLoading = false, error = e.message) }
                }
            }
        }
    }

    fun loadByMood(tag: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = jamendo.getTracksByMood(tags = tag)
                val songs = response.results.map { it.toSongEntity() }
                songDao.upsertSongs(songs)
                _uiState.update { it.copy(featuredSongs = songs, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
