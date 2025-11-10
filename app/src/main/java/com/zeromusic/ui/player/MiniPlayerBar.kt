package com.zeromusic.ui.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zeromusic.data.model.PlayerState

/**
 * 迷你播放器条
 * 显示在屏幕底部的小型播放控制器
 */
@Composable
fun MiniPlayerBar(
    viewModel: PlayerViewModel = hiltViewModel(),
    onClick: () -> Unit = {}
) {
    val playbackInfo by viewModel.playbackInfo.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    
    // 如果没有正在播放的歌曲,不显示
    if (playbackInfo.currentSong == null) {
        return
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable(onClick = onClick),
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 歌曲信息
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = playbackInfo.currentSong?.title ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = playbackInfo.currentSong?.artist ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // 播放/暂停按钮
            IconButton(
                onClick = { viewModel.togglePlayPause() },
                enabled = playerState !is PlayerState.Error
            ) {
                Icon(
                    imageVector = if (playbackInfo.isPlaying) {
                        Icons.Default.Pause
                    } else {
                        Icons.Default.PlayArrow
                    },
                    contentDescription = if (playbackInfo.isPlaying) "暂停" else "播放",
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // 下一曲按钮
            IconButton(
                onClick = { viewModel.skipToNext() },
                enabled = playerState !is PlayerState.Error
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "下一曲"
                )
            }
        }
        
        // 播放进度指示器
        if (playerState is PlayerState.Buffering) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
            )
        } else {
            LinearProgressIndicator(
                progress = { playbackInfo.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
            )
        }
    }
}