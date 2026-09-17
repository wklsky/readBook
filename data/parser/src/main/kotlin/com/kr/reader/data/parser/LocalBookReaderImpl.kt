/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: data/parser/src/main/kotlin/com/kr/reader/data/parser/LocalBookReaderImpl.kt
 * @Description: 本地正文读取实现：按 format 分发到具体解析器，PDF 无文本层故返回空串
 */
package com.kr.reader.data.parser

import com.kr.reader.core.model.Book
import com.kr.reader.core.model.BookFormat
import com.kr.reader.core.model.Chapter
import com.kr.reader.core.model.parse.ChapterMeta
import com.kr.reader.core.model.parse.FileInput
import com.kr.reader.data.parser.epub.EpubParser
import com.kr.reader.data.parser.pdf.PdfParser
import com.kr.reader.data.parser.txt.TxtParser
import com.kr.reader.domain.repository.LocalBookReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalBookReaderImpl @Inject constructor(
    private val txtParser: TxtParser,
    private val epubParser: EpubParser,
    private val pdfParser: PdfParser,
) : LocalBookReader {

    override suspend fun readChapter(book: Book, chapter: Chapter): String {
        val input = input(book, chapter) ?: return ""
        return when (book.format) {
            BookFormat.TXT -> txtParser.readChapter(input, chapter.toMeta())
            BookFormat.EPUB -> epubParser.readChapter(input, chapter.toMeta())
            // PDF 走位图渲染（see PdfPageRenderer），文本层不可用，返回空串让上层切换渲染模式
            BookFormat.PDF -> ""
            BookFormat.NET -> ""
        }
    }

    override suspend fun readPreview(book: Book, maxChars: Int): String {
        val first = Chapter(
            bookId = book.id,
            index = 0,
            title = "",
            startOffset = 0,
            endOffset = maxChars.toLong(),
        )
        val content = readChapter(book, first)
        // 预览用途先归一化再截断，避免把换行与全角空格带进卡片展示
        return content.replace('\n', ' ').replace(Regex("[\\s　]+"), " ").take(maxChars)
    }

    private fun input(book: Book, chapter: Chapter): FileInput? {
        val path = book.filePath ?: return null
        return FileInput(
            tempFilePath = path,
            displayName = book.originalName.ifBlank { book.title },
            encoding = book.encoding,
            pageIndex = if (book.format == BookFormat.PDF) chapter.index else null,
        )
    }

    private fun Chapter.toMeta(): ChapterMeta = ChapterMeta(
        index = index,
        title = title,
        startOffset = startOffset,
        endOffset = endOffset,
        charCount = charCount,
        isVolume = isVolume,
    )
}
