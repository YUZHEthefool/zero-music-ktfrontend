package com.zeromusic.player

import com.zeromusic.data.model.PlaybackInfo
import com.zeromusic.data.model.PlayerState
import com.zeromusic.data.model.RepeatMode
import com.zeromusic.data.model.Song
import kotlinx.coroutines.flow.StateFlow

/**
 * 音乐播放器接口
 * 定义播放器的核心功能
 */
interface MusicPlayer {
    
    /**
     * 播放器状态流
     */
    val playerState: StateFlow<PlayerState>
    
    /**
     * 播放信息流
     */
    val playbackInfo: StateFlow<PlaybackInfo>
    
    /**
     * 播放指定歌曲
     * @param song 要播放的歌曲
     */
    suspend fun play(song: Song)
    
    /**
     * 继续播放
     */
    fun resume()
    
    /**
     * 暂停播放
     */
    fun pause()
    
    /**
     * 停止播放
     */
    fun stop()
    
    /**
     * 播放下一曲
     */
    fun skipToNext()
    
    /**
     * 播放上一曲
     */
    fun skipToPrevious()
    
    /**
     * 跳转到指定位置
     * @param position 位置 (毫秒)
     */
    fun seekTo(position: Long)
    
    /**
     * 设置循环模式
     * @param mode 循环模式
     */
    fun setRepeatMode(mode: RepeatMode)
    
    /**
     * 设置随机播放
     * @param enabled 是否启用随机播放
     */
    fun setShuffleEnabled(enabled: Boolean)
    
    /**
     * 设置播放列表
     * @param songs 歌曲列表
     */
    fun setPlaylist(songs: List<Song>)
    
    /**
     * 释放资源
     */
    fun release()
}