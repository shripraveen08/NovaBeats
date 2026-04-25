package com.raga.music.ui.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raga.music.player.PlayerViewModel
import com.raga.music.ui.home.SongListItem

@Composable
fun LibraryScreen(
    playerVm: PlayerViewModel,
    libraryVm: LibraryViewModel = hiltViewModel()
) {
    val uiState by libraryVm.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Tab Row ────────────────────────────────────────────────────────────
        val tabs = listOf("All Songs", "Downloaded", "Liked")
        var selectedTab by remember { mutableIntStateOf(0) }

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick  = {
                        selectedTab = index
                        libraryVm.setFilter(index)
                    },
                    text = { Text(title) }
                )
            }
        }

        // ── Song Count Header ─────────────────────────────────────────────────
        val currentList = when (selectedTab) {
            1    -> uiState.downloadedSongs
            2    -> uiState.likedSongs
            else -> uiState.allSongs
        }

        Text(
            "${currentList.size} songs",
            style    = MaterialTheme.typography.labelMedium,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp, 12.dp, 16.dp, 4.dp)
        )

        // ── Song List ─────────────────────────────────────────────────────────
        if (currentList.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        when (selectedTab) {
                            1    -> "No downloaded songs yet.\nSearch and download tracks to listen offline."
                            2    -> "No liked songs yet.\nTap ❤️ on any song to save it here."
                            else -> "Your library is empty.\nSearch for music in the Explore tab."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                items(currentList, key = { it.id }) { song ->
                    SongListItem(
                        song    = song,
                        onClick = { playerVm.playSong(song, currentList) }
                    )
                }
            }
        }
    }
}
