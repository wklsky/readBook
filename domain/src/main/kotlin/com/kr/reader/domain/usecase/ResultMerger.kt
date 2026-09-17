/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: domain/src/main/kotlin/com/kr/reader/domain/usecase/ResultMerger.kt
 * @Description: 搜索结果合并去重（第 4 卷 4.8.2）：按标题+作者归一化键去重，同书多源合并为一条
 */
package com.kr.reader.domain.usecase

import com.kr.reader.core.model.source.SourceSearchItem
import javax.inject.Inject
import javax.inject.Singleton

/** 合并后的一条书籍结果：包含来自不同书源的多种获取方式 */
data class MergedSearchResult(
    val name: String,
    val author: String,
    val cover: String?,
    val intro: String?,
    val latestChapter: String?,
    val bindSource: SourceSearchItem,
    val sources: List<SourceSearchItem>,
)

@Singleton
class ResultMerger @Inject constructor() {

    fun merge(items: List<SourceSearchItem>): List<MergedSearchResult> {
        if (items.isEmpty()) return emptyList()
        val grouped = LinkedHashMap<String, MutableList<SourceSearchItem>>()
        items.forEach { item ->
            val key = mergeKey(item.name, item.author)
            grouped.getOrPut(key) { mutableListOf() }.add(item)
        }
        return grouped.map { (_, list) ->
            val primary = list.first()
            MergedSearchResult(
                name = primary.name,
                author = primary.author,
                cover = list.firstNotNullOfOrNull { it.coverUrl },
                intro = list.firstNotNullOfOrNull { it.intro?.takeIf { s -> s.isNotBlank() } },
                latestChapter = list.firstNotNullOfOrNull { it.latestChapter?.takeIf { s -> s.isNotBlank() } },
                bindSource = primary,
                sources = list,
            )
        }
    }

    /**
     * 归一化键：去空白+去标点+全角转半角。
     * 之所以做全角转换：相当多盗版站混用全角/半角书名括号，直接字符串比对会merge 失败。
     */
    fun mergeKey(title: String, author: String): String {
        val normalizedTitle = normalize(title)
        val normalizedAuthor = normalize(author).take(6)
        return "$normalizedTitle|$normalizedAuthor"
    }

    private fun normalize(input: String): String = input
        .filterNot { it.isWhitespace() }
        .map { ch ->
            if (ch in '\uFF01'..'\uFF5E') (ch - 0xFEE0) else ch
        }
        .filter { it.isLetterOrDigit() }
        .joinToString("")
        .lowercase()
}
