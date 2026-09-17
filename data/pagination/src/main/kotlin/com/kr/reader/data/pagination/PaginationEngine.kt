/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: data/pagination/src/main/kotlin/com/kr/reader/data/pagination/PaginationEngine.kt
 * @Description: Compose 分页引擎（第 4 卷 4.3.2）：基于 TextMeasurer 真实换行，支持整体性要与长段拆分
 */
package com.kr.reader.data.pagination

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.kr.reader.core.model.TextAlignMode
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import com.kr.reader.core.common.DefaultDispatcher
import com.kr.reader.core.model.page.PageLineDescriptor
import com.kr.reader.core.model.page.PageSnapshot
import com.kr.reader.core.model.page.PageStyleSpec
import com.kr.reader.core.model.page.PaginationException
import com.kr.reader.core.model.page.PaginationRequest
import com.kr.reader.domain.repository.ChapterPaginator
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** 超过该字符数的单页标记为延迟绘制，避免一次性提交超大文本造成掉帧 */
private const val LAZY_DRAW_CHAR_THRESHOLD = 8000

/** 极端长行（无任何空白）的保护：超过后强制截断，避免一次 measure 卡住主线程 */
private const val MAX_PARAGRAPH_CHARS = 200_000

@Singleton
class PaginationEngine @Inject constructor(
    private val textMeasurerHolder: TextMeasurerHolder,
) : ChapterPaginator {

    /** 同一本书同一章的并发分页没有意义，用互斥锁保证「后到者直接用先到者的结果」 */
    private val mutex = Mutex()

    /** key = bookId:chapterIndex:styleHash:size */
    private val cache = ConcurrentHashMap<String, List<PageSnapshot>>()

    override suspend fun paginate(request: PaginationRequest): List<PageSnapshot> = withContext(DefaultDispatcher) {
        val (measurer, density) = textMeasurerHolder.current()
            ?: throw PaginationException("尚未初始化")
        val width = request.contentWidthPx.coerceAtLeast(1f)
        val height = request.contentHeightPx.coerceAtLeast(1f)
        val key = cacheKey(request)
        cache[key]?.takeIf { !request.regenerate }?.let { return@withContext it }

        mutex.withLock {
            cache[key]?.takeIf { !request.regenerate }?.let { return@withContext it }
            val pages = doPaginate(request, measurer, density, width, height)
            cache[key] = pages
            pages
        }
    }

    private fun doPaginate(
        request: PaginationRequest,
        measurer: TextMeasurer,
        density: Density,
        width: Float,
        height: Float,
    ): List<PageSnapshot> {
        val spec = request.pageSpec
        val style = spec.toStyle(density)
        val paragraphs = splitParagraphs(request.content)
        if (paragraphs.isEmpty()) return emptyList()

        val pages = ArrayList<PageSnapshot>()
        val currentLines = ArrayList<PageLineDescriptor>()
        val currentText = StringBuilder()
        var currentHeight = 0f
        var pageStartOffset = 0
        var lastEndOffset = 0
        var isFirstLineOnPage = true

        fun finishPage(endOffset: Int) {
            if (currentText.isEmpty() && pages.isEmpty()) return
            pages.add(
                PageSnapshot(
                    index = pages.size,
                    chapterIndex = request.chapterIndex,
                    startCharOffset = pageStartOffset,
                    endCharOffset = endOffset,
                    lineDescriptors = currentLines.toList(),
                    displayText = currentText.toString(),
                    drawLazily = currentText.length > LAZY_DRAW_CHAR_THRESHOLD,
                ),
            )
            currentLines.clear()
            currentText.clear()
            currentHeight = 0f
            isFirstLineOnPage = true
        }

        paragraphs.forEach { paragraph ->
            val text = paragraph.text
            if (text.isEmpty()) return@forEach
            val layoutResult = measurer.measure(
                text = AnnotatedString(text),
                style = style,
                constraints = Constraints.fixedWidth(width.toInt()),
                maxLines = Int.MAX_VALUE,
            )
            val lineCount = layoutResult.lineCount
            for (lineIndex in 0 until lineCount) {
                val start = layoutResult.getLineStart(lineIndex)
                val end = layoutResult.getLineEnd(lineIndex, visibleEnd = true)
                if (end <= start) continue
                val lineText = text.substring(start, end)
                // 段落内首行需要额外加上行间距；跨段落还需要加上段间距
                val isFirstLineOfParagraph = lineIndex == 0
                val extraSpacing = when {
                    isFirstLineOnPage && isFirstLineOfParagraph -> 0f
                    isFirstLineOfParagraph -> spec.paragraphSpacingPx
                    else -> 0f
                }
                val lineHeightPx = layoutResult.getLineBottom(lineIndex) - layoutResult.getLineTop(lineIndex)
                val required = lineHeightPx + extraSpacing
                if (currentHeight + required > height && !currentText.isEmpty()) {
                    finishPage(lastEndOffset)
                    pageStartOffset = paragraph.start + start
                }
                if (currentText.isNotEmpty()) {
                    currentText.append('\n')
                }
                val lineStartOffset = paragraph.start + start
                currentText.append(lineText)
                currentHeight += required
                lastEndOffset = paragraph.start + end
                if (isFirstLineOnPage) {
                    pageStartOffset = lineStartOffset
                    isFirstLineOnPage = false
                }
                currentLines.add(
                    PageLineDescriptor(
                        lineVersion = LINE_VERSION,
                        lineHeightPx = lineHeightPx,
                        contentWidthPx = width,
                        lineSpacingExtraPx = spec.lineSpacingExtraPx,
                        paragraphSpacingPx = spec.paragraphSpacingPx,
                        charStyleHash = spec.charStyleHash,
                        fontFamilyId = spec.fontFamilyId,
                    ),
                )
            }
        }
        finishPage(lastEndOffset)
        return pages
    }

    /**
     * 按换行切段并记录每段的起始字符偏移。
     * 刻意不修改任何字符（不去 trim、不合并空白）：一旦改动就破坏「字符偏移 → 原文」的映射。
     */
    private fun splitParagraphs(content: String): List<Paragraph> {
        val result = ArrayList<Paragraph>()
        var start = 0
        var index = 0
        while (index < content.length) {
            val ch = content[index]
            if (ch == '\n' || ch == '\r') {
                if (index > start) result.add(Paragraph(content.substring(start, index), start))
                index++
                if (ch == '\r' && index < content.length && content[index] == '\n') index++
                start = index
            } else {
                index++
            }
        }
        if (start < content.length) result.add(Paragraph(content.substring(start), start))
        return result.map { p ->
            if (p.text.length > MAX_PARAGRAPH_CHARS) {
                Paragraph(p.text.take(MAX_PARAGRAPH_CHARS), p.start)
            } else {
                p
            }
        }
    }

    private fun PageStyleSpec.toStyle(density: Density): TextStyle {
        // PageStyleSpec 里的数值都是 px（由 UI 层按设备密度换算而来），这里反解回 sp/em 供 Compose 使用
        val scale = density.density * density.fontScale
        return TextStyle(
            fontSize = TextUnit(fontSizePx / scale, TextUnitType.Sp),
            lineHeight = TextUnit(lineHeightPx / scale, TextUnitType.Sp),
            letterSpacing = TextUnit(letterSpacingEm, TextUnitType.Em),
            textAlign = when (align) {
                TextAlignMode.JUSTIFY -> TextAlign.Justify
                TextAlignMode.START -> TextAlign.Start
            },
            textIndent = if (paragraphIndentWidth > 0f) {
                TextIndent(firstLine = TextUnit(paragraphIndentWidth / scale, TextUnitType.Sp), restLine = TextUnit.Zero)
            } else {
                TextIndent.None
            },
        )
    }

    private fun cacheKey(request: PaginationRequest): String = buildString {
        append(request.bookId)
        append(':')
        append(request.chapterIndex)
        append(':')
        append(request.pageSpec.charStyleHash)
        append(':')
        append(request.contentWidthPx.toInt())
        append('x')
        append(request.contentHeightPx.toInt())
        append(':')
        append(request.content.length)
    }

    fun invalidate(bookId: Long) {
        cache.keys.removeIf { it.startsWith("$bookId:") }
    }

    fun clear() = cache.clear()

    private data class Paragraph(val text: String, val start: Int)

    companion object {
        private const val LINE_VERSION = 1
    }
}
