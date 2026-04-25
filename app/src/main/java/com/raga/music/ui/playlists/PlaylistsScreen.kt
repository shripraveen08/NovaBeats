package com.raga.music.ui.playlists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.raga.music.data.local.entities.PlaylistEntity
import com.raga.music.player.PlayerViewModel
import java.util.UUID

@Composable
fun PlaylistsScreen(
    playerVm: PlayerViewModel,
    playlistsVm: PlaylistsViewModel = hiltViewModel()
) {
    val uiState by playlistsVm.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text     = { Text("New Playlist") },
                icon     = { Icon(Icons.Default.Add, null) },
                onClick  = { showCreateDialog = true }
            )
        }
    ) { padding ->

        if (uiState.playlists.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📋", style = MaterialTheme.typography.displayMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("No playlists yet", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Tap + to create your first playlist",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp)
            ) {
                item {
                    Text(
                        "Your Playlists",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                items(uiState.playlists, key = { it.id }) { playlist ->
                    PlaylistItem(playlist = playlist, onClick = { /* TODO: open playlist */ })
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }

    // ── Create Playlist Dialog ────────────────────────────────────────────────
    if (showCreateDialog) {
        CreatePlaylistDialog(
            onConfirm = { name ->
                playlistsVm.createPlaylist(name)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }
}

@Composable
fun PlaylistItem(playlist: PlaylistEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape    = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier          = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (playlist.thumbnailUrl.isNotEmpty()) {
                AsyncImage(
                    model              = playlist.thumbnailUrl,
                    contentDescription = null,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp))
                )
            } else {
                Box(
                    Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎵", style = MaterialTheme.typography.titleLarge)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(playlist.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    playlist.source.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CreatePlaylistDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text("Create Playlist") },
        text    = {
            OutlinedTextField(
                value         = name,
                onValueChange = { name = it },
                label         = { Text("Playlist name") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick  = { if (name.isNotBlank()) onConfirm(name.trim()) },
                enabled  = name.isNotBlank()
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
