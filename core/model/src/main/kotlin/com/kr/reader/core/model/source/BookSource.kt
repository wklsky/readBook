/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/model/src/main/kotlin/com/kr/reader/core/model/source/BookSource.kt
 * @Description: 书源模型与规则集。规则以嵌套数据类描述 CSS-like 取选择器，支持 JsonPath/Regex/XPath 三类提取语法
 */
package com.kr.reader.core.model.source

import com.kr.reader.core.model.SourceHealth
import kotlinx.serialization.Serializable

/**
 * 内容提取规则：三类语法互斥
 * - "#id" / ".class" / "index siblings"：Jsoup 选择器
 * - "\$1-5 / \$.data.list[\$]"：JsonPath / 数组下标
 * - "regex:pattern\$1"：正则提取组
 */
@Serializable
data class ContentRule(
    val rule: String,
    /** 正则/DOM 提取后置处理串，如 "trim|unescape" */
    val processing: String = "",
)

@Serializable
data class UrlRule(
    val pattern: String,
    /** 是否需要在 URL 前拼 source baseUrl */
    val absolute: Boolean = true,
    val nextSuffix: String = "",
    val nextEnabled: Boolean = false,
    /** 翻页一次最多取多少条目录 */
    val nextMaxPage: Int = 10,
)

@Serializable
data class SearchRule(
    val url: String,
    val method: String = "GET",
    val body: String = "",
    val headers: Map<String, String> = emptyMap(),
    /** 搜索结果列表容器选择器；为空表示整页作为列表 */
    val list: String = "",
    val name: ContentRule? = null,
    val author: ContentRule? = null,
    val cover: ContentRule? = null,
    val intro: ContentRule? = null,
    val detailUrl: ContentRule? = null,
    val latestChapter: ContentRule? = null,
    /** 下一页按钮选择器 */
    val nextPage: String = "",
)

@Serializable
data class DetailRule(
    val title: ContentRule? = null,
    val author: ContentRule? = null,
    val cover: ContentRule? = null,
    val intro: ContentRule? = null,
    val catalog: UrlRule? = null,
    val latestChapter: ContentRule? = null,
)

@Serializable
data class CatalogRule(
    val list: String = "",
    val name: ContentRule? = null,
    val url: ContentRule? = null,
    /** 目录是否倒序（部分站点最新章在前） */
    val reverse: Boolean = false,
    val volumePattern: String = "",
    val nextPage: String = "",
)

@Serializable
data class SourceRules(
    val search: SearchRule,
    val detail: DetailRule,
    val catalog: CatalogRule,
    val content: ContentRule,
    /** 站点 headers/cookies 补充 */
    val headers: Map<String, String> = emptyMap(),
    val variables: Map<String, String> = emptyMap(),
)

@Serializable
data class BookSource(
    val id: Long = 0,
    val name: String,
    val groupName: String = "默认",
    val baseUrl: String,
    val enabled: Boolean = true,
    val sortOrder: Int = 0,
    val charset: String = "UTF-8",
    /** 为空时由 network 模块按域演进递补 UA */
    val customUserAgent: String? = null,
    val timeoutMs: Int = 15000,
    val needCookie: Boolean = false,
    val health: SourceHealth = SourceHealth.UNKNOWN,
    val failCount: Int = 0,
    val lastSuccessAt: Long = 0L,
    val lastCheckedAt: Long = 0L,
    val avgLatencyMs: Int = 0,
    val createdAt: Long = 0L,
    val rules: SourceRules,
) {
    /** 健康分：延迟越低分越高，连续失败会显著扣分，用于聚合结果排序 */
    val healthScore: Int
        get() {
            val latencyPenalty = (avgLatencyMs / 1000).coerceAtMost(30)
            val failurePenalty = failCount * 10
            return (100 - latencyPenalty - failurePenalty).coerceAtLeast(0)
        }
}
