/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-23 10:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-23 10:00
 * @FilePath: data/sources/src/main/kotlin/com/kr/reader/data/sources/SourceMappers.kt
 * @Description: 书源实体 ↔ 领域模型映射：rules 以 JSON 列存储，解码失败必须降级为「空规则」而不是整行丢失
 */
package com.kr.reader.data.sources

import android.util.Log
import com.kr.reader.core.database.entity.BookSourceEntity
import com.kr.reader.core.model.SourceHealth
import com.kr.reader.core.model.source.BookSource
import com.kr.reader.core.model.source.CatalogRule
import com.kr.reader.core.model.source.ContentRule
import com.kr.reader.core.model.source.DetailRule
import com.kr.reader.core.model.source.SearchRule
import com.kr.reader.core.model.source.SourceRules
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val TAG = "SourceMappers"

/** 规则列编解码独立配置：规则来自用户导入，字段可能缺失，必须宽松解析 */
private val rulesCodec = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = false
    prettyPrint = false
}

/**
 * 规则损坏时的兜底。
 * 之所以不直接丢掉这一行：书源是用户手工维护的资产，
 * 让它继续出现在管理页，用户才有机会「重新导入覆盖」把规则修回来。
 */
private val EMPTY_RULES = SourceRules(
    search = SearchRule(url = ""),
    detail = DetailRule(),
    catalog = CatalogRule(),
    content = ContentRule(rule = ""),
)

fun BookSourceEntity.toModel(): BookSource = BookSource(
    id = id,
    name = name,
    groupName = groupName,
    baseUrl = baseUrl,
    enabled = enabled,
    sortOrder = sortOrder,
    charset = charset,
    customUserAgent = customUserAgent,
    timeoutMs = timeoutMs,
    needCookie = needCookie,
    health = runCatching { SourceHealth.valueOf(health) }.getOrDefault(SourceHealth.UNKNOWN),
    failCount = failCount,
    lastSuccessAt = lastSuccessAt,
    lastCheckedAt = lastCheckedAt,
    avgLatencyMs = avgLatencyMs,
    createdAt = createdAt,
    rules = decodeRules(rulesJson),
)

fun BookSource.toEntity(): BookSourceEntity = BookSourceEntity(
    id = id,
    name = name,
    groupName = groupName,
    baseUrl = baseUrl,
    enabled = enabled,
    sortOrder = sortOrder,
    charset = charset.ifBlank { "UTF-8" },
    customUserAgent = customUserAgent,
    timeoutMs = timeoutMs,
    needCookie = needCookie,
    health = health.name,
    failCount = failCount,
    lastSuccessAt = lastSuccessAt,
    lastCheckedAt = lastCheckedAt,
    avgLatencyMs = avgLatencyMs,
    createdAt = if (createdAt == 0L) System.currentTimeMillis() else createdAt,
    rulesJson = encodeRules(rules),
)

private fun decodeRules(json: String): SourceRules {
    if (json.isBlank()) return EMPTY_RULES
    return runCatching { rulesCodec.decodeFromString<SourceRules>(json) }
        .getOrElse {
            Log.w(TAG, "书源规则解析失败：${it.message}")
            EMPTY_RULES
        }
}

private fun encodeRules(rules: SourceRules): String = runCatching {
    rulesCodec.encodeToString(rules)
}.getOrDefault("")
