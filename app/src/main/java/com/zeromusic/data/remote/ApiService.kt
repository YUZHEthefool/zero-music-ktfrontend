package com.zeromusic.data.remote

import com.zeromusic.data.model.PlaylistResponse
import com.zeromusic.data.model.Song
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Zero Music API 服务接口
 * 定义所有与后端交互的 API 端点
 */
interface ApiService {
    
    /**
     * 获取所有歌曲列表
     * GET /api/songs
     */
    @GET("/api/songs")
    suspend fun getAllSongs(): Response<PlaylistResponse>
    
    /**
     * 根据 ID 获取单首歌曲信息
     * GET /api/song/{id}
     */
    @GET("/api/song/{id}")
    suspend fun getSongById(@Path("id") songId: String): Response<Song>
    
    /**
     * 注意: 音频流端点不在这里定义
     * 音频流使用直接的 URL: /api/stream/{id}
     * ExoPlayer 会直接使用这个 URL 进行流式播放
     */
}