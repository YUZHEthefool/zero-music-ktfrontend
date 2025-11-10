package com.zeromusic.data.model

/**
 * 播放器状态
 */
sealed class PlayerState {
    object Idle : PlayerState()
    object Buffering : PlayerState()
    object Playing : PlayerState()
    object Paused : PlayerState()
    data class Error(val message: String) : PlayerState()
}

/**
 * 播放模式
 */
enum class RepeatMode {
    OFF,        // 不循环
    ALL,        // 列表循环
    ONE         // 单曲循环
}

/**
 * 播放器信息
 */
data class PlaybackInfo(
    val currentSong: Song? = null,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val isPlaying: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val isShuffleEnabled: Boolean = false
) {
    /**
     * 获取播放进度 (0.0 - 1.0)
     */
    val progress: Float
        get() = if (duration > 0) {
            (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
}