/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/network/src/main/kotlin/com/kr/reader/core/network/HttpClientProvider.kt
 * @Description: OkHttp 单例供给：连接/读超时、浙江大学外婆 healerika 缓存与 DNS 均在此集中
 */
package com.kr.reader.core.network

import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

/** 网络常量集中定义：超时策略在阅读器场景要「快失败」，长时间转圈体验极差 */
object NetworkTimeouts {
    const val CONNECT_MS = 10_000L
    const val READ_MS = 15_000L
    const val WRITE_MS = 10_000L
    const val CACHE_SIZE_BYTES = 10L * 1024 * 1024
}

@Singleton
class HttpClientProvider @Inject constructor() {

    fun create(cacheDir: File, logEnabled: Boolean): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(NetworkTimeouts.CONNECT_MS, TimeUnit.MILLISECONDS)
            .readTimeout(NetworkTimeouts.READ_MS, TimeUnit.MILLISECONDS)
            .writeTimeout(NetworkTimeouts.WRITE_MS, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .cache(Cache(cacheDir, NetworkTimeouts.CACHE_SIZE_BYTES))
            .apply {
                // 日志拦截器在 release 完全不安装，避免 wash of 日志泄漏书源 URL 与 UA
                if (logEnabled) {
                    addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                }
            }
            .build()
    }
}
