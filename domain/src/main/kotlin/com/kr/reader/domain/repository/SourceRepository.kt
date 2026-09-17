/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: domain/src/main/kotlin/com/kr/reader/domain/repository/SourceRepository.kt
 * @Description: 书源、缓存与本地/网络正文读取契约：domain 不感知 OkHttp/Room/Jsoup 具体实现
 */
package com.kr.reader.domain.repository

import com.kr.reader.core.model.Book
import com.kr.reader.core.model.BookSource
import com.kr.reader.core.model.Chapter
import com.kr.reader.core.model.source.CatalogItem
import com.kr.reader.core.model.source.MergedBook
import com.kr.reader.core.model.source.SourceBookDiff
import com.kr.reader.core.model.source.SourceSearchItem
import kotlinx.coroutines.flow.Flow

interface SourceRepository {

    fun observeSources(): Flow<List<BookSource>>

    suspend fun all(): List<BookSource>

    /** 聚合搜索时按健康度排序的可用源 */
    suspend fun enabledSourcesSortedByHealth(): List<BookSource>

    suspend fun get(id: Long): BookSource?

    suspend fun insert(source: BookSource): Long

    suspend fun delete(id: Long)

    suspend fun setEnabled(id: Long, enabled: Boolean)

    suspend fun existsByName(name: String): Boolean

    suspend fun reorder(id: Long, newOrder: Int)

    suspend fun recordSuccess(id: Long, latencyMs: Int)

    suspend fun recordFailure(id: Long)
}

interface SourceEngine {

    suspend fun search(source: BookSource, keyword: String): List<SourceSearchItem>

    suspend fun fetchDetail(source: BookSource, detailUrl: String): MergedBook

    suspend fun fetchCatalog(source: BookSource, bookKey: String): List<CatalogItem>

    suspend fun fetchContent(source: BookSource, chapterUrl: String): String

    /** 同书多源差异审计（第 4 卷 4.8.3） */
    suspend fun compareSources(bookId: Long): List<SourceBookDiff>

    suspend fun switchSource(bookId: Long, targetSourceId: Long): Book
}

/** 本地正文读取：TXT/EPUB/PDF 三种解析器对外的统一抽象 */
interface LocalBookReader {

    suspend fun readChapter(book: Book, chapter: Chapter): String

    suspend fun readPreview(book: Book, maxChars: Int = 2000): String
}

interface CacheRepository {

    suspend fun get(bookId: Long, index: Int): com.kr.reader.core.model.page.CachedChapter?

    suspend fun put(entry: com.kr.reader.core.model.page.CachedChapter)

    suspend fun clearBook(bookId: Long)

    fun observeCachedCount(bookId: Long): Flow<Int>

    suspend fun evictIfNeeded(limitMb: Int)
}
