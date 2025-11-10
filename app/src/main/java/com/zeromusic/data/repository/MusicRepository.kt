package com.zeromusic.data.repository

import com.zeromusic.data.model.PlaylistResponse
import com.zeromusic.data.model.Song
import com.zeromusic.data.remote.ApiService
import com.zeromusic.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 音乐数据仓库
 * 负责管理音乐数据的获取和缓存
 */
@Singleton
class MusicRepository @Inject constructor(
    private val apiService: ApiService
) {
    
    /**
     * 获取所有歌曲列表
     * @return Flow<Result<PlaylistResponse>>
     */
    fun getAllSongs(): Flow<Result<PlaylistResponse>> = flow {
        try {
            Logger.network("GET", "/api/songs")
            val response = apiService.getAllSongs()
            if (response.isSuccessful && response.body() != null) {
                val songCount = response.body()!!.songs.size
                Logger.i("Successfully loaded $songCount songs")
                emit(Result.success(response.body()!!))
            } else {
                Logger.e("Failed to load songs: ${response.message()}")
                emit(Result.failure(Exception("获取歌曲列表失败: ${response.message()}")))
            }
        } catch (e: Exception) {
            Logger.e("Error loading songs", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * 根据 ID 获取单首歌曲
     * @param songId 歌曲 ID
     * @return Flow<Result<Song>>
     */
    fun getSongById(songId: String): Flow<Result<Song>> = flow {
        try {
            Logger.network("GET", "/api/song/$songId")
            val response = apiService.getSongById(songId)
            if (response.isSuccessful && response.body() != null) {
                val song = response.body()!!
                Logger.i("Successfully loaded song: ${song.title}")
                emit(Result.success(song))
            } else {
                Logger.e("Failed to load song $songId: ${response.message()}")
                emit(Result.failure(Exception("获取歌曲详情失败: ${response.message()}")))
            }
        } catch (e: Exception) {
            Logger.e("Error loading song $songId", e)
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}