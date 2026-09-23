/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-23 10:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-23 10:00
 * @FilePath: data/sources/src/main/kotlin/com/kr/reader/data/sources/CacheRepositoryImpl.kt
 * @Description: 网络书章节缓存实现：按 (bookId, index) 覆盖写入，超限按 LRU 淘汰
 */
package com.kr.reader.data.sources

import com.kr.reader.core.common.IoDispatcher
import com.kr.reader.core.database.dao.CacheDao
import com.kr.reader.core.database.entity.ChapterCacheEntity
import com.kr.reader.core.model.page.CachedChapter
import com.kr.reader.domain.repository.CacheRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

@Singleton
class CacheRepositoryImpl @Inject constructor(
    private val dao: CacheDao,
) : CacheRepository {

    /** 命中即续期：LRU 的「最近使用」必须在这里刷新，否则淘汰会误删用户正在追更的书 */
    override suspend fun get(bookId: Long, index: Int): CachedChapter? = withContext(IoDispatcher) {
        val entity = dao.get(bookId, index) ?: return@withContext null
        dao.touch(entity.id, System.currentTimeMillis())
        entity.toModel(lastAccessedAt = System.currentTimeMillis())
    }

    override suspend fun put(entry: CachedChapter): Unit = withContext(IoDispatcher) {
        dao.insert(entry.toEntity(cachedAt = entry.cachedAt.takeIf { it > 0 } ?: System.currentTimeMillis()))
    }

    override suspend fun clearBook(bookId: Long): Unit = withContext(IoDispatcher) {
        dao.clearBook(bookId)
    }

    override fun observeCachedCount(bookId: Long): Flow<Int> = dao.observeCachedCount(bookId)

    /**
     * 容量淘汰：字符数按 UTF-8 中文 3 字节折算成容量，超限时按批次删最久未访问。
     * 之所以分批删而不是一次全删：一次性删几十条会产生明显卡顿，且用户刚缓存的内容不应被清空。
     */
    override suspend fun evictIfNeeded(limitMb: Int): Unit = withContext(IoDispatcher) {
        if (limitMb <= 0) return@withContext
        val limitChars = limitMb.toLong() * 1024 * 1024 / BYTES_PER_CHAR
        var guard = 0
        while (dao.totalCacheSize() ?: 0L > limitChars && guard < MAX_EVICT_ROUNDS) {
            val victims = dao.oldestIds(EVICT_BATCH_SIZE)
            if (victims.isEmpty()) return@withContext
            dao.deleteByIds(victims)
            guard++
        }
    }

    private companion object {
        const val BYTES_PER_CHAR = 3L
        const val EVICT_BATCH_SIZE = 100
        const val MAX_EVICT_ROUNDS = 20
    }
}

private fun ChapterCacheEntity.toModel(lastAccessedAt: Long = this.lastAccessedAt): CachedChapter = CachedChapter(
    id = id,
    bookId = bookId,
    index = index,
    sourceId = sourceId,
    sourceUrl = sourceUrl,
    title = title,
    content = content,
    charCount = charCount,
    cachedAt = cachedAt,
    lastAccessedAt = lastAccessedAt,
)

private fun CachedChapter.toEntity(cachedAt: Long): ChapterCacheEntity = ChapterCacheEntity(
    id = id,
    bookId = bookId,
    index = index,
    sourceId = sourceId,
    sourceUrl = sourceUrl,
    title = title,
    content = content,
    charCount = charCount,
    cachedAt = cachedAt,
    lastAccessedAt = if (lastAccessedAt > 0) lastAccessedAt else System.currentTimeMillis(),
)
