package com.zeromusic.util

import android.os.Build
import android.os.Debug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 性能监控工具
 * 用于监控应用性能指标
 */
object PerformanceMonitor {
    
    private val isMonitoring = AtomicBoolean(false)
    private val scope = CoroutineScope(Dispatchers.Default)
    
    /**
     * 启动性能监控
     */
    fun startMonitoring() {
        if (isMonitoring.compareAndSet(false, true)) {
            Logger.i("Performance monitoring started")
            scope.launch {
                while (isActive && isMonitoring.get()) {
                    logMemoryUsage()
                    delay(30000) // 每30秒记录一次
                }
            }
        }
    }
    
    /**
     * 停止性能监控
     */
    fun stopMonitoring() {
        if (isMonitoring.compareAndSet(true, false)) {
            Logger.i("Performance monitoring stopped")
        }
    }
    
    /**
     * 记录内存使用情况
     */
    private fun logMemoryUsage() {
        val runtime = Runtime.getRuntime()
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
        val maxMemory = runtime.maxMemory() / 1024 / 1024
        val totalMemory = runtime.totalMemory() / 1024 / 1024
        
        Logger.i("Memory: Used=${usedMemory}MB, Total=${totalMemory}MB, Max=${maxMemory}MB")
        
        // 如果内存使用超过80%,记录警告
        val memoryUsagePercent = (usedMemory.toFloat() / maxMemory * 100).toInt()
        if (memoryUsagePercent > 80) {
            Logger.w("High memory usage: ${memoryUsagePercent}%")
        }
    }
    
    /**
     * 记录方法执行时间
     */
    inline fun <T> measureTime(tag: String, block: () -> T): T {
        val startTime = System.currentTimeMillis()
        val result = block()
        val duration = System.currentTimeMillis() - startTime
        Logger.d("$tag took ${duration}ms")
        return result
    }
    
    /**
     * 获取当前内存信息
     */
    fun getMemoryInfo(): MemoryInfo {
        val runtime = Runtime.getRuntime()
        return MemoryInfo(
            usedMemoryMB = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024,
            totalMemoryMB = runtime.totalMemory() / 1024 / 1024,
            maxMemoryMB = runtime.maxMemory() / 1024 / 1024
        )
    }
    
    /**
     * 获取本地分配的内存大小
     */
    fun getNativeHeapSize(): Long {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Debug.getNativeHeapSize() / 1024 / 1024
        } else {
            0
        }
    }
    
    /**
     * 建议执行垃圾回收
     */
    fun suggestGC() {
        Logger.d("Suggesting garbage collection")
        System.gc()
    }
}

/**
 * 内存信息数据类
 */
data class MemoryInfo(
    val usedMemoryMB: Long,
    val totalMemoryMB: Long,
    val maxMemoryMB: Long
) {
    val usagePercent: Int
        get() = ((usedMemoryMB.toFloat() / maxMemoryMB) * 100).toInt()
    
    override fun toString(): String {
        return "Memory: $usedMemoryMB/$maxMemoryMB MB ($usagePercent%)"
    }
}