package com.raga.music.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raga.music.data.local.dao.SongDao
import com.raga.music.data.local.entities.SongEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class LibraryUiState(
    val allSongs: List<SongEntity> = emptyList(),
    val downloadedSongs: List<SongEntity> = emptyList(),
    val likedSongs: List<SongEntity> = emptyList()
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val songDao: SongDao
) : ViewModel() {

    val uiState: StateFlow<LibraryUiState> = combine(
        songDao.getAllSongs(),
        songDao.getDownloadedSongs(),
        songDao.getLikedSongs()
    ) { all, downloaded, liked ->
        LibraryUiState(
            allSongs        = all,
            downloadedSongs = downloaded,
            likedSongs      = liked
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LibraryUiState())

    fun setFilter(tab: Int) { /* tab switching handled in UI */ }
}
