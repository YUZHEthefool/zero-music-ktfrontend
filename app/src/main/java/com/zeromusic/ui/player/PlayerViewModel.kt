package com.zeromusic.ui.player

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zeromusic.data.model.PlaybackInfo
import com.zeromusic.data.model.PlayerState
import com.zeromusic.data.model.RepeatMode
import com.zeromusic.data.model.Song
import com.zeromusic.player.MusicPlayer
import com.zeromusic.service.MusicPlaybackService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 播放器 ViewModel
 * 管理播放器的状态和控制
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    application: Application,
    private val musicPlayer: MusicPlayer
) : AndroidViewModel(application) {
    
    private val context = application.applicationContext
    private var serviceStarted = false
    
    /**
     * 播放器状态
     */
    val playerState: StateFlow<PlayerState> = musicPlayer.playerState
    
    /**
     * 播放信息
     */
    val playbackInfo: StateFlow<PlaybackInfo> = musicPlayer.playbackInfo
    
    /**
     * 播放歌曲
     * @param song 要播放的歌曲
     */
    fun playSong(song: Song) {
        viewModelScope.launch {
            musicPlayer.play(song)
        }
    }
    
    /**
     * 播放/暂停切换
     */
    fun togglePlayPause() {
        if (playbackInfo.value.isPlaying) {
            musicPlayer.pause()
        } else {
            musicPlayer.resume()
        }
    }
    
    /**
     * 暂停播放
     */
    fun pause() {
        musicPlayer.pause()
        updateNotification()
    }
    
    /**
     * 继续播放
     */
    fun resume() {
        musicPlayer.resume()
        updateNotification()
    }
    
    /**
     * 停止播放
     */
    fun stop() {
        musicPlayer.stop()
        stopService()
    }
    
    /**
     * 播放下一曲
     */
    fun skipToNext() {
        musicPlayer.skipToNext()
        updateNotification()
    }
    
    /**
     * 播放上一曲
     */
    fun skipToPrevious() {
        musicPlayer.skipToPrevious()
        updateNotification()
    }
    
    /**
     * 跳转到指定位置
     * @param position 位置 (毫秒)
     */
    fun seekTo(position: Long) {
        musicPlayer.seekTo(position)
    }
    
    /**
     * 切换循环模式
     */
    fun toggleRepeatMode() {
        val currentMode = playbackInfo.value.repeatMode
        val nextMode = when (currentMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        musicPlayer.setRepeatMode(nextMode)
    }
    
    /**
     * 切换随机播放
     */
    fun toggleShuffle() {
        val enabled = !playbackInfo.value.isShuffleEnabled
        musicPlayer.setShuffleEnabled(enabled)
    }
    
    /**
     * 设置播放列表
     * @param songs 歌曲列表
     */
    fun setPlaylist(songs: List<Song>) {
        musicPlayer.setPlaylist(songs)
    }
    
    /**
     * 启动前台服务 (如果需要)
     */
    private fun startServiceIfNeeded() {
        if (!serviceStarted) {
            val intent = Intent(context, MusicPlaybackService::class.java)
            context.startForegroundService(intent)
            serviceStarted = true
        }
    }
    
    /**
     * 更新通知
     */
    private fun updateNotification() {
        val song = playbackInfo.value.currentSong ?: return
        val isPlaying = playbackInfo.value.isPlaying
        
        val intent = Intent(context, MusicPlaybackService::class.java).apply {
            action = "UPDATE_NOTIFICATION"
            putExtra("title", song.title)
            putExtra("artist", song.artist)
            putExtra("isPlaying", isPlaying)
        }
        context.startService(intent)
    }
    
    /**
     * 停止前台服务
     */
    private fun stopService() {
        if (serviceStarted) {
            val intent = Intent(context, MusicPlaybackService::class.java).apply {
                action = MusicPlaybackService.ACTION_STOP
            }
            context.startService(intent)
            serviceStarted = false
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        stopService()
        musicPlayer.release()
    }
}