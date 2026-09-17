/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/common/src/main/kotlin/com/kr/reader/core/common/SafeCall.kt
 * @Description: 安全调用扩展（第 2 卷 2.4.2 铁律 3）：网络/文件/解析操作统一走 Result 包装，禁止在 UI 层 try-catch
 */
package com.kr.reader.core.common

import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * 挂起安全调用：协程取消必须重新抛出，否则 structured concurrency 会被破坏
 * （例如用户离开阅读页后 RealSourceEngine 的 fetch 还在写入 DbRepository）
 */
suspend inline fun <reified T : Any> suspendRunCatching(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (ce: CancellationException) {
    throw ce
} catch (t: Throwable) {
    Result.failure(t)
}

/** 不可取消的合成恢复辅助 */
fun <T> CancellableContinuation<T>.resumeSafely(value: T, onCancellation: CompletionHandler? = null) {
    resume(value, onCancellation)
}

inline fun <T> Result<T>.onFailureLog(block: (Throwable) -> Unit): Result<T> = apply {
    exceptionOrNull()?.let(block)
}
