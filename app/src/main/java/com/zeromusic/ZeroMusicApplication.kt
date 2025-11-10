package com.zeromusic

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Zero Music Application 类
 * 使用 Hilt 进行依赖注入
 */
@HiltAndroidApp
class ZeroMusicApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // 应用初始化逻辑
    }
}