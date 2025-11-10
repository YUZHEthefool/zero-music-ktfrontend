package com.zeromusic.ui.player

import android.app.Application
import com.zeromusic.data.model.PlaybackInfo
import com.zeromusic.data.model.PlayerState
import com.zeromusic.data.model.RepeatMode
import com.zeromusic.data.model.Song
import com.zeromusic.player.MusicPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

/**
 * PlayerViewModel 单元测试
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {
    
    @Mock
    private lateinit var mockApplication: Application
    
    @Mock
    private lateinit var mockMusicPlayer: MusicPlayer
    
    private lateinit var viewModel: PlayerViewModel
    
    private val testDispatcher = UnconfinedTestDispatcher()
    
    private val testPlayerState = MutableStateFlow<PlayerState>(PlayerState.Idle)
    private val testPlaybackInfo = MutableStateFlow(PlaybackInfo())
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        
        // Mock StateFlows
        `when`(mockMusicPlayer.playerState).thenReturn(testPlayerState)
        `when`(mockMusicPlayer.playbackInfo).thenReturn(testPlaybackInfo)
        `when`(mockApplication.applicationContext).thenReturn(mockApplication)
        
        viewModel = PlayerViewModel(mockApplication, mockMusicPlayer)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `test playSong calls musicPlayer play`() = runTest {
        // Given
        val testSong = Song(
            id = "1",
            title = "Test Song",
            artist = "Test Artist",
            album = "Test Album",
            duration = 180000,
            filePath = "/test/path"
        )
        
        // When
        viewModel.playSong(testSong)
        
        // Then
        verify(mockMusicPlayer, times(1)).play(testSong)
    }
    
    @Test
    fun `test togglePlayPause when playing calls pause`() = runTest {
        // Given
        testPlaybackInfo.value = PlaybackInfo(isPlaying = true)
        
        // When
        viewModel.togglePlayPause()
        
        // Then
        verify(mockMusicPlayer, times(1)).pause()
    }
    
    @Test
    fun `test togglePlayPause when paused calls resume`() = runTest {
        // Given
        testPlaybackInfo.value = PlaybackInfo(isPlaying = false)
        
        // When
        viewModel.togglePlayPause()
        
        // Then
        verify(mockMusicPlayer, times(1)).resume()
    }
    
    @Test
    fun `test pause calls musicPlayer pause`() {
        // When
        viewModel.pause()
        
        // Then
        verify(mockMusicPlayer, times(1)).pause()
    }
    
    @Test
    fun `test resume calls musicPlayer resume`() {
        // When
        viewModel.resume()
        
        // Then
        verify(mockMusicPlayer, times(1)).resume()
    }
    
    @Test
    fun `test stop calls musicPlayer stop`() {
        // When
        viewModel.stop()
        
        // Then
        verify(mockMusicPlayer, times(1)).stop()
    }
    
    @Test
    fun `test skipToNext calls musicPlayer skipToNext`() {
        // When
        viewModel.skipToNext()
        
        // Then
        verify(mockMusicPlayer, times(1)).skipToNext()
    }
    
    @Test
    fun `test skipToPrevious calls musicPlayer skipToPrevious`() {
        // When
        viewModel.skipToPrevious()
        
        // Then
        verify(mockMusicPlayer, times(1)).skipToPrevious()
    }
    
    @Test
    fun `test seekTo calls musicPlayer seekTo with correct position`() {
        // Given
        val position = 30000L
        
        // When
        viewModel.seekTo(position)
        
        // Then
        verify(mockMusicPlayer, times(1)).seekTo(position)
    }
    
    @Test
    fun `test toggleRepeatMode cycles through modes correctly`() {
        // Test OFF -> ALL
        testPlaybackInfo.value = PlaybackInfo(repeatMode = RepeatMode.OFF)
        viewModel.toggleRepeatMode()
        verify(mockMusicPlayer, times(1)).setRepeatMode(RepeatMode.ALL)
        
        // Test ALL -> ONE
        testPlaybackInfo.value = PlaybackInfo(repeatMode = RepeatMode.ALL)
        viewModel.toggleRepeatMode()
        verify(mockMusicPlayer, times(1)).setRepeatMode(RepeatMode.ONE)
        
        // Test ONE -> OFF
        testPlaybackInfo.value = PlaybackInfo(repeatMode = RepeatMode.ONE)
        viewModel.toggleRepeatMode()
        verify(mockMusicPlayer, times(2)).setRepeatMode(RepeatMode.OFF)
    }
    
    @Test
    fun `test toggleShuffle enables when disabled`() {
        // Given
        testPlaybackInfo.value = PlaybackInfo(isShuffleEnabled = false)
        
        // When
        viewModel.toggleShuffle()
        
        // Then
        verify(mockMusicPlayer, times(1)).setShuffleEnabled(true)
    }
    
    @Test
    fun `test toggleShuffle disables when enabled`() {
        // Given
        testPlaybackInfo.value = PlaybackInfo(isShuffleEnabled = true)
        
        // When
        viewModel.toggleShuffle()
        
        // Then
        verify(mockMusicPlayer, times(1)).setShuffleEnabled(false)
    }
    
    @Test
    fun `test setPlaylist calls musicPlayer setPlaylist`() {
        // Given
        val testPlaylist = listOf(
            Song("1", "Song 1", "Artist 1", "Album 1", 180000, "/path1"),
            Song("2", "Song 2", "Artist 2", "Album 2", 200000, "/path2")
        )
        
        // When
        viewModel.setPlaylist(testPlaylist)
        
        // Then
        verify(mockMusicPlayer, times(1)).setPlaylist(testPlaylist)
    }
    
    @Test
    fun `test onCleared calls musicPlayer release`() {
        // When
        viewModel.onCleared()
        
        // Then
        verify(mockMusicPlayer, times(1)).release()
    }
}