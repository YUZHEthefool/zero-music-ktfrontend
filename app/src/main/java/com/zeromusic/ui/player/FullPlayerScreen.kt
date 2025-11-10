package com.zeromusic.ui.player

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zeromusic.data.model.PlayerState
import com.zeromusic.data.model.RepeatMode

/**
 * 全屏播放器界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerScreen(
    viewModel: PlayerViewModel = hiltViewModel(),
    onClose: () -> Unit = {}
) {
    val playbackInfo by viewModel.playbackInfo.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("正在播放") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "关闭"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 专辑封面区域
            AlbumArtSection(
                albumName = playbackInfo.currentSong?.album ?: "",
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 歌曲信息
            SongInfoSection(
                title = playbackInfo.currentSong?.title ?: "未播放",
                artist = playbackInfo.currentSong?.artist ?: "",
                album = playbackInfo.currentSong?.album ?: ""
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 进度条
            ProgressSection(
                currentPosition = playbackInfo.currentPosition,
                duration = playbackInfo.duration,
                onSeek = { position -> viewModel.seekTo(position) }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 播放控制按钮
            PlaybackControlsSection(
                isPlaying = playbackInfo.isPlaying,
                repeatMode = playbackInfo.repeatMode,
                isShuffleEnabled = playbackInfo.isShuffleEnabled,
                isEnabled = playerState !is PlayerState.Error,
                onPlayPauseClick = { viewModel.togglePlayPause() },
                onPreviousClick = { viewModel.skipToPrevious() },
                onNextClick = { viewModel.skipToNext() },
                onRepeatClick = { viewModel.toggleRepeatMode() },
                onShuffleClick = { viewModel.toggleShuffle() }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 错误信息
            if (playerState is PlayerState.Error) {
                Text(
                    text = (playerState as PlayerState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * 专辑封面区域
 */
@Composable
fun AlbumArtSection(
    albumName: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 4.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Album,
                contentDescription = albumName,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * 歌曲信息区域
 */
@Composable
fun SongInfoSection(
    title: String,
    artist: String,
    album: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = artist,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (album.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = album,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 进度条区域
 */
@Composable
fun ProgressSection(
    currentPosition: Long,
    duration: Long,
    onSeek: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = if (duration > 0) {
                (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
            } else {
                0f
            },
            onValueChange = { progress ->
                val newPosition = (progress * duration).toLong()
                onSeek(newPosition)
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(currentPosition),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatTime(duration),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 播放控制按钮区域
 */
@Composable
fun PlaybackControlsSection(
    isPlaying: Boolean,
    repeatMode: RepeatMode,
    isShuffleEnabled: Boolean,
    isEnabled: Boolean,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onShuffleClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 主要播放控制按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 上一曲按钮
            IconButton(
                onClick = onPreviousClick,
                enabled = isEnabled
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "上一曲",
                    modifier = Modifier.size(40.dp)
                )
            }
            
            // 播放/暂停按钮
            FilledIconButton(
                onClick = onPlayPauseClick,
                enabled = isEnabled,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) {
                        Icons.Default.Pause
                    } else {
                        Icons.Default.PlayArrow
                    },
                    contentDescription = if (isPlaying) "暂停" else "播放",
                    modifier = Modifier.size(40.dp)
                )
            }
            
            // 下一曲按钮
            IconButton(
                onClick = onNextClick,
                enabled = isEnabled
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "下一曲",
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 辅助控制按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 随机播放按钮
            IconButton(onClick = onShuffleClick) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "随机播放",
                    tint = if (isShuffleEnabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            
            Spacer(modifier = Modifier.width(48.dp))
            
            // 循环模式按钮
            IconButton(onClick = onRepeatClick) {
                Icon(
                    imageVector = when (repeatMode) {
                        RepeatMode.OFF -> Icons.Default.Repeat
                        RepeatMode.ALL -> Icons.Default.Repeat
                        RepeatMode.ONE -> Icons.Default.RepeatOne
                    },
                    contentDescription = "循环模式",
                    tint = if (repeatMode != RepeatMode.OFF) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

/**
 * 格式化时间 (毫秒转为 MM:SS)
 */
private fun formatTime(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}