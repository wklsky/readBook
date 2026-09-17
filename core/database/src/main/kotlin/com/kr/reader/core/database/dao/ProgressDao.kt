/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/database/src/main/kotlin/com/kr/reader/core/database/dao/ProgressDao.kt
 * @Description: 阅读进度 DAO：REPLACE 无事务，靠 SQLite 单语句原子性实现高频写入的低开销
 */
package com.kr.reader.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kr.reader.core.database.entity.ReadingProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: ReadingProgressEntity)

    @Query("SELECT * FROM reading_progress WHERE bookId = :bookId")
    suspend fun get(bookId: Long): ReadingProgressEntity?

    @Query("SELECT * FROM reading_progress WHERE bookId = :bookId")
    fun observe(bookId: Long): Flow<ReadingProgressEntity?>

    @Query("SELECT * FROM reading_progress ORDER BY updatedAt DESC LIMIT :limit")
    fun observeRecentLoaded(limit: Int): Flow<List<ReadingProgressEntity>>

    /** 删除书籍时同步清理进度，避免留下一张不被任何外键约束的行 */
    @Query("DELETE FROM reading_progress WHERE bookId IN (:ids)")
    suspend fun delete(ids: List<Long>)

    @Query("SELECT * FROM reading_progress")
    suspend fun getAll(): List<ReadingProgressEntity>
}
