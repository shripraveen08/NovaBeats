package com.raga.music.ui.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raga.music.data.local.dao.PlaylistDao
import com.raga.music.data.local.entities.PlaylistEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class PlaylistsUiState(
    val playlists: List<PlaylistEntity> = emptyList()
)

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    private val playlistDao: PlaylistDao
) : ViewModel() {

    val uiState: StateFlow<PlaylistsUiState> =
        playlistDao.getAllPlaylists()
            .map { PlaylistsUiState(playlists = it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlaylistsUiState())

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            playlistDao.upsertPlaylist(
                PlaylistEntity(
                    id     = UUID.randomUUID().toString(),
                    name   = name,
                    source = "local"
                )
            )
        }
    }

    fun deletePlaylist(playlist: PlaylistEntity) {
        viewModelScope.launch { playlistDao.deletePlaylist(playlist) }
    }
}
