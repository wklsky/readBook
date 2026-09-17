/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: data/parser/src/main/kotlin/com/kr/reader/data/parser/pdf/PdfParser.kt
 * @Description: PDF 解析器（第 4 卷 4.2.4）：无稳定文本层，因此按页位图渲染 + 页面级进度
 */
package com.kr.reader.data.parser.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.kr.reader.core.common.IoDispatcher
import com.kr.reader.core.model.Book
import com.kr.reader.core.model.BookFormat
import com.kr.reader.core.model.parse.BookMetadata
import com.kr.reader.core.model.parse.BookParser
import com.kr.reader.core.model.parse.ChapterMeta
import com.kr.reader.core.model.parse.FileInput
import com.kr.reader.domain.repository.PdfPageRenderer
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext

@Singleton
class PdfParser @Inject constructor(
    @ApplicationContext private val context: Context,
) : BookParser, PdfPageRenderer {

    override val supportedFormat: BookFormat = BookFormat.PDF

    private val pageCountCache = java.util.concurrent.ConcurrentHashMap<String, Int>()

    override suspend fun getMetadata(input: FileInput): BookMetadata = withContext(IoDispatcher) {
        val count = pageCount(input.tempFilePath)
        BookMetadata(
            title = input.displayName.substringBeforeLast('.'),
            author = null,
            coverPath = renderCover(input),
            intro = "共 $count 页",
            chapterRuleId = null,
        )
    }

    /**
     * PDF 目录退化策略：拿不到 TOC 时给出单章「全文」，
     * 进度以页码为单位（第 3 卷 3.1.2 的 PDF 偏移退化方案）。
     */
    override suspend fun extractChapterList(input: FileInput): List<ChapterMeta> = withContext(IoDispatcher) {
        val count = pageCount(input.tempFilePath)
        listOf(
            ChapterMeta(
                index = 0,
                title = "全文",
                startOffset = 0L,
                endOffset = count.toLong(),
                charCount = count,
            ),
        )
    }

    override suspend fun readChapter(input: FileInput, chapter: ChapterMeta): String = ""

    override fun close(input: FileInput) {
        pageCountCache.remove(input.tempFilePath)
    }

    override suspend fun pageCount(book: Book): Int = withContext(IoDispatcher) {
        book.filePath?.let { pageCount(it) } ?: 0
    }

    override suspend fun renderPage(book: Book, pageIndex: Int, widthPx: Int, heightPx: Int): String? =
        withContext(IoDispatcher) {
            val path = book.filePath ?: return@withContext null
            val dir = File(context.cacheDir, "pdf/${book.id}")
            if (!dir.exists()) dir.mkdirs()
            val target = File(dir, "page_$pageIndex.png")
            // 命中缓存直接返回：PDF 页面不会变，重新渲染只是浪费 CPU
            if (target.exists()) return@withContext target.absolutePath
            openRenderer(path) { renderer ->
                if (pageIndex !in 0 until renderer.pageCount) return@openRenderer null
                renderer.openPage(pageIndex).use { page ->
                    val bitmap = Bitmap.createBitmap(
                        widthPx.coerceAtMost(MAX_PAGE_WIDTH),
                        heightPx.coerceAtMost(MAX_PAGE_HEIGHT),
                        Bitmap.Config.ARGB_8888,
                    )
                    bitmap.eraseColor(Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    FileOutputStream(target).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    target.absolutePath
                }
            }
        }

    private suspend fun renderCover(input: FileInput): String? = withContext(IoDispatcher) {
        val dir = File(context.filesDir, "covers")
        if (!dir.exists()) dir.mkdirs()
        val target = File(dir, "${input.displayName.hashCode()}.jpg")
        if (target.exists()) return@withContext target.absolutePath
        openRenderer(input.tempFilePath) { renderer ->
            renderer.openPage(0).use { page ->
                val bitmap = Bitmap.createBitmap(COVER_WIDTH, COVER_HEIGHT, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                FileOutputStream(target).use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out) }
                target.absolutePath
            }
        }
    }

    private suspend fun pageCount(path: String): Int = pageCountCache[path] ?: withContext(IoDispatcher) {
        val count = openRenderer(path) { it.pageCount } ?: 0
        pageCountCache[path] = count
        count
    }

    /**
     * 加密 PDF 与损坏 PDF 都会抛异常（SecurityException / IOException）。
     * 这里统一吞掉并返回 null：PDF 是次要格式，不能让它拖垮导入流程。
     */
    private fun <T> openRenderer(path: String, block: (PdfRenderer) -> T): T? = runCatching {
        val file = File(path)
        if (!file.exists()) return@runCatching null
        val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(fd)
        try {
            block(renderer)
        } finally {
            renderer.close()
            fd.close()
        }
    }.getOrNull()

    companion object {
        private const val COVER_WIDTH = 300
        private const val COVER_HEIGHT = 400
        private const val MAX_PAGE_WIDTH = 2048
        private const val MAX_PAGE_HEIGHT = 4096
    }
}
