package com.zeromusic.util

import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * 错误处理工具
 * 统一处理应用中的各种异常
 */
object ErrorHandler {
    
    /**
     * 处理异常并返回用户友好的错误消息
     */
    fun handleError(throwable: Throwable): ErrorResult {
        Logger.e("Error occurred", throwable)
        
        return when (throwable) {
            is UnknownHostException -> ErrorResult(
                message = "无法连接到服务器，请检查网络连接",
                type = ErrorType.NETWORK
            )
            
            is SocketTimeoutException -> ErrorResult(
                message = "连接超时，请稍后重试",
                type = ErrorType.TIMEOUT
            )
            
            is IOException -> ErrorResult(
                message = "网络错误，请检查您的连接",
                type = ErrorType.NETWORK
            )
            
            is HttpException -> {
                val code = throwable.code()
                when (code) {
                    400 -> ErrorResult(
                        message = "请求错误",
                        type = ErrorType.CLIENT_ERROR,
                        code = code
                    )
                    401 -> ErrorResult(
                        message = "未授权访问",
                        type = ErrorType.AUTH_ERROR,
                        code = code
                    )
                    403 -> ErrorResult(
                        message = "禁止访问",
                        type = ErrorType.AUTH_ERROR,
                        code = code
                    )
                    404 -> ErrorResult(
                        message = "请求的资源不存在",
                        type = ErrorType.CLIENT_ERROR,
                        code = code
                    )
                    500 -> ErrorResult(
                        message = "服务器错误",
                        type = ErrorType.SERVER_ERROR,
                        code = code
                    )
                    503 -> ErrorResult(
                        message = "服务暂时不可用",
                        type = ErrorType.SERVER_ERROR,
                        code = code
                    )
                    else -> ErrorResult(
                        message = "发生错误 (${code})",
                        type = ErrorType.UNKNOWN,
                        code = code
                    )
                }
            }
            
            else -> ErrorResult(
                message = throwable.message ?: "未知错误",
                type = ErrorType.UNKNOWN
            )
        }
    }
    
    /**
     * 获取错误的简短描述
     */
    fun getErrorMessage(throwable: Throwable): String {
        return handleError(throwable).message
    }
}

/**
 * 错误结果
 */
data class ErrorResult(
    val message: String,
    val type: ErrorType,
    val code: Int? = null
)

/**
 * 错误类型
 */
enum class ErrorType {
    NETWORK,        // 网络错误
    TIMEOUT,        // 超时
    AUTH_ERROR,     // 认证错误
    CLIENT_ERROR,   // 客户端错误 (4xx)
    SERVER_ERROR,   // 服务器错误 (5xx)
    UNKNOWN         // 未知错误
}