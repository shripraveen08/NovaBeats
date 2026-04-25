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
            
            // Try Internet Archive first (most reliable)
            try {
                val songs = loadInternetArchiveMusic()
                if (songs.isNotEmpty()) {
                    songDao.upsertSongs(songs)
                    _uiState.update { it.copy(bollywoodTrending = songs, isLoading = false) }
                    return@launch
                }
            } catch (e: Exception) {
                // Continue to next option
            }
            
            // Try JioSaavn as backup (might work sometimes)
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
    
    private suspend fun loadInternetArchiveMusic(): List<SongEntity> {
        return try {
            val songs = mutableListOf<SongEntity>()
            
            // Search for Bollywood music specifically
            val bollywoodQueries = listOf(
                "title:(bollywood OR hindi OR indian) AND mediatype:audio",
                "title:(song OR music) AND (bollywood OR hindi) AND mediatype:audio",
                "description:(bollywood OR hindi) AND mediatype:audio",
                "creator:(indian OR hindi) AND mediatype:audio"
            )
            
            for (query in bollywoodQueries) {
                try {
                    val response = archive.searchMusic(query = query, rows = 8)
                    response.response?.docs?.take(3)?.forEach { doc ->
                        try {
                            val item = archive.getItemMetadata(doc.identifier)
                            val songEntities = item.toSongEntities()
                            if (songEntities.isNotEmpty()) {
                                songs += songEntities.first()
                            }
                        } catch (e: Exception) {
                            // Skip this item
                        }
                    }
                } catch (e: Exception) {
                    // Continue to next query
                }
            }
            
            // If no Bollywood songs found, get general music
            if (songs.isEmpty()) {
                val response = archive.searchMusic(
                    query = "(title:(song OR music) AND mediatype:audio) NOT (mediatype:collection)",
                    rows = 8
                )
                response.response?.docs?.take(5)?.forEach { doc ->
                    try {
                        val item = archive.getItemMetadata(doc.identifier)
                        val songEntities = item.toSongEntities()
                        if (songEntities.isNotEmpty()) {
                            songs += songEntities.first()
                        }
                    } catch (e: Exception) {
                        // Skip this item
                    }
                }
            }
            
            songs
        } catch (e: Exception) {
            emptyList()
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
                    "focus" -> loadBollywoodMoodMusic("focus study instrumental")
                    "sleep" -> loadBollywoodMoodMusic("sleep meditation ambient")
                    "workout" -> loadBollywoodMoodMusic("workout motivation energetic")
                    "chill" -> loadBollywoodMoodMusic("chill lofi relax")
                    "party" -> loadBollywoodMoodMusic("party dance upbeat")
                    "sad" -> loadBollywoodMoodMusic("sad emotional romantic")
                    else -> loadBollywoodMoodMusic(tag)
                }
                songDao.upsertSongs(songs)
                _uiState.update { it.copy(bollywoodTrending = songs, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
    
    private suspend fun loadBollywoodMoodMusic(baseQuery: String): List<SongEntity> {
        return try {
            val songs = mutableListOf<SongEntity>()
            
            // Search for Bollywood music with mood
            val bollywoodMoodQueries = listOf(
                "title:($baseQuery) AND (bollywood OR hindi OR indian) AND mediatype:audio",
                "title:($baseQuery) AND (song OR music) AND (bollywood OR hindi) AND mediatype:audio",
                "description:($baseQuery) AND (bollywood OR hindi) AND mediatype:audio"
            )
            
            for (query in bollywoodMoodQueries) {
                try {
                    val response = archive.searchMusic(query = query, rows = 10)
                    response.response?.docs?.take(4)?.forEach { doc ->
                        try {
                            val item = archive.getItemMetadata(doc.identifier)
                            val songEntities = item.toSongEntities()
                            if (songEntities.isNotEmpty()) {
                                songs += songEntities.first()
                            }
                        } catch (e: Exception) {
                            // Skip this item
                        }
                    }
                } catch (e: Exception) {
                    // Continue to next query
                }
            }
            
            // If no Bollywood mood songs found, try general mood music
            if (songs.isEmpty()) {
                val response = archive.searchMusic(
                    query = "title:($baseQuery) AND mediatype:audio",
                    rows = 8
                )
                response.response?.docs?.take(6)?.forEach { doc ->
                    try {
                        val item = archive.getItemMetadata(doc.identifier)
                        val songEntities = item.toSongEntities()
                        if (songEntities.isNotEmpty()) {
                            songs += songEntities.first()
                        }
                    } catch (e: Exception) {
                        // Skip this item
                    }
                }
            }
            
            // Last resort: try JioSaavn
            if (songs.isEmpty()) {
                songs.addAll(jioSaavn.searchSongs("$baseQuery bollywood", 8))
            }
            
            songs
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun searchBollywood(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val songs = loadBollywoodMoodMusic(query)
                songDao.upsertSongs(songs)
                _uiState.update { it.copy(bollywoodTrending = songs, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
