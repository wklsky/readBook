/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/database/src/main/kotlin/com/kr/reader/core/database/dao/SourceDao.kt
 * @Description: book_sources 与 chapter_cache DAO：健康度用于聚合搜索的源排序过滤
 */
package com.kr.reader.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.kr.reader.core.database.entity.BookSourceEntity
import com.kr.reader.core.database.entity.ChapterCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SourceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(source: BookSourceEntity): Long

    @Query("SELECT * FROM book_sources ORDER BY sortOrder ASC")
    fun observeSources(): Flow<List<BookSourceEntity>>

    @Query("SELECT * FROM book_sources WHERE enabled = 1 AND health != 'BROKEN' ORDER BY sortOrder")
    suspend fun getEnabledSourcesSortedByHealth(): List<BookSourceEntity>

    @Query("SELECT * FROM book_sources WHERE id = :id")
    suspend fun getSource(id: Long): BookSourceEntity?

    @Query("SELECT COUNT(id) FROM book_sources WHERE name = :name")
    suspend fun existsByName(name: String): Int

    @Query("UPDATE book_sources SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)

    @Query("UPDATE book_sources SET sortOrder = :order WHERE id = :id")
    suspend fun updateSortOrder(id: Long, order: Int)

    @Query("DELETE FROM book_sources WHERE id = :id")
    suspend fun delete(id: Long)

    /**
     * 记录抓取成功：failCount 归零、健康度恢复 OK、平均延迟按指数平滑。
     * 用 (avg*3+latency)/4 而不是全量重算，避免整表写入。
     */
    @Transaction
    @Query("SELECT * FROM book_sources WHERE id = :id LIMIT 1")
    suspend fun recordSuccess(id: Long, latencyMs: Int, at: Long) {
        val entity = loadForUpdate(id) ?: return
        insert(
            entity.copy(
                failCount = 0,
                health = "OK",
                lastSuccessAt = at,
                lastCheckedAt = at,
                avgLatencyMs = (entity.avgLatencyMs * 3 + latencyMs) / 4,
            ),
        )
    }

    /**
     * 记录抓取失败：连续失败 3 次降 DEGRADED、10 次直接 BROKEN。
     * BROKEN 源会被聚合搜索跳过，必须靠用户手动点击「重试」才能恢复，
     * 用一个 success 也可以恢复。
     */
    @Query("SELECT * FROM book_sources WHERE id = :id LIMIT 1")
    suspend fun loadForUpdate(id: Long): BookSourceEntity?

    @Query(
        """
        UPDATE book_sources
        SET failCount = failCount + 1,
            health = CASE WHEN failCount + 1 >= 10 THEN 'BROKEN'
                         WHEN failCount + 1 >= 3 THEN 'DEGRADED'
                         ELSE health END,
            lastCheckedAt = :at
        WHERE id = :id
        """,
    )
    suspend fun recordFailureInternal(id: Long, at: Long)

    @Transaction
    suspend fun recordFailure(id: Long, at: Long) {
        recordFailureInternal(id, at)
    }
}

@Dao
interface CacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ChapterCacheEntity): Long

    @Query("SELECT * FROM chapter_cache WHERE bookId = :bookId AND `index` = :index LIMIT 1")
    suspend fun get(bookId: Long, index: Int): ChapterCacheEntity?

    @Query("UPDATE chapter_cache SET lastAccessedAt = :at WHERE id = :id")
    suspend fun touch(id: Long, at: Long)

    @Query("SELECT COUNT(id) FROM chapter_cache WHERE bookId = :bookId")
    fun observeCachedCount(bookId: Long): Flow<Int>

    @Query("DELETE FROM chapter_cache WHERE bookId = :bookId")
    suspend fun clearBook(bookId: Long)

    @Query("SELECT bookId, SUM(charCount) AS totalChar FROM chapter_cache GROUP BY bookId ORDER BY totalChar DESC")
    suspend fun groupsBySize(): List<CacheSize>

    /** 缓存淘汰：先删最旧 100 条，仍超限时对整个 dir 做 LRU 清理 */
    @Query(
        """
        DELETE FROM chapter_cache WHERE id IN (
            SELECT id FROM chapter_cache ORDER BY lastAccessedAt ASC LIMIT :limit
        )
        """,
    )
    suspend fun evictOldest(limit: Int)

    @Query("SELECT id FROM chapter_cache ORDER BY lastAccessedAt ASC LIMIT :limit")
    suspend fun oldestIds(limit: Int): List<Long>

    @Query("DELETE FROM chapter_cache WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("SELECT SUM(charCount) FROM chapter_cache")
    suspend fun totalCacheSize(): Long?
}

data class CacheSize(val bookId: Long, val totalChar: Long)
