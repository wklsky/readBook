/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/network/src/main/kotlin/com/kr/reader/core/network/DomainRateLimiter.kt
 * @Description: 域名压测器：同一主机最小请求间隔保护，防止书源站被我们打出 IP 封禁
 */
package com.kr.reader.core.network

import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import kotlinx.coroutines.delay

/** 默认每个域名最小请求间隔。 */
private const val DEFAULT_MIN_INTERVAL_MS = 200L

@Singleton
class DomainRateLimiter @Inject constructor() {

    private val lastRequestAt = ConcurrentHashMap<String, TimeMark>()

    suspend fun acquire(url: String) {
        val host = hostOf(url)
        val now = TimeSource.Monotonic.markNow()
        val last = lastRequestAt[host] ?: return
        val elapsedMs = last.elapsedNow().inWholeMilliseconds
        val wait = DEFAULT_MIN_INTERVAL_MS - elapsedMs
        if (wait > 0) delay(wait)
        lastRequestAt[host] = now
    }

    fun markRequest(url: String) {
        lastRequestAt[hostOf(url)] = TimeSource.Monotonic.markNow()
    }

    private fun hostOf(url: String): String = runCatching {
        java.net.URL(url).host
    }.getOrDefault(url)
}
