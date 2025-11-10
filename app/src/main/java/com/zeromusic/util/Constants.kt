package com.zeromusic.util

/**
 * 应用常量
 */
object Constants {
    /**
     * 后端 API 基础 URL
     * 注意: 
     * - 在模拟器中访问本机服务器使用 10.0.2.2
     * - 在真机中使用实际的 IP 地址 (如 192.168.x.x)
     * - 端口号需要与后端配置一致 (默认 8080)
     */
    const val BASE_URL = "http://10.0.2.2:8080"
    
    /**
     * 网络请求超时时间 (秒)
     */
    const val NETWORK_TIMEOUT = 30L
    
    /**
     * 数据库名称
     */
    const val DATABASE_NAME = "zero_music.db"
}