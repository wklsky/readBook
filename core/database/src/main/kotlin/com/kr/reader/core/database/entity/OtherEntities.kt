/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/database/src/main/kotlin/com/kr/reader/core/database/entity/OtherEntities.kt
 * @Description: chapters / reading_progress / bookmarks / book_groups / reading_sessions 表实体
 */
package com.kr.reader.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chapters",
    indices = [Index(value = ["book_id", "index"])],
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val index: Int,
    val title: String,
    val startOffset: Long,
    val endOffset: Long,
    val charCount: Int = 0,
    val sourceUrl: String? = null,
    val isVolume: Boolean = false,
)

@Entity(tableName = "reading_progress")
data class ReadingProgressEntity(
    @PrimaryKey val bookId: Long,
    val chapterIndex: Int,
    val chapterTitle: String,
    val charOffsetInChapter: Int,
    val globalCharOffset: Long,
    val percent: Float,
    val chapterPercent: Float,
    val readingSeconds: Long = 0L,
    val updatedAt: Long = 0L,
)

@Entity(
    tableName = "bookmarks",
    indices = [Index(value = ["book_id", "created_at"])],
)
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val chapterIndex: Int,
    val chapterTitle: String,
    val charOffset: Int,
    val snippet: String,
    val note: String? = null,
    val createdAt: Long = 0L,
)

@Entity(tableName = "book_groups")
data class BookGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sortOrder: Int = 0,
    val isBuiltin: Boolean = false,
    val createdAt: Long = 0L,
)

@Entity(
    tableName = "reading_sessions",
    indices = [Index(value = ["start_ms"])],
)
data class ReadingSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long,
    val chapterIndex: Int,
    val startMs: Long,
    val endMs: Long,
    /** 单章会话持续 ms，直接入库避免 UI 每次重算 */
    val durationMs: Long,
    val endCharOffset: Int,
)
