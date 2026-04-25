package com.raga.music.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.raga.music.data.local.entities.SongEntity
import com.raga.music.player.PlayerViewModel

@Composable
fun HomeScreen(
    playerVm: PlayerViewModel,
    homeVm: HomeViewModel = hiltViewModel()
) {
    val uiState by homeVm.uiState.collectAsState()

    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            HomeHeader()
        }

        item {
            MoodSection(onMoodSelected = homeVm::loadByMood)
        }

        if (uiState.recentSongs.isNotEmpty()) {
            item {
                SectionTitle("Recently Played")
                LazyRow(
                    contentPadding      = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.recentSongs) { song ->
                        SongCard(song = song, onClick = {
                            playerVm.playSong(song, uiState.recentSongs)
                        })
                    }
                }
            }
        }

        if (uiState.bollywoodTrending.isNotEmpty()) {
            item { SectionTitle("� Music — Trending") }
            item {
                LazyRow(
                    contentPadding      = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.bollywoodTrending) { song ->
                        SongCard(song = song, onClick = {
                            playerVm.playSong(song, uiState.bollywoodTrending)
                        })
                    }
                }
            }
        }

        
        if (uiState.isLoading) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun HomeHeader() {
    Column(modifier = Modifier.padding(16.dp, 24.dp, 16.dp, 8.dp)) {
        Text(
            text  = "Raga",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text  = "Your Music Soundtrack",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MoodSection(onMoodSelected: (String) -> Unit) {
    val moods = listOf(
        "🎯 Focus" to "focus",
        "😴 Sleep"  to "sleep",
        "💪 Workout" to "workout",
        "😌 Chill"  to "chill",
        "🎉 Party"  to "party",
        "💔 Sad"    to "sad"
    )

    Column {
        SectionTitle("What's your mood?")
        LazyRow(
            contentPadding        = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(moods) { (label, tag) ->
                FilterChip(
                    selected = false,
                    onClick  = { onMoodSelected(tag) },
                    label    = { Text(label) }
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text     = title,
        style    = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun SongCard(song: SongEntity, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model              = song.albumArtUrl,
            contentDescription = song.title,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(12.dp))
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text     = song.title,
            style    = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text     = song.artist,
            style    = MaterialTheme.typography.labelSmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SongListItem(song: SongEntity, onClick: () -> Unit) {
    ListItem(
        headlineContent   = { Text(song.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = { Text("${song.artist} • ${song.source.replaceFirstChar { it.uppercase() }}", maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingContent    = {
            AsyncImage(
                model              = song.albumArtUrl,
                contentDescription = null,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}
