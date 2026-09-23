/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-23 10:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-23 10:00
 * @FilePath: data/sources/src/main/kotlin/com/kr/reader/data/sources/ChapterMatcher.kt
 * @Description: 换源章节匹配（第 4 卷 4.8.3）：标题精确 → 归一化 → 章序号 → 相对位置四级策略
 */
package com.kr.reader.data.sources

import com.kr.reader.core.model.Chapter
import com.kr.reader.core.model.source.CatalogItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChapterMatcher @Inject constructor() {

    /**
     * @return 下标 i 表示「旧目录第 i 章」在「新目录」中的下标，长度与 oldChapters 一致。
     * 换源后新目录章节数常常不同（有的源缺章、有的源多卷），因此最后一级必须按比例兜底，
     * 否则用户会被直接甩回第一章。
     */
    fun buildIndexMap(oldChapters: List<Chapter>, newItems: List<CatalogItem>): IntArray {
        if (newItems.isEmpty()) return IntArray(oldChapters.size)
        val result = IntArray(oldChapters.size)

        val byExactTitle = LinkedHashMap<String, Int>()
        val byNormalized = LinkedHashMap<String, Int>()
        val byNumber = LinkedHashMap<Int, Int>()
        newItems.forEachIndexed { index, item ->
            val title = item.title.trim()
            byExactTitle.putIfAbsent(title, index)
            byNormalized.putIfAbsent(normalize(title), index)
            chapterNumber(title)?.let { byNumber.putIfAbsent(it, index) }
        }

        oldChapters.forEachIndexed { oldIndex, chapter ->
            val title = chapter.title.trim()
            result[oldIndex] = byExactTitle[title]
                ?: byNormalized[normalize(title)]
                ?: chapterNumber(title)?.let { byNumber[it] }
                ?: positionalIndex(oldIndex, oldChapters.size, newItems.size)
        }
        return result
    }

    /** 单章定位：供「跳到上次读到的章节」使用 */
    fun matchOne(title: String, items: List<CatalogItem>): Int {
        val exact = items.indexOfFirst { it.title.trim() == title.trim() }
        if (exact >= 0) return exact
        val normalized = normalize(title)
        val normalizedHit = items.indexOfFirst { normalize(it.title) == normalized }
        if (normalizedHit >= 0) return normalizedHit
        val number = chapterNumber(title)
        if (number != null) {
            val numberHit = items.indexOfFirst { chapterNumber(it.title) == number }
            if (numberHit >= 0) return numberHit
        }
        return 0
    }

    /** 相对位置兜底：按「已读比例」映射到新目录，避免出现 -1 这类越界下标 */
    private fun positionalIndex(oldIndex: Int, oldSize: Int, newSize: Int): Int {
        if (oldSize <= 0 || newSize <= 0) return 0
        return (oldIndex.toLong() * newSize / oldSize).toInt().coerceIn(0, newSize - 1)
    }

    /**
     * 归一化：去空白 + 去标点 + 全角转半角。
     * 不同源对同一章的标题差异极大（「第1章 开局」vs「第一章 开局」vs「第001章 开局」），
     * 只做字符串比对会大面积失配。
     */
    fun normalize(title: String): String = title
        .filterNot { it.isWhitespace() }
        .map { ch -> if (ch in '！'..'～') (ch - 0xFEE0) else ch }
        .filter { it.isLetterOrDigit() }
        .joinToString("")
        .lowercase()

    /** 抽取「第 N 章」的数字；中文数字只覆盖到常见的前二十章，再往后交给归一化与比例兜底 */
    fun chapterNumber(title: String): Int? {
        val arabic = ARABIC_CHAPTER.find(title)
        if (arabic != null) return arabic.groupValues[1].toIntOrNull()
        val chinese = CHINESE_CHAPTER.find(title)?.groupValues?.get(1) ?: return null
        return chineseNumber(chinese)
    }

    private fun chineseNumber(text: String): Int? {
        if (text.length == 1) return CHINESE_DIGITS[text[0]]
        if (text.startsWith("十")) {
            val rest = text.getOrNull(1)?.let { CHINESE_DIGITS[it] } ?: 0
            return 10 + rest
        }
        val tensIndex = text.indexOf('十')
        if (tensIndex > 0) {
            val tens = CHINESE_DIGITS[text[0]] ?: return null
            val ones = text.getOrNull(tensIndex + 1)?.let { CHINESE_DIGITS[it] } ?: 0
            return tens * 10 + ones
        }
        return null
    }

    private companion object {
        val ARABIC_CHAPTER = Regex("""第\s*0*(\d+)\s*[章节回卷篇]""")
        val CHINESE_CHAPTER = Regex("""第\s*([零一二三四五六七八九十百]+)\s*[章节回卷篇]""")
        val CHINESE_DIGITS = mapOf(
            '零' to 0, '一' to 1, '二' to 2, '三' to 3, '四' to 4,
            '五' to 5, '六' to 6, '七' to 7, '八' to 8, '九' to 9,
        )
    }
}
