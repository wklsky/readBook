/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/database/src/main/kotlin/com/kr/reader/core/database/dao/MiscDaos.kt
 * @Description: 书签、分组与阅读会话 DAO
 */
package com.kr.reader.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kr.reader.core.database.entity.BookGroupEntity
import com.kr.reader.core.database.entity.BookmarkEntity
import com.kr.reader.core.database.entity.ReadingSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookmark: BookmarkEntity): Long

    @Query("SELECT * FROM bookmarks WHERE bookId = :bookId ORDER BY createdAt DESC")
    fun observeByBook(bookId: Long): Flow<List<BookmarkEntity>>

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM bookmarks WHERE bookId = :bookId")
    suspend fun deleteByBook(bookId: Long)
}

@Dao
interface GroupDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(group: BookGroupEntity): Long

    @Query("SELECT * FROM book_groups ORDER BY sortOrder ASC")
    fun observeGroups(): Flow<List<BookGroupEntity>>

    @Query("DELETE FROM book_groups WHERE id = :id AND isBuiltin = 0")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM book_groups WHERE id = :id LIMIT 1")
    suspend fun get(id: Long): BookGroupEntity?

    @Query("UPDATE book_groups SET name = :name WHERE id = :id")
    suspend fun rename(id: Long, name: String)
}

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: ReadingSessionEntity): Long

    @Query("SELECT SUM(durationMs) FROM reading_sessions WHERE bookId = :bookId")
    suspend fun totalDuration(bookId: Long): Long?

    @Query("SELECT SUM(durationMs) FROM reading_sessions WHERE startMs BETWEEN :from AND :to")
    suspend fun totalBetween(from: Long, to: Long): Long?

    @Query("SELECT DISTINCT DATE(startMs / 1000, 'unixepoch', 'localtime') FROM reading_sessions")
    suspend fun activeDays(): List<String>

    @Query("DELETE FROM reading_sessions WHERE bookId = :bookId")
    suspend fun deleteByBook(bookId: Long)
}
