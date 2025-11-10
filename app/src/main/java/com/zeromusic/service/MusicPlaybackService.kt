package com.zeromusic.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.zeromusic.MainActivity
import com.zeromusic.R
import com.zeromusic.player.MusicPlayer
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 音乐播放服务
 * 作为前台服务运行,支持后台播放和媒体通知
 */
@UnstableApi
@AndroidEntryPoint
class MusicPlaybackService : MediaSessionService() {
    
    @Inject
    lateinit var musicPlayer: MusicPlayer
    
    private var mediaSession: MediaSession? = null
    
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "music_playback_channel"
        private const val CHANNEL_NAME = "音乐播放"
        
        // 通知 Actions
        const val ACTION_PLAY = "com.zeromusic.action.PLAY"
        const val ACTION_PAUSE = "com.zeromusic.action.PAUSE"
        const val ACTION_NEXT = "com.zeromusic.action.NEXT"
        const val ACTION_PREVIOUS = "com.zeromusic.action.PREVIOUS"
        const val ACTION_STOP = "com.zeromusic.action.STOP"
        const val ACTION_UPDATE_NOTIFICATION = "UPDATE_NOTIFICATION"
    }
    
    private var isForegroundServiceStarted = false
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        if (mediaSession == null) {
            // 创建 MediaSession
            mediaSession = MediaSession.Builder(this, musicPlayer as androidx.media3.common.Player)
                .build()
        }
        return mediaSession
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> musicPlayer.resume()
            ACTION_PAUSE -> musicPlayer.pause()
            ACTION_NEXT -> musicPlayer.skipToNext()
            ACTION_PREVIOUS -> musicPlayer.skipToPrevious()
            ACTION_STOP -> {
                musicPlayer.stop()
                stopForegroundService()
            }
            ACTION_UPDATE_NOTIFICATION -> {
                val title = intent.getStringExtra("title") ?: ""
                val artist = intent.getStringExtra("artist") ?: ""
                val isPlaying = intent.getBooleanExtra("isPlaying", false)
                
                if (!isForegroundServiceStarted) {
                    startForegroundService(title, artist, isPlaying)
                    isForegroundServiceStarted = true
                } else {
                    updateNotification(title, artist, isPlaying)
                }
            }
        }
        
        return START_STICKY
    }
    
    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
    
    /**
     * 创建通知渠道 (Android 8.0+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "显示正在播放的音乐信息"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
    
    /**
     * 创建播放通知
     */
    fun createPlaybackNotification(
        title: String,
        artist: String,
        isPlaying: Boolean
    ): Notification {
        // 点击通知打开应用
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 上一曲
        val previousIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, MusicPlaybackService::class.java).apply {
                action = ACTION_PREVIOUS
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 播放/暂停
        val playPauseIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, MusicPlaybackService::class.java).apply {
                action = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 下一曲
        val nextIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, MusicPlaybackService::class.java).apply {
                action = ACTION_NEXT
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 停止
        val stopIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, MusicPlaybackService::class.java).apply {
                action = ACTION_STOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(artist)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentIntent(contentIntent)
            .setDeleteIntent(stopIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(isPlaying)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession?.sessionCompatToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .addAction(
                R.drawable.ic_skip_previous,
                "上一曲",
                previousIntent
            )
            .addAction(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow,
                if (isPlaying) "暂停" else "播放",
                playPauseIntent
            )
            .addAction(
                R.drawable.ic_skip_next,
                "下一曲",
                nextIntent
            )
            .build()
    }
    
    /**
     * 更新通知
     */
    fun updateNotification(title: String, artist: String, isPlaying: Boolean) {
        val notification = createPlaybackNotification(title, artist, isPlaying)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.notify(NOTIFICATION_ID, notification)
    }
    
    /**
     * 启动前台服务
     */
    fun startForegroundService(title: String, artist: String, isPlaying: Boolean) {
        val notification = createPlaybackNotification(title, artist, isPlaying)
        startForeground(NOTIFICATION_ID, notification)
    }
    
    /**
     * 停止前台服务
     */
    fun stopForegroundService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
}