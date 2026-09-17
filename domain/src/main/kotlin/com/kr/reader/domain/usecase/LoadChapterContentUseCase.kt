/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: domain/src/main/kotlin/com/kr/reader/domain/usecase/LoadChapterContentUseCase.kt
 * @Description: 正文加载（第 4 卷 4.2.1）：Cache → 本地解析器 → 书源抓取，三级择优，失败吞异常不能崩空崩溃
 */
package com.kr.reader.domain.usecase

import com.kr.reader.core.model.Book
import com.kr.reader.core.model.Chapter
import com.kr.reader.core.model.page.CachedChapter
import com.kr.reader.domain.repository.CacheRepository
import com.kr.reader.domain.repository.LocalBookReader
import com.kr.reader.domain.repository.SourceEngine
import com.kr.reader.domain.repository.SourceRepository
import javax.inject.Inject

class LoadChapterContentUseCase @Inject constructor(
    private val cacheRepository: CacheRepository,
    private val localBookReader: LocalBookReader,
    private val sourceEngine: SourceEngine,
    private val sourceRepository: SourceRepository,
    private val purifyContent: PurifyContentUseCase,
) {

    suspend operator fun invoke(book: Book, chapter: Chapter): String {
        // 1. 网络书走缓存；命中就完全不碰网络，离线也能读
        cacheRepository.get(book.id, chapter.index)?.let { return it.content }

        val raw = when {
            book.isNetBook && !chapter.sourceUrl.isNullOrBlank() -> fetchFromSource(book, chapter)
            else -> runCatching { localBookReader.readChapter(book, chapter) }.getOrDefault("")
        }
        val content = purifyContent(raw)
        if (content.isBlank()) return content

        if (book.isNetBook && book.sourceId != null) {
            cacheRepository.put(
                CachedChapter(
                    bookId = book.id,
                    index = chapter.index,
                    sourceId = book.sourceId ?: 0L,
                    sourceUrl = chapter.sourceUrl.orEmpty(),
                    title = chapter.title,
                    content = content,
                    charCount = content.length,
                    cachedAt = System.currentTimeMillis(),
                    lastAccessedAt = System.currentTimeMillis(),
                ),
            )
        }
        return content
    }

    private suspend fun fetchFromSource(book: Book, chapter: Chapter): String {
        val sourceId = book.sourceId ?: return ""
        val url = chapter.sourceUrl ?: return ""
        val source = sourceRepository.get(sourceId) ?: return ""
        return runCatching { sourceEngine.fetchContent(source, url) }.getOrDefault("")
    }
}
