package com.novabeats.player

import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.novabeats.data.local.dao.RecentlyPlayedDao
import com.novabeats.data.local.dao.SongDao
import com.novabeats.data.local.entities.RecentlyPlayedEntity
import com.novabeats.data.local.entities.SongEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerState(
    val currentSong: SongEntity? = null,
    val isPlaying: Boolean = false,
    val progress: Long = 0L,
    val duration: Long = 0L,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val shuffleEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val sleepTimerMinutes: Int = 0,
    val queue: List<SongEntity> = emptyList()
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val songDao: SongDao,
    private val recentDao: RecentlyPlayedDao
) : ViewModel() {

    private val _state = MutableStateFlow(PlayerState())
    val state = _state.asStateFlow()

    private var controller: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null

    private var sleepJob: Job? = null

    init {
        connectToService()
    }

    private fun connectToService() {
        val token = SessionToken(
            context,
            ComponentName(context, NovaBeatPlayerService::class.java)
        )

        controllerFuture = MediaController.Builder(context, token).buildAsync()

        controllerFuture?.addListener({
            controller = controllerFuture?.get()
            controller?.addListener(playerListener)
            syncState()
        }, MoreExecutors.directExecutor())
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.update { it.copy(isPlaying = isPlaying) }
        }

        override fun onPlaybackStateChanged(state: Int) {
            _state.update { it.copy(isLoading = state == Player.STATE_BUFFERING) }
        }

        override fun onMediaItemTransition(item: MediaItem?, reason: Int) {
            updateCurrentSong(item)
        }
    }

    private fun syncState() {
        controller?.let { c ->
            _state.update {
                it.copy(
                    isPlaying = c.isPlaying,
                    shuffleEnabled = c.shuffleModeEnabled,
                    repeatMode = c.repeatMode
                )
            }
        }
    }

    private fun updateCurrentSong(item: MediaItem?) {
        val songId = item?.mediaId ?: return

        viewModelScope.launch {
            val song = songDao.getSongById(songId)

            _state.update { it.copy(currentSong = song) }

            song?.let {
                songDao.incrementPlayCount(it.id)
                recentDao.upsertRecent(RecentlyPlayedEntity(it.id))
                recentDao.trimOldEntries()
            }
        }
    }

    // ─── Public controls ─────────────────────────────────────────────

    fun playSong(song: SongEntity, queue: List<SongEntity> = emptyList()) {
        viewModelScope.launch {

            val c = controller ?: return@launch

            val fullQueue = if (queue.isEmpty()) listOf(song) else queue
            val items = fullQueue.map { it.toMediaItem() }
            val index = fullQueue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)

            c.setMediaItems(items, index, 0L)
            c.prepare()
            c.play()

            _state.update { it.copy(queue = fullQueue) }

            songDao.upsertSong(song)
        }
    }

    fun togglePlayPause() {
        controller?.run {
            if (isPlaying) pause() else play()
        }
    }

    fun seekNext() = controller?.seekToNextMediaItem()

    fun seekPrevious() = controller?.seekToPreviousMediaItem()

    fun seekTo(position: Long) = controller?.seekTo(position)

    fun toggleShuffle() {
        val newVal = !(controller?.shuffleModeEnabled ?: false)
        controller?.shuffleModeEnabled = newVal
        _state.update { it.copy(shuffleEnabled = newVal) }
    }

    fun toggleRepeat() {
        val next = when (controller?.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }

        controller?.repeatMode = next
        _state.update { it.copy(repeatMode = next) }
    }

    fun toggleLike(songId: String, liked: Boolean) {
        viewModelScope.launch {
            songDao.setLiked(songId, liked)
        }
    }

    fun setSleepTimer(minutes: Int) {
        sleepJob?.cancel()

        _state.update { it.copy(sleepTimerMinutes = minutes) }

        if (minutes > 0) {
            sleepJob = viewModelScope.launch {
                kotlinx.coroutines.delay(minutes * 60_000L)
                controller?.pause()
                _state.update { it.copy(sleepTimerMinutes = 0) }
            }
        }
    }

    fun getProgress(): Long = controller?.currentPosition ?: 0L

    fun getDuration(): Long = controller?.duration?.coerceAtLeast(0L) ?: 0L

    override fun onCleared() {
        controller?.removeListener(playerListener)

        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }

        controller = null
        controllerFuture = null

        sleepJob?.cancel()

        super.onCleared()
    }
}

private fun SongEntity.toMediaItem(): MediaItem =
    MediaItem.Builder()
        .setMediaId(id)
        .setUri(
            if (isDownloaded && localPath.isNotEmpty())
                localPath
            else
                streamUrl
        )
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .setArtworkUri(android.net.Uri.parse(albumArtUrl))
                .build()
        )
        .build()