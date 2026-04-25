package com.raga.music.ui.explore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.raga.music.player.PlayerViewModel
import com.raga.music.ui.home.SongListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    playerVm: PlayerViewModel,
    exploreVm: ExploreViewModel = hiltViewModel()
) {
    val uiState by exploreVm.uiState.collectAsState()
    var query by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Search Bar ─────────────────────────────────────────────────────────
        SearchBar(
            query           = query,
            onQueryChange   = {
                query = it
                if (it.length >= 2) exploreVm.search(it)
            },
            onSearch        = { exploreVm.search(query) },
            active          = false,
            onActiveChange  = {},
            placeholder     = { Text("Search Bollywood music…") },
            leadingIcon     = { Icon(Icons.Default.Search, null) },
            trailingIcon    = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = {
                        query = ""
                        exploreVm.clearSearch()
                    }) { Icon(Icons.Default.Clear, null) }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {}

        // ── Source Filter Chips ────────────────────────────────────────────────
        Row(
            modifier              = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "JioSaavn", "Archive").forEach { label ->
                FilterChip(
                    selected = uiState.activeSource == label.lowercase(),
                    onClick  = { exploreVm.setSource(label.lowercase()) },
                    label    = { Text(label) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Results ───────────────────────────────────────────────────────────
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.results.isEmpty() && query.isNotEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No results found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            uiState.results.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎵", style = MaterialTheme.typography.displayMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("Search millions of Bollywood tracks", style = MaterialTheme.typography.bodyLarge)
                        Text("Powered by JioSaavn & Internet Archive", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            else -> {
                LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                    item {
                        Text(
                            "${uiState.results.size} results",
                            style    = MaterialTheme.typography.labelMedium,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                    items(uiState.results, key = { it.id }) { song ->
                        SongListItem(
                            song    = song,
                            onClick = { playerVm.playSong(song, uiState.results) }
                        )
                    }
                }
            }
        }
    }
}
