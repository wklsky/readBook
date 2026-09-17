/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: data/parser/src/main/kotlin/com/kr/reader/data/parser/BookInfoExtractorImpl.kt
 * @Description: 元信息与目录抽取实现：按扩展名分发到 TXT/EPUB/PDF 解析器
 */
package com.kr.reader.data.parser

import com.kr.reader.core.model.parse.BookMetadata
import com.kr.reader.core.model.parse.ChapterMeta
import com.kr.reader.core.model.parse.FileInput
import com.kr.reader.data.parser.epub.EpubParser
import com.kr.reader.data.parser.pdf.PdfParser
import com.kr.reader.data.parser.txt.CharsetDetector
import com.kr.reader.data.parser.txt.TxtParser
import com.kr.reader.domain.repository.BookInfoExtractor
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookInfoExtractorImpl @Inject constructor(
    private val txtParser: TxtParser,
    private val epubParser: EpubParser,
    private val pdfParser: PdfParser,
) : BookInfoExtractor {

    override suspend fun metadata(input: FileInput): BookMetadata = parserFor(input).getMetadata(input)

    override suspend fun chapters(input: FileInput): List<ChapterMeta> = parserFor(input).extractChapterList(input)

    override suspend fun detectEncoding(file: File): String = CharsetDetector.detect(file)

    /** 文件名后缀提示的编码兜底：日语小说多为 Shift_JIS，国产盗版站导出的多为 GBK */
    override fun getFallbackEncoding(fileName: String): String {
        val lower = fileName.lowercase()
        return when {
            lower.contains("utf") -> "UTF-8"
            lower.contains("gb") -> "GBK"
            lower.contains("big5") -> "BIG5"
            lower.contains("jp") || lower.contains("jis") -> "Shift_JIS"
            else -> "UTF-8"
        }
    }

    private fun parserFor(input: FileInput) = when {
        input.displayName.endsWith(".txt", true) -> txtParser
        input.displayName.endsWith(".epub", true) -> epubParser
        input.displayName.endsWith(".pdf", true) -> pdfParser
        input.tempFilePath.endsWith(".txt", true) -> txtParser
        input.tempFilePath.endsWith(".epub", true) -> epubParser
        else -> pdfParser
    }
}
