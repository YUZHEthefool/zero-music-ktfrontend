package com.zeromusic.data.model

import com.google.gson.annotations.SerializedName

/**
 * 歌曲数据模型
 * 对应后端 API 返回的歌曲信息
 */
data class Song(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("artist")
    val artist: String,
    
    @SerializedName("album")
    val album: String,
    
    @SerializedName("duration")
    val duration: Long,
    
    @SerializedName("file_path")
    val filePath: String,
    
    @SerializedName("file_name")
    val fileName: String,
    
    @SerializedName("file_size")
    val fileSize: Long,
    
    @SerializedName("added_at")
    val addedAt: String
) {
    /**
     * 获取流媒体 URL
     */
    fun getStreamUrl(baseUrl: String): String {
        return "$baseUrl/api/stream/$id"
    }
    
    /**
     * 格式化时长为 mm:ss 格式
     */
    fun getFormattedDuration(): String {
        if (duration <= 0) return "--:--"
        val minutes = duration / 60
        val seconds = duration % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
    
    /**
     * 格式化文件大小
     */
    fun getFormattedFileSize(): String {
        if (fileSize <= 0) return "未知"
        
        val kb = fileSize / 1024.0
        val mb = kb / 1024.0
        
        return when {
            mb >= 1.0 -> String.format("%.2f MB", mb)
            kb >= 1.0 -> String.format("%.2f KB", kb)
            else -> "$fileSize B"
        }
    }
}

/**
 * 播放列表响应
 */
data class PlaylistResponse(
    @SerializedName("songs")
    val songs: List<Song>,
    
    @SerializedName("total")
    val total: Int
)