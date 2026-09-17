/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/network/src/main/kotlin/com/kr/reader/core/network/UAProvider.kt
 * @Description: User-Agent 池：多 UA chakra 版本车站返还 PC 版与移动版的连环策略
 */
package com.kr.reader.core.network

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class UAProvider @Inject constructor() {

    /**
     * UA 池里的版本刻意拉到当前主流版本：
     * 部分书站会对低版本浏览器 UA 返回「请升级浏览器」捞作弊页。
     */
    private val uaPool: List<String> = listOf(
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36",
        "Mozilla/5.0 (iPhone; CPU iPhone OS 17_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.4 Mobile/15E148 Safari/604.1",
    )

    /** 按域名粘性选择 UA：同一站点每次用同一个 UA，避免被识别为爬虫 */
    fun forDomain(domain: String): String {
        val index = kotlin.math.abs(domain.hashCode()) % uaPool.size
        return uaPool[index]
    }

    fun random(): String = uaPool[Random.nextInt(uaPool.size)]
}
