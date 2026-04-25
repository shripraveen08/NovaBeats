package com.raga.music.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raga.music.data.local.dao.RecentlyPlayedDao
import com.raga.music.data.local.dao.SongDao
import com.raga.music.data.local.entities.SongEntity
import com.raga.music.data.remote.jiosaavn.JioSaavnRepository
import com.raga.music.data.remote.soundcloud.SoundCloudRepository
import com.raga.music.data.remote.archive.ArchiveApiService
import com.raga.music.data.remote.archive.toSongEntities
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
    private val soundCloud: SoundCloudRepository,
    private val jioSaavn: JioSaavnRepository,
    private val archive: ArchiveApiService
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
            _uiState.update { it.copy(isLoading = true) }
            
            // Try SoundCloud first (most reliable)
            try {
                val songs = soundCloud.getBollywoodSongs()
                if (songs.isNotEmpty()) {
                    songDao.upsertSongs(songs)
                    _uiState.update { it.copy(bollywoodTrending = songs, isLoading = false) }
                    return@launch
                }
            } catch (e: Exception) {
                // Continue to next option
            }
            
            // Try JioSaavn as backup
            try {
                val songs = jioSaavn.getTrendingBollywoodSongs()
                if (songs.isNotEmpty()) {
                    songDao.upsertSongs(songs)
                    _uiState.update { it.copy(bollywoodTrending = songs, isLoading = false) }
                    return@launch
                }
            } catch (e: Exception) {
                // Continue to fallback
            }
            
            // Load fallback content
            loadFallbackContent()
        }
    }
    
    private suspend fun loadFallbackContent() {
        try {
            // Try to load music from Internet Archive as fallback
            val archiveSongs = try {
                val response = archive.searchMusic(
                    query = "title:(music) AND mediatype:audio",
                    rows = 5
                )
                val songs = mutableListOf<SongEntity>()
                response.response?.docs?.take(3)?.forEach { doc ->
                    try {
                        val item = archive.getItemMetadata(doc.identifier)
                        songs += item.toSongEntities()
                    } catch (e: Exception) {
                        // Skip this item if metadata fails
                    }
                }
                songs
            } catch (e: Exception) {
                emptyList()
            }
            
            if (archiveSongs.isNotEmpty()) {
                songDao.upsertSongs(archiveSongs)
                _uiState.update { it.copy(bollywoodTrending = archiveSongs, isLoading = false) }
            } else {
                // Create demo songs as last resort
                val demoSongs = listOf(
                    SongEntity(
                        id = "demo_1",
                        title = "Demo Song 1 - Internet Archive",
                        artist = "Various Artists",
                        album = "Public Domain Collection",
                        albumArtUrl = "https://picsum.photos/300/300?random=1",
                        duration = 240000L,
                        source = "demo",
                        streamUrl = ""
                    ),
                    SongEntity(
                        id = "demo_2",
                        title = "Demo Song 2 - Free Music",
                        artist = "Various Artists", 
                        album = "Public Domain Collection",
                        albumArtUrl = "https://picsum.photos/300/300?random=2",
                        duration = 180000L,
                        source = "demo",
                        streamUrl = ""
                    )
                )
                songDao.upsertSongs(demoSongs)
                _uiState.update { it.copy(bollywoodTrending = demoSongs, isLoading = false) }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, error = "Unable to load music content") }
        }
    }

    fun loadByMood(tag: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val songs = when (tag) {
                    "focus" -> soundCloud.getFocusSongs(20)
                    "sleep" -> soundCloud.searchSongs("sleep meditation", 20)
                    "workout" -> soundCloud.getWorkoutSongs(20)
                    "chill" -> soundCloud.searchSongs("chill lofi", 20)
                    "party" -> soundCloud.getPartySongs(20)
                    "sad" -> soundCloud.getSadSongs(20)
                    else -> soundCloud.searchSongs(tag, 20)
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
