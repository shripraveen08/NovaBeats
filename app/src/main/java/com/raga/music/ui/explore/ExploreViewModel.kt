package com.raga.music.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raga.music.data.local.dao.SongDao
import com.raga.music.data.local.entities.SongEntity
import com.raga.music.data.remote.archive.ArchiveApiService
import com.raga.music.data.remote.archive.toSongEntities
import com.raga.music.data.remote.jiosaavn.JioSaavnRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExploreUiState(
    val results: List<SongEntity> = emptyList(),
    val isLoading: Boolean = false,
    val activeSource: String = "all",
    val error: String? = null
)

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val songDao: SongDao,
    private val jioSaavn: JioSaavnRepository,
    private val archive: ArchiveApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var lastQuery = ""

    fun search(query: String) {
        if (query == lastQuery) return
        lastQuery = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // debounce
            _uiState.update { it.copy(isLoading = true, error = null) }

            val source = _uiState.value.activeSource
            val results = mutableListOf<SongEntity>()

            try {
                if (source == "all" || source == "jiosaavn") {
                    val r = jioSaavn.searchSongs(query, 10)
                    results += r
                }
            } catch (_: Exception) {}

            try {
                if (source == "all" || source == "archive") {
                    // Search for Bollywood music first
                    val bollywoodQueries = listOf(
                        "title:($query) AND (bollywood OR hindi OR indian) AND mediatype:audio",
                        "title:($query) AND (song OR music) AND (bollywood OR hindi) AND mediatype:audio",
                        "title:($query) AND mediatype:audio"
                    )
                    
                    for (bq in bollywoodQueries) {
                        try {
                            val r = archive.searchMusic(query = bq, rows = 5)
                            r.response?.docs?.take(2)?.forEach { doc ->
                                try {
                                    val item = archive.getItemMetadata(doc.identifier)
                                    results += item.toSongEntities()
                                } catch (_: Exception) {}
                            }
                            if (results.size >= 8) break // Stop if we have enough results
                        } catch (_: Exception) {}
                    }
                }
            } catch (_: Exception) {}

            songDao.upsertSongs(results)
            _uiState.update { it.copy(results = results, isLoading = false) }
        }
    }

    fun setSource(source: String) {
        _uiState.update { it.copy(activeSource = source) }
        if (lastQuery.isNotEmpty()) {
            lastQuery = "" // force re-search
            search(lastQuery)
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        lastQuery = ""
        _uiState.update { it.copy(results = emptyList(), isLoading = false) }
    }
}
