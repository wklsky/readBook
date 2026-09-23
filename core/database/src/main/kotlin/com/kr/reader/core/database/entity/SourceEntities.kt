/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/database/src/main/kotlin/com/kr/reader/core/database/entity/SourceEntities.kt
 * @Description: book_sources 与 chapter_cache 表实体：rules 以 JSON 字符串落库，SourceJsonCodec 负责解码
 */
package com.kr.reader.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "book_sources")
data class BookSourceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val groupName: String = "默认",
    val baseUrl: String,
    val enabled: Boolean = true,
    val sortOrder: Int = 0,
    val charset: String = "UTF-8",
    val customUserAgent: String? = null,
    val timeoutMs: Int = 15000,
    val needCookie: Boolean = false,
    val health: String = "UNKNOWN",
    val failCount: Int = 0,
    val lastSuccessAt: Long = 0L,
    val lastCheckedAt: Long = 0L,
    val avgLatencyMs: Int = 0,
    val createdAt: Long = 0L,
    /** SourceRules 的 JSON 序列化结果；之所以不用额外表：规则整体读写，无独立查询需求 */
    val rulesJson: String = "",
)

@Entity(
    tableName = "chapter_cache",
    indices = [
        Index(value = ["cached_at"]),
        // (bookId, index) 必须唯一：缺这个索引时重复缓存同一章会插入多行，
        // 而读取走的是 LIMIT 1，拿到的永远是第一次缓存的旧内容，「更新缓存」形同失效
        Index(value = ["bookId", "index"], unique = true),
    ],
)
data class ChapterCacheEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val index: Int,
    val sourceId: Long,
    val sourceUrl: String,
    val title: String,
    val content: String,
    val charCount: Int,
    val cachedAt: Long,
    val lastAccessedAt: Long,
)
