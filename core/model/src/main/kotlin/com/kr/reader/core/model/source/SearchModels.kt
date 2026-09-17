/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/model/src/main/kotlin/com/kr/reader/core/model/source/SearchModels.kt
 * @Description: 聚合搜索与目录-middle 模型：单源结果、来源差异、合并后的书籍视图
 */
package com.kr.reader.core.model.source

import kotlinx.serialization.Serializable

/** 单源搜索命中的一条书籍 */
@Serializable
data class SourceSearchItem(
    val name: String,
    val author: String = "",
    val coverUrl: String? = null,
    val intro: String? = null,
    /** 详情页 URL，也是该书在此源的稳定主键 */
    val detailUrl: String,
    val latestChapter: String? = null,
    val sourceId: Long,
    val sourceName: String,
)

/** 「同一本书在多个源」的差异 Audit Item（第 4 卷 4.8.3） */
@Serializable
data class SourceBookDiff(
    val sourceId: Long,
    val sourceName: String,
    val extraChapterCount: Int,
    val extraChapters: List<String>,
    val missingChapterCount: Int,
    val missingChapters: List<String>,
    val latestChapterTitle: String?,
    val latestUpdateMs: Long,
    val reliabilityScore: Int,
)

/** 同一本书在某源上的目录/详情描述 */
@Serializable
data class SourceBookRef(
    val sourceId: Long,
    val sourceName: String,
    val detailUrl: String,
    val latestChapter: String?,
    val reliability: Int,
    val totalChapters: Int,
)

/** 合并后的书籍视图，供详情页展示「同书多源」 */
@Serializable
data class MergedBook(
    val bookId: Long,
    val title: String,
    val author: String,
    val coverUrl: String?,
    val intro: String?,
    val sources: List<SourceBookRef>,
    /** 当前展示所选的源下标 */
    val selectedIndex: Int = 0,
    val latestChapterTitle: String?,
)

/** 网络书目录条目 */
@Serializable
data class CatalogItem(
    val title: String,
    val url: String,
    val isVolume: Boolean = false,
)
