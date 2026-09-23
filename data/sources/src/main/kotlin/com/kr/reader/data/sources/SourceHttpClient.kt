/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-23 10:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-23 10:00
 * @FilePath: data/sources/src/main/kotlin/com/kr/reader/data/sources/SourceHttpClient.kt
 * @Description: 书源 HTTP 抓取：按域限流 + UA 轮换 + Referer 伪装 + 失败退避（第 4 卷 4.7.3 五层反封禁的前四层）
 */
package com.kr.reader.data.sources

import android.content.Context
import com.kr.reader.core.common.IoDispatcher
import com.kr.reader.core.model.source.BookSource
import com.kr.reader.core.network.DomainRateLimiter
import com.kr.reader.core.network.HttpClientProvider
import com.kr.reader.core.network.UAProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.net.URL
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/** HTTP 状态码异常：上层据此区分「站点封禁」与「书源规则失效」 */
class SourceHttpException(val code: Int, url: String) : IOException("HTTP $code @ $url")

@Singleton
class SourceHttpClient @Inject constructor(
    private val provider: HttpClientProvider,
    private val rateLimiter: DomainRateLimiter,
    private val uaProvider: UAProvider,
    @ApplicationContext private val context: Context,
) {

    private val baseClient: OkHttpClient by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        provider.create(File(context.cacheDir, CACHE_DIR), logEnabled = false)
    }

    suspend fun fetch(
        source: BookSource,
        url: String,
        method: String = "GET",
        body: String? = null,
        headers: Map<String, String> = emptyMap(),
    ): String = withContext(IoDispatcher) {
        // 限流必须在发请求之前：聚合搜索会并发打同一站点，不限流极易触发风控
        rateLimiter.acquire(url)
        val client = clientFor(source)
        val request = buildRequest(source, url, method, body, headers)

        repeat(MAX_RETRY + 1) { attempt ->
            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        if (RETRYABLE_CODES.contains(response.code) && attempt < MAX_RETRY) {
                            delay(backoffMs(attempt))
                            return@repeat
                        }
                        throw SourceHttpException(response.code, url)
                    }
                    val bytes = response.body?.bytes() ?: throw IOException("响应体为空")
                    rateLimiter.markRequest(url)
                    return@withContext decode(bytes, source.charset)
                }
            } catch (e: IOException) {
                if (attempt >= MAX_RETRY) throw e
                delay(backoffMs(attempt))
            }
        }
        throw IOException("请求失败：$url")
    }

    /** 纯文本抓取（书源订阅 URL 导入用），不参与限流与重试的复杂策略 */
    suspend fun fetchPlain(url: String): String = withContext(IoDispatcher) {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", uaProvider.random())
            .build()
        baseClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw SourceHttpException(response.code, url)
            response.body?.string().orEmpty()
        }
    }

    /**
     * 每个书源声明的超时不同（有的站点慢到 30s 才响应），
     * 用 newBuilder 派生客户端而不是改造全局客户端，避免慢源拖累快源。
     */
    private fun clientFor(source: BookSource): OkHttpClient {
        val timeout = source.timeoutMs.takeIf { it > 0 } ?: return baseClient
        return baseClient.newBuilder()
            .readTimeout(timeout.toLong(), TimeUnit.MILLISECONDS)
            .callTimeout((timeout + CALL_TIMEOUT_SLACK_MS).toLong(), TimeUnit.MILLISECONDS)
            .build()
    }

    private fun buildRequest(
        source: BookSource,
        url: String,
        method: String,
        body: String?,
        headers: Map<String, String>,
    ): Request {
        val builder = Request.Builder().url(url)
        headers.forEach { (key, value) -> builder.header(key, value) }
        // UA 按域名粘性选取：同一站点固定用同一个 UA，频繁切换反而像爬虫
        builder.header("User-Agent", source.customUserAgent?.takeIf { it.isNotBlank() } ?: uaProvider.forDomain(hostOf(url)))
        builder.header("Accept-Language", "zh-CN,zh;q=0.9")
        builder.header("Referer", source.baseUrl)
        if (method.equals("POST", ignoreCase = true)) {
            builder.post((body ?: "").toRequestBody(FORM_MEDIA_TYPE))
        }
        return builder.build()
    }

    /**
     * 编码裁决顺序：BOM > HTML meta > 书源声明 > UTF-8。
     * 中文盗版站大量使用 GBK 且 meta 与实际不符，任何一步猜错都会导致整页乱码。
     */
    private fun decode(bytes: ByteArray, declaredCharset: String): String {
        val charsetName = bomCharset(bytes)?.name()
            ?: metaCharset(bytes)
            ?: declaredCharset.ifBlank { "UTF-8" }
        val charset = runCatching { Charset.forName(charsetName) }.getOrDefault(Charsets.UTF_8)
        return runCatching { String(bytes, charset) }.getOrDefault { String(bytes, Charsets.UTF_8) }
    }

    private fun bomCharset(bytes: ByteArray): Charset? {
        if (bytes.size >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()) {
            return Charsets.UTF_8
        }
        if (bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte()) return Charsets.UTF_16LE
        if (bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()) return Charsets.UTF_16BE
        return null
    }

    private fun metaCharset(bytes: ByteArray): String? {
        val head = String(bytes, 0, minOf(META_SCAN_BYTES, bytes.size), Charsets.ISO_8859_1)
        return META_CHARSET.find(head)?.groupValues?.get(1)
    }

    private fun hostOf(url: String): String = runCatching { URL(url).host }.getOrDefault(url)

    private fun backoffMs(attempt: Int): Long = BACKOFF_BASE_MS * (1 shl attempt)

    private companion object {
        const val CACHE_DIR = "http_source"
        const val MAX_RETRY = 2
        const val BACKOFF_BASE_MS = 1000L
        const val CALL_TIMEOUT_SLACK_MS = 5_000L
        const val META_SCAN_BYTES = 2048
        val RETRYABLE_CODES = intArrayOf(408, 425, 429, 500, 502, 503, 504)
        val META_CHARSET = Regex("""charset\s*=\s*["']?([\w-]+)""", RegexOption.IGNORE_CASE)
        val FORM_MEDIA_TYPE = "application/x-www-form-urlencoded; charset=utf-8".toMediaType()
    }
}
