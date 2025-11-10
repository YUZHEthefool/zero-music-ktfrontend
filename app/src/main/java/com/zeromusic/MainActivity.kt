package com.zeromusic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.zeromusic.ui.player.FullPlayerScreen
import com.zeromusic.ui.player.MiniPlayerBar
import com.zeromusic.ui.playlist.PlaylistScreen
import com.zeromusic.ui.theme.ZeroMusicTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Zero Music 主 Activity
 * 使用 Jetpack Compose 构建 UI
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            ZeroMusicTheme {
                var showFullPlayer by remember { mutableStateOf(false) }
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // 主界面 - 播放列表
                        if (!showFullPlayer) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                // 播放列表占据大部分空间
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                ) {
                                    PlaylistScreen()
                                }
                                
                                // 底部迷你播放器
                                MiniPlayerBar(
                                    onClick = { showFullPlayer = true }
                                )
                            }
                        }
                        
                        // 全屏播放器
                        if (showFullPlayer) {
                            FullPlayerScreen(
                                onClose = { showFullPlayer = false }
                            )
                        }
                    }
                }
            }
        }
    }
}