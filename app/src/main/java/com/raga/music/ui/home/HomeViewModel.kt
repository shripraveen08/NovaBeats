package com.raga.music.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raga.music.data.local.dao.RecentlyPlayedDao
import com.raga.music.data.local.dao.SongDao
import com.raga.music.data.local.entities.SongEntity
import com.raga.music.data.remote.jiosaavn.JioSaavnRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val recentSongs: List<SongEntity> = emptyList(),
    val bollywoodTrending: List<SongEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val songDao: SongDao,
    private val recentDao: RecentlyPlayedDao,
    private val jioSaavn: JioSaavnRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeRecent()
        loadBollywoodTrending()
    }

    private fun observeRecent() {
        recentDao.getRecentlyPlayed()
            .onEach { songs -> _uiState.update { it.copy(recentSongs = songs) } }
            .launchIn(viewModelScope)
    }

    private fun loadBollywoodTrending() {
        viewModelScope.launch {
            try {
                val songs = jioSaavn.getTrendingBollywoodSongs()
                songDao.upsertSongs(songs)
                _uiState.update { it.copy(bollywoodTrending = songs) }
            } catch (e: Exception) {
                // Silent fail - Bollywood section will be empty
            }
        }
    }

    fun loadByMood(tag: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val songs = when (tag) {
                    "focus" -> jioSaavn.searchSongs("focus instrumental", 20)
                    "sleep" -> jioSaavn.searchSongs("sleep meditation", 20)
                    "workout" -> jioSaavn.searchSongs("workout motivation", 20)
                    "chill" -> jioSaavn.searchSongs("chill lofi", 20)
                    "party" -> jioSaavn.getPartySongs()
                    "sad" -> jioSaavn.getSadSongs()
                    else -> jioSaavn.searchSongs(tag, 20)
                }
                songDao.upsertSongs(songs)
                _uiState.update { it.copy(bollywoodTrending = songs, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun searchBollywood(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val songs = jioSaavn.searchSongs(query)
                songDao.upsertSongs(songs)
                _uiState.update { it.copy(bollywoodTrending = songs, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
