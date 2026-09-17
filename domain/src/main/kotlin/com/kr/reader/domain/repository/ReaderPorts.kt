/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: domain/src/main/kotlin/com/kr/reader/domain/repository/ReaderPorts.kt
 * @Description: 由 data 层实现的能力端口：文件归档、元数据/章节抽取、分页引擎
 */
package com.kr.reader.domain.repository

import com.kr.reader.core.model.Book
import com.kr.reader.core.model.import.ImportRequest
import com.kr.reader.core.model.parse.BookMetadata
import com.kr.reader.core.model.parse.ChapterMeta
import com.kr.reader.core.model.parse.FileInput
import com.kr.reader.core.model.page.PageSnapshot
import com.kr.reader.core.model.page.PaginationRequest
import java.io.File

/** 导入链路依赖的文件能力：SAF URI → temp → private → 去重哈希 */
interface ImportFileGateway {

    suspend fun toTempFile(request: ImportRequest): Result<String>

    suspend fun archiveToPrivate(tempFile: String, displayName: String): Result<String>

    suspend fun persistUriPermission(request: ImportRequest)

    suspend fun computeDedupHash(tempFile: String, sizeBytes: Long): String

    suspend fun cleanupTemp()

    suspend fun freeSpaceMb(): Long
}

/** 元信息与目录抽取能力，屏蔽 TXT/EPUB/PDF 差异 */
interface BookInfoExtractor {

    suspend fun metadata(input: FileInput): BookMetadata

    suspend fun chapters(input: FileInput): List<ChapterMeta>

    suspend fun detectEncoding(file: File): String

    fun getFallbackEncoding(fileName: String): String
}

/** 分页能力端口：具体实现需要 Compose TextMeasurer，因此只能由 data:pagination 提供 */
interface ChapterPaginator {

    suspend fun paginate(request: PaginationRequest): List<PageSnapshot>
}

/**
 * PDF 页面渲染端口。
 * 之所以单列：PDF 没有稳定文本层（第 4 卷 4.2.4 退化策略），只能按页渲染位图，
 * 走的是与文本分页完全不同的链路。
 */
interface PdfPageRenderer {

    suspend fun pageCount(book: Book): Int

    /** 渲染指定页为 PNG 并返回本地文件路径 */
    suspend fun renderPage(book: Book, pageIndex: Int, widthPx: Int, heightPx: Int): String?
}
