package com.zeromusic.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.zeromusic.data.model.PlaybackInfo
import com.zeromusic.data.model.PlayerState
import com.zeromusic.data.model.RepeatMode
import com.zeromusic.data.model.Song
import com.zeromusic.util.Constants
import com.zeromusic.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ExoPlayer 实现的音乐播放器
 */
@Singleton
class ExoPlayerMusicPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) : MusicPlayer, Player by exoPlayer {
    
    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()
    
    /**
     * 获取底层的 ExoPlayer 实例
     * 用于 MediaSession 集成
     */
    fun getPlayer(): Player = exoPlayer
    
    private val _playerState = MutableStateFlow<PlayerState>(PlayerState.Idle)
    override val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()
    
    private val _playbackInfo = MutableStateFlow(PlaybackInfo())
    override val playbackInfo: StateFlow<PlaybackInfo> = _playbackInfo.asStateFlow()
    
    private var playlist: List<Song> = emptyList()
    private var currentSongIndex: Int = -1
    
    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressUpdateJob: Job? = null
    
    init {
        setupPlayer()
    }
    
    private fun setupPlayer() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                val stateName = when (playbackState) {
                    Player.STATE_IDLE -> "IDLE"
                    Player.STATE_BUFFERING -> "BUFFERING"
                    Player.STATE_READY -> "READY"
                    Player.STATE_ENDED -> "ENDED"
                    else -> "UNKNOWN"
                }
                Logger.player("Playback state changed", stateName)
                updatePlayerState(playbackState)
            }
            
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                Logger.player("Is playing changed", isPlaying.toString())
                updatePlaybackInfo()
                if (isPlaying) {
                    startProgressUpdate()
                } else {
                    stopProgressUpdate()
                }
            }
            
            override fun onPlayerError(error: PlaybackException) {
                Logger.e("Player error occurred", error)
                _playerState.value = PlayerState.Error(
                    error.message ?: "播放出错"
                )
            }
        })
    }
    
    private fun updatePlayerState(playbackState: Int) {
        _playerState.value = when (playbackState) {
            Player.STATE_IDLE -> PlayerState.Idle
            Player.STATE_BUFFERING -> PlayerState.Buffering
            Player.STATE_READY -> {
                if (exoPlayer.isPlaying) PlayerState.Playing else PlayerState.Paused
            }
            Player.STATE_ENDED -> {
                handlePlaybackEnded()
                PlayerState.Paused
            }
            else -> PlayerState.Idle
        }
    }
    
    private fun handlePlaybackEnded() {
        when (_playbackInfo.value.repeatMode) {
            RepeatMode.ONE -> {
                exoPlayer.seekTo(0)
                exoPlayer.play()
            }
            RepeatMode.ALL -> {
                skipToNext()
            }
            RepeatMode.OFF -> {
                if (currentSongIndex < playlist.size - 1) {
                    skipToNext()
                }
            }
        }
    }
    
    private fun updatePlaybackInfo() {
        _playbackInfo.value = _playbackInfo.value.copy(
            currentPosition = exoPlayer.currentPosition,
            duration = exoPlayer.duration.takeIf { it > 0 } ?: 0,
            isPlaying = exoPlayer.isPlaying
        )
    }
    
    private fun startProgressUpdate() {
        stopProgressUpdate()
        progressUpdateJob = scope.launch {
            while (isActive && exoPlayer.isPlaying) {
                updatePlaybackInfo()
                delay(500) // 每 500ms 更新一次
            }
        }
    }
    
    private fun stopProgressUpdate() {
        progressUpdateJob?.cancel()
        progressUpdateJob = null
    }
    
    override suspend fun play(song: Song) {
        Logger.player("Playing song", "${song.title} - ${song.artist}")
        
        val index = playlist.indexOfFirst { it.id == song.id }
        if (index != -1) {
            currentSongIndex = index
        } else {
            // 如果歌曲不在播放列表中,添加到列表
            playlist = playlist + song
            currentSongIndex = playlist.size - 1
        }
        
        val streamUrl = "${Constants.BASE_URL}/api/stream/${song.id}"
        Logger.network("GET", streamUrl)
        val mediaItem = MediaItem.fromUri(streamUrl)
        
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
        
        _playbackInfo.value = _playbackInfo.value.copy(
            currentSong = song
        )
    }
    
    override fun resume() {
        Logger.player("Resume playback")
        exoPlayer.play()
    }
    
    override fun pause() {
        Logger.player("Pause playback")
        exoPlayer.pause()
    }
    
    override fun stop() {
        Logger.player("Stop playback")
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        _playbackInfo.value = PlaybackInfo()
        _playerState.value = PlayerState.Idle
        currentSongIndex = -1
    }
    
    override fun skipToNext() {
        if (playlist.isEmpty()) {
            Logger.w("Cannot skip to next: playlist is empty")
            return
        }
        
        Logger.player("Skip to next")
        
        val nextIndex = if (_playbackInfo.value.isShuffleEnabled) {
            (0 until playlist.size).filter { it != currentSongIndex }.randomOrNull() ?: 0
        } else {
            (currentSongIndex + 1) % playlist.size
        }
        
        currentSongIndex = nextIndex
        val nextSong = playlist[currentSongIndex]
        
        scope.launch {
            play(nextSong)
        }
    }
    
    override fun skipToPrevious() {
        if (playlist.isEmpty()) {
            Logger.w("Cannot skip to previous: playlist is empty")
            return
        }
        
        Logger.player("Skip to previous")
        
        // 如果当前播放进度超过 3 秒,则重新播放当前歌曲
        if (exoPlayer.currentPosition > 3000) {
            Logger.player("Restarting current song", "position > 3s")
            exoPlayer.seekTo(0)
            return
        }
        
        val previousIndex = if (currentSongIndex > 0) {
            currentSongIndex - 1
        } else {
            playlist.size - 1
        }
        
        currentSongIndex = previousIndex
        val previousSong = playlist[currentSongIndex]
        
        scope.launch {
            play(previousSong)
        }
    }
    
    override fun seekTo(position: Long) {
        Logger.player("Seek to position", "${position}ms")
        exoPlayer.seekTo(position)
        updatePlaybackInfo()
    }
    
    override fun setRepeatMode(mode: RepeatMode) {
        Logger.player("Set repeat mode", mode.toString())
        _playbackInfo.value = _playbackInfo.value.copy(
            repeatMode = mode
        )
        
        exoPlayer.repeatMode = when (mode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
        }
    }
    
    override fun setShuffleEnabled(enabled: Boolean) {
        Logger.player("Set shuffle", enabled.toString())
        _playbackInfo.value = _playbackInfo.value.copy(
            isShuffleEnabled = enabled
        )
    }
    
    override fun setPlaylist(songs: List<Song>) {
        Logger.player("Set playlist", "${songs.size} songs")
        playlist = songs
        if (currentSongIndex >= songs.size) {
            currentSongIndex = -1
        }
    }
    
    override fun release() {
        Logger.player("Releasing player resources")
        stopProgressUpdate()
        exoPlayer.release()
    }
}