/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: domain/src/main/kotlin/com/kr/reader/domain/usecase/LoadChaptersUseCase.kt
 * @Description: 目录加载（第 4 卷 4.2.5）：本地书取库，网络书回源抓取，异常情况下退化为已有目录而不是报错。
 */
package com.kr.reader.domain.usecase

import com.kr.reader.core.model.BookFormat
import com.kr.reader.core.model.Chapter
import com.kr.reader.core.model.parse.FileInput
import com.kr.reader.domain.repository.BookInfoExtractor
import com.kr.reader.domain.repository.BookRepository
import com.kr.reader.domain.repository.SourceEngine
import com.kr.reader.domain.repository.SourceRepository
import javax.inject.Inject

class LoadChaptersUseCase @Inject constructor(
    private val bookRepository: BookRepository,
    private val sourceRepository: SourceRepository,
    private val sourceEngine: SourceEngine,
    private val infoExtractor: BookInfoExtractor,
) {

    suspend operator fun invoke(bookId: Long, forceRefresh: Boolean = false): List<Chapter> {
        val book = bookRepository.getBook(bookId) ?: return emptyList()
        val cached = bookRepository.getChapters(bookId)
        if (cached.isNotEmpty() && !forceRefresh) return cached

        if (book.isNetBook) return rebuildFromSource(bookId, book.sourceId, book.sourceBookKey, cached)
        if (book.format == BookFormat.TXT) rebuildFromLocal(book)
        return bookRepository.getChapters(bookId)
    }

    private suspend fun rebuildFromSource(
        bookId: Long,
        sourceId: Long?,
        sourceBookKey: String?,
        fallback: List<Chapter>,
    ): List<Chapter> {
        if (sourceId == null || sourceBookKey == null) return fallback
        val source = sourceRepository.get(sourceId) ?: return fallback
        val items = runCatching { sourceEngine.fetchCatalog(source, sourceBookKey) }.getOrDefault(emptyList())
        if (items.isEmpty()) return fallback
        val chapters = items.filter { !it.isVolume }.mapIndexed { index, item ->
            Chapter(
                bookId = bookId,
                index = index,
                title = item.title,
                startOffset = 0,
                endOffset = 0,
                charCount = 0,
                sourceUrl = item.url,
            )
        }
        bookRepository.updateSourceInfo(
            bookId = bookId,
            newChapters = chapters,
            latestChapterTitle = chapters.lastOrNull()?.title,
            intro = null,
        )
        return chapters
    }

    /** 本地 TXT 目录丢失时按原文件重建：章节表是附加派生数据，丢了可以重算 */
    private suspend fun rebuildFromLocal(book: com.kr.reader.core.model.Book) {
        val path = book.filePath ?: return
        val input = FileInput(tempFilePath = path, displayName = book.originalName, encoding = book.encoding)
        val metas = runCatching { infoExtractor.chapters(input) }.getOrNull() ?: return
        if (metas.isEmpty()) return
        bookRepository.upsertImported(
            book = book.copy(totalChapters = metas.size),
            chapters = metas.map {
                Chapter(
                    bookId = book.id,
                    index = it.index,
                    title = it.title,
                    startOffset = it.startOffset,
                    endOffset = it.endOffset,
                    charCount = it.charCount,
                    isVolume = it.isVolume,
                )
            },
        )
    }
}
