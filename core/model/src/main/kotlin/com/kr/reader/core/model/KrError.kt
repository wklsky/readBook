/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/model/src/main/kotlin/com/kr/reader/core/model/KrError.kt
 * @Description: 分层错误处理契约（第 2 卷 2.5）：数据层抛含 type 的 AppError，UI 按 type 差异化提示与重试
 */
package com.kr.reader.core.model

sealed interface AppError {

    /** 面向用户的话术，必须可直接展示，禁止日志式英文原文 */
    val userMessage: String

    /** 是否允许用户重试（网络类通常可重试，文件损坏不可） */
    val recoverable: Boolean

    /** 上报埋点用的稳定标识，不随文案变化 */
    val errorType: String
}

data class FileNotFound(override val userMessage: String = "无法找到书籍文件") : AppError {
    override val recoverable: Boolean = true
    override val errorType: String get() = "file_not_found"
    override fun toString(): String = "FileNotFound(message=$userMessage)"
}

data class EncodingDetectionFailed(
    override val userMessage: String = "无法识别文件编码类型",
) : AppError {
    override val recoverable: Boolean = true
    override val errorType: String get() = "encoding_failed"
    override fun toString(): String = "EncodingDetectionFailed(message=$userMessage)"
}

data class CorruptFile(override val userMessage: String = "文件已损坏") : AppError {
    override val recoverable: Boolean = false
    override val errorType: String get() = "corrupt_file"
    override fun toString(): String = "CorruptFile(message=$userMessage)"
}

data class NetworkFailed(override val userMessage: String = "网络连接失败") : AppError {
    override val recoverable: Boolean = true
    override val errorType: String get() = "network_failed"
    override fun toString(): String = "NetworkFailed(message=$userMessage)"
}

data class Timeout(override val userMessage: String = "加载超时") : AppError {
    override val recoverable: Boolean = true
    override val errorType: String get() = "timeout"
    override fun toString(): String = "Timeout(message=$userMessage)"
}

data class ParseFailed(override val userMessage: String = "内容解析失败") : AppError {
    override val recoverable: Boolean = false
    override val errorType: String get() = "parse_failed"
    override fun toString(): String = "ParseFailed(message=$userMessage)"
}

data class SourceRuleError(override val userMessage: String = "书源规则失效") : AppError {
    override val recoverable: Boolean = true
    override val errorType: String = "source_rule_error"
    override fun toString(): String = "SourceRuleError(message=$userMessage)"
}

data class StorageFull(override val userMessage: String = "存储空间不足") : AppError {
    override val recoverable: Boolean = false
    override val errorType: String = "storage_full"
    override fun toString(): String = "StorageFull(message=$userMessage)"
}

data class UnknownAppError(override val userMessage: String = "发生未知错误") : AppError {
    override val recoverable: Boolean = true
    override val errorType: String get() = "unknown"
    override fun toString(): String = "UnknownAppError(message=$userMessage)"
}

/**
 * 从任意 Throwable 降级出可展示错误。
 * 之所以集中处理：网络层、解析层、IO 层抛出的异常类型各异，
 * 统一在此收敛可避免 UI 层出现 switch-if 链条。
 */
fun Throwable.toAppError(): AppError = when (this) {
    is AppError -> this
    is java.io.FileNotFoundException -> FileNotFound()
    is java.net.SocketTimeoutException -> Timeout()
    is java.io.IOException -> NetworkFailed()
    is OutOfMemoryError -> StorageFull()
    else -> UnknownAppError()
}

/** 日志用简短摘要，避免把整段堆栈写进 logcat 造成 I/O 抖动 */
fun Throwable.logSummary(): String = "${javaClass.simpleName}: ${message.orEmpty()}"
