/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-23 10:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-23 10:00
 * @FilePath: data/sources/src/test/kotlin/com/kr/reader/data/sources/ChapterMatcherTest.kt
 * @Description: 换源章节匹配单测：四级策略必须逐级降级且永不越界
 */
package com.kr.reader.data.sources

import com.google.common.truth.Truth.assertThat
import com.kr.reader.core.model.Chapter
import com.kr.reader.core.model.source.CatalogItem
import org.junit.Test

class ChapterMatcherTest {

    private val matcher = ChapterMatcher()

    private fun chapter(index: Int, title: String) = Chapter(
        bookId = 1L,
        index = index,
        title = title,
        startOffset = 0L,
        endOffset = 0L,
    )

    private fun item(title: String) = CatalogItem(title = title, url = "https://a.b/$title")

    @Test
    fun `标题完全一致时按精确匹配`() {
        val old = listOf(chapter(0, "第一章 开局"), chapter(1, "第二章 入门"))
        val new = listOf(item("第一章 开局"), item("第二章 入门"), item("第三章 进阶"))
        assertThat(matcher.buildIndexMap(old, new).toList()).containsExactly(0, 1).inOrder()
    }

    @Test
    fun `阿拉伯数字与中文数字章节应归一化命中`() {
        // 换源最常见的差异：A 源写「第1章」，B 源写「第一章」，必须视为同一章
        val old = listOf(chapter(0, "第1章 开局"), chapter(1, "第002章 入门"))
        val new = listOf(item("第一章 开局"), item("第二章 入门"))
        assertThat(matcher.buildIndexMap(old, new).toList()).containsExactly(0, 1).inOrder()
    }

    @Test
    fun `归一化仍失配时按章序号匹配`() {
        val old = listOf(chapter(0, "序章"), chapter(5, "第6章 完全不同的标题"))
        val new = listOf(item("第1章 a"), item("第2章 b"), item("第6章 c"))
        val map = matcher.buildIndexMap(old, new)
        assertThat(map[1]).isEqualTo(2)
    }

    @Test
    fun `全部失配时按比例兜底且不越界`() {
        val old = List(10) { chapter(it, "未知标题 $it") }
        val new = List(4) { item("新源标题 $it") }
        val map = matcher.buildIndexMap(old, new)
        assertThat(map.all { it in 0..3 }).isTrue()
        // 旧目录最后一章必须映射到新目录最后一章，否则用户会被凭空往前扔好几章
        assertThat(map[9]).isEqualTo(3)
    }

    @Test
    fun `新目录为空时全部退化到第一章`() {
        val map = matcher.buildIndexMap(listOf(chapter(0, "第一章")), emptyList())
        assertThat(map.toList()).containsExactly(0)
    }

    @Test
    fun `单章定位优先精确标题`() {
        val new = listOf(item("第一章 开局"), item("第二章 入门"))
        assertThat(matcher.matchOne("第二章 入门", new)).isEqualTo(1)
        assertThat(matcher.matchOne("第2章 入门", new)).isEqualTo(1)
        assertThat(matcher.matchOne("不存在的章节", new)).isEqualTo(0)
    }
}
