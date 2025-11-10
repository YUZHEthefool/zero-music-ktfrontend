package com.zeromusic.di

import android.content.Context
import com.zeromusic.player.ExoPlayerMusicPlayer
import com.zeromusic.player.MusicPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 播放器依赖注入模块
 */
@Module
@InstallIn(SingletonComponent::class)
object PlayerModule {
    
    @Provides
    @Singleton
    fun provideMusicPlayer(
        @ApplicationContext context: Context
    ): MusicPlayer {
        return ExoPlayerMusicPlayer(context)
    }
}