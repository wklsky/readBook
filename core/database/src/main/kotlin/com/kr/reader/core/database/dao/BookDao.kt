/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/database/src/main/kotlin/com/kr/reader/core/database/dao/BookDao.kt
 * @Description: books / chapters 表 DAO。注意：实体不声明 @ColumnInfo，列名即字段名（camelCase），Query 必须与之完全一致
 */
package com.kr.reader.core.database.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.kr.reader.core.database.entity.BookEntity
import com.kr.reader.core.database.entity.ChapterEntity
import kotlinx.coroutines.flow.Flow

/**
 * 书架行投影：books 全字段 + 进度表展示列。
 * 之所以不用 @Relation：书架会有数百本书，@Relation 会对每本书再发一次查询，
 * 而 LEFT JOIN 一次取回明显更快；Room 的 @Embedded 投影本身支持这种写法。
 */
data class ShelfRow(
    @Embedded val book: BookEntity,
    val chapterIndex: Int?,
    val chapterTitle: String?,
    val percent: Float?,
    val chapterPercent: Float?,
    val updatedAt: Long?,
)

@Dao
interface BookDao {

    /** IGNORE 策略：dedupHash 唯一命中时返回 -1，由上层决定是否提示用户 */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(book: BookEntity): Long

    @Update
    suspend fun update(book: BookEntity)

    @Query("SELECT * FROM books WHERE id = :bookId LIMIT 1")
    suspend fun getBook(bookId: Long): BookEntity?

    @Query("SELECT * FROM books ORDER BY lastReadAt DESC")
    fun observeBooks(): Flow<List<BookEntity>>

    @Query("SELECT COUNT(id) FROM books")
    suspend fun countAll(): Int

    /**
     * 书架主查询。sort 由上层把 ShelfSort 映射为列名，四选一由 CASE 短路实现，
     * 避免为四种排序分别写四个 @Query 导致 SQL 漂移。
     */
    @Query(
        """
        SELECT b.*, p.chapterIndex, p.chapterTitle, p.percent, p.chapterPercent, p.updatedAt
        FROM books b LEFT JOIN reading_progress p ON p.bookId = b.id
        WHERE (:groupId IS NULL OR b.groupId = :groupId)
          AND (:onlyFavourite = 0 OR b.isFavourite = 1)
        ORDER BY
            CASE WHEN :sort = 'lastReadAt' THEN COALESCE(p.updatedAt, b.addedAt) ELSE 0 END DESC,
            CASE WHEN :sort = 'addedAt' THEN b.addedAt ELSE 0 END DESC,
            CASE WHEN :sort = 'title' THEN b.title ELSE '' END COLLATE NOCASE ASC,
            CASE WHEN :sort = 'sortOrder' THEN b.sortOrder ELSE 0 END ASC
        """,
    )
    fun observeShelf(groupId: Long?, sort: String, onlyFavourite: Boolean): Flow<List<ShelfRow>>

    @Query("UPDATE books SET lastReadAt = :time WHERE id = :bookId")
    suspend fun updateLastRead(bookId: Long, time: Long)

    @Query("UPDATE books SET groupId = :groupId WHERE id IN (:ids)")
    suspend fun setGroup(ids: List<Long>, groupId: Long?)

    @Query("UPDATE books SET isFavourite = :favourite WHERE id IN (:ids)")
    suspend fun setFavourite(ids: List<Long>, favourite: Boolean)

    @Query("UPDATE books SET sortOrder = :order WHERE id = :bookId")
    suspend fun updateSortOrder(bookId: Long, order: Int)

    @Query("UPDATE books SET totalChapters = :count WHERE id = :bookId")
    suspend fun updateTotalChapters(bookId: Long, count: Int)

    @Query("UPDATE books SET latestChapterTitle = :title, intro = :intro WHERE id = :bookId")
    suspend fun updateSourceInfo(bookId: Long, title: String?, intro: String?)

    @Query("SELECT * FROM books WHERE dedupHash = :hash LIMIT 1")
    suspend fun findByDedupHash(hash: String): BookEntity?

    @Query("SELECT id FROM books WHERE sourceId = :sourceId AND sourceBookKey = :bookKey LIMIT 1")
    suspend fun findNetBook(sourceId: Long, bookKey: String): Long?

    @Query("SELECT MAX(sortOrder) FROM books WHERE groupId IS :groupId")
    suspend fun maxSortOrder(groupId: Long?): Int?

    @Transaction
    @Query("DELETE FROM books WHERE id IN (:ids)")
    suspend fun deleteBooks(ids: List<Long>)

    @Query("SELECT filePath FROM books WHERE id IN (:ids)")
    suspend fun getFilePaths(ids: List<Long>): List<String?>

    // ── chapters ────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Query("DELETE FROM chapters WHERE bookId = :bookId")
    suspend fun deleteChapters(bookId: Long)

    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY `index`")
    suspend fun getChapters(bookId: Long): List<ChapterEntity>

    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY `index`")
    fun observeChapters(bookId: Long): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE bookId = :bookId AND `index` = :index LIMIT 1")
    suspend fun getChapter(bookId: Long, index: Int): ChapterEntity?

    @Query("SELECT COUNT(id) FROM chapters WHERE bookId = :bookId")
    suspend fun countChapters(bookId: Long): Int

    @Query("SELECT COUNT(id) FROM chapters WHERE bookId = :bookId")
    fun observeChapterCount(bookId: Long): Flow<Int>

    /** 全局字符偏移 → 所属章节：用 ≤ 的最大 startOffset 定位 */
    @Query(
        """
        SELECT * FROM chapters
        WHERE bookId = :bookId AND startOffset <= :offset
        ORDER BY startOffset DESC LIMIT 1
        """,
    )
    suspend fun locateChapterByOffset(bookId: Long, offset: Long): ChapterEntity?
}
