package com.zeromusic.ui.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeromusic.data.model.Song
import com.zeromusic.data.repository.MusicRepository
import com.zeromusic.util.ErrorHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 播放列表 UI 状态
 */
sealed class PlaylistUiState {
    object Loading : PlaylistUiState()
    data class Success(val songs: List<Song>) : PlaylistUiState()
    data class Error(val message: String) : PlaylistUiState()
}

/**
 * 播放列表 ViewModel
 * 管理播放列表的数据和状态
 */
@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val musicRepository: MusicRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<PlaylistUiState>(PlaylistUiState.Loading)
    val uiState: StateFlow<PlaylistUiState> = _uiState.asStateFlow()
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    init {
        loadSongs()
    }
    
    /**
     * 加载歌曲列表
     */
    fun loadSongs() {
        viewModelScope.launch {
            _uiState.value = PlaylistUiState.Loading
            
            musicRepository.getAllSongs().collect { result ->
                result.fold(
                    onSuccess = { response ->
                        _uiState.value = PlaylistUiState.Success(response.songs)
                    },
                    onFailure = { exception ->
                        val errorMessage = ErrorHandler.getErrorMessage(exception)
                        _uiState.value = PlaylistUiState.Error(errorMessage)
                    }
                )
            }
        }
    }
    
    /**
     * 刷新歌曲列表
     */
    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            
            musicRepository.getAllSongs().collect { result ->
                result.fold(
                    onSuccess = { response ->
                        _uiState.value = PlaylistUiState.Success(response.songs)
                    },
                    onFailure = { exception ->
                        val errorMessage = ErrorHandler.getErrorMessage(exception)
                        _uiState.value = PlaylistUiState.Error(errorMessage)
                    }
                )
                _isRefreshing.value = false
            }
        }
    }
    
    /**
     * 重试加载
     */
    fun retry() {
        loadSongs()
    }
}