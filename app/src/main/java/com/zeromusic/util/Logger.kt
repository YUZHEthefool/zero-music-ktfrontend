package com.zeromusic.util

import android.util.Log

/**
 * 日志工具类
 * 统一管理应用日志输出
 */
object Logger {
    
    private const val TAG = "ZeroMusic"
    private var isDebugMode = true
    
    /**
     * 设置调试模式
     */
    fun setDebugMode(enabled: Boolean) {
        isDebugMode = enabled
    }
    
    /**
     * Debug 级别日志
     */
    fun d(message: String, tag: String = TAG) {
        if (isDebugMode) {
            Log.d(tag, message)
        }
    }
    
    /**
     * Info 级别日志
     */
    fun i(message: String, tag: String = TAG) {
        if (isDebugMode) {
            Log.i(tag, message)
        }
    }
    
    /**
     * Warning 级别日志
     */
    fun w(message: String, tag: String = TAG) {
        Log.w(tag, message)
    }
    
    /**
     * Error 级别日志
     */
    fun e(message: String, throwable: Throwable? = null, tag: String = TAG) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }
    
    /**
     * 网络请求日志
     */
    fun network(method: String, url: String, response: String? = null) {
        if (isDebugMode) {
            Log.d("${TAG}_Network", "$method $url ${response?.let { "-> $it" } ?: ""}")
        }
    }
    
    /**
     * 播放器事件日志
     */
    fun player(event: String, details: String = "") {
        if (isDebugMode) {
            Log.d("${TAG}_Player", "$event ${if (details.isNotEmpty()) "- $details" else ""}")
        }
    }
}