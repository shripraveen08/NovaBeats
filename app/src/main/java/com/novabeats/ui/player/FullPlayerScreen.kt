package com.novabeats.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.novabeats.player.PlayerViewModel
import kotlinx.coroutines.delay

@Composable
fun FullPlayerScreen(
    playerVm: PlayerViewModel,
    onDismiss: () -> Unit
) {
    val state by playerVm.state.collectAsState()
    val song = state.currentSong ?: return

    var progress by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(1L) }
    var showSleepTimer by remember { mutableStateOf(false) }

    // Poll position every second
    LaunchedEffect(state.isPlaying) {
        while (true) {
            progress = playerVm.getProgress()
            duration = playerVm.getDuration().coerceAtLeast(1L)
            delay(1000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Top bar ───────────────────────────────────────────────────────
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.KeyboardArrowDown, "Close", Modifier.size(32.dp))
                }
                Spacer(Modifier.weight(1f))
                Text("Now Playing", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { showSleepTimer = true }) {
                    Icon(Icons.Default.Bedtime, "Sleep timer")
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── Album Art ─────────────────────────────────────────────────────
            AsyncImage(
                model              = song.albumArtUrl,
                contentDescription = song.title,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .size(280.dp)
                    .clip(RoundedCornerShape(20.dp))
            )

            Spacer(Modifier.height(32.dp))

            // ── Song Info + Like ──────────────────────────────────────────────
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = song.title,
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                    Text(
                        text  = song.artist,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = { playerVm.toggleLike(song.id, !song.isLiked) }) {
                    Icon(
                        imageVector        = if (song.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint               = if (song.isLiked) Color.Red else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // ── Source Badge ──────────────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth()) {
                val (label, color) = when (song.source) {
                    "jamendo" -> "Jamendo — Free CC Music" to Color(0xFF1DB954)
                    "archive" -> "Internet Archive — Public Domain" to Color(0xFF185FA5)
                    "local"   -> "Local File" to Color(0xFF854F0B)
                    else      -> song.source to Color.Gray
                }
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = color.copy(alpha = 0.12f)
                ) {
                    Text(
                        label,
                        style    = MaterialTheme.typography.labelSmall,
                        color    = color,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Progress Bar ──────────────────────────────────────────────────
            Slider(
                value         = (progress.toFloat() / duration).coerceIn(0f, 1f),
                onValueChange = { playerVm.seekTo((it * duration).toLong()) },
                modifier      = Modifier.fillMaxWidth()
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatDuration(progress), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatDuration(duration), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(16.dp))

            // ── Controls ──────────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                IconButton(onClick = playerVm::toggleShuffle) {
                    Icon(
                        Icons.Default.Shuffle,
                        "Shuffle",
                        tint = if (state.shuffleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = playerVm::seekPrevious, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.SkipPrevious, "Previous", Modifier.size(36.dp))
                }
                FilledIconButton(
                    onClick  = playerVm::togglePlayPause,
                    modifier = Modifier.size(64.dp),
                    shape    = CircleShape
                ) {
                    Icon(
                        imageVector        = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        modifier           = Modifier.size(36.dp)
                    )
                }
                IconButton(onClick = playerVm::seekNext, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.SkipNext, "Next", Modifier.size(36.dp))
                }
                IconButton(onClick = playerVm::toggleRepeat) {
                    val icon = when (state.repeatMode) {
                        Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne
                        Player.REPEAT_MODE_ALL -> Icons.Default.Repeat
                        else                   -> Icons.Default.Repeat
                    }
                    Icon(
                        icon, "Repeat",
                        tint = if (state.repeatMode != Player.REPEAT_MODE_OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Sleep timer indicator ─────────────────────────────────────────
            if (state.sleepTimerMinutes > 0) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "😴 Sleep timer: ${state.sleepTimerMinutes} min remaining",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // ── Sleep Timer Dialog ────────────────────────────────────────────────────
    if (showSleepTimer) {
        SleepTimerDialog(
            onConfirm = { minutes ->
                playerVm.setSleepTimer(minutes)
                showSleepTimer = false
            },
            onDismiss = { showSleepTimer = false }
        )
    }
}

@Composable
fun SleepTimerDialog(onConfirm: (Int) -> Unit, onDismiss: () -> Unit) {
    val options = listOf(5, 10, 15, 20, 30, 45, 60)
    AlertDialog(
        onDismissRequest = onDismiss,
        title            = { Text("Sleep Timer") },
        text             = {
            Column {
                Text("Auto-pause after:", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
                options.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { min ->
                            OutlinedButton(
                                onClick  = { onConfirm(min) },
                                modifier = Modifier.weight(1f)
                            ) { Text("${min}m") }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
