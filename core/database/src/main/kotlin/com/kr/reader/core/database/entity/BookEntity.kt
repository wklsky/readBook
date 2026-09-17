/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/database/src/main/kotlin/com/kr/reader/core/database/entity/BookEntity.kt
 * @Description: books 表实体：书架记录，同时承载本地书与网络书（source 相关字段可为 null）
 */
package com.kr.reader.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "books",
    indices = [
        Index(value = ["source_id", "source_book_key"], unique = true),
        Index(value = ["last_read_at"]),
    ],
    foreignKeys = [
        ForeignKey(
            entity = BookGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["group_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
)
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val author: String? = null,
    val coverPath: String? = null,
    val coverUrl: String? = null,
    val format: String,
    val filePath: String? = null,
    val fileSize: Long = 0,
    val originalName: String = "",
    val encoding: String? = null,
    val chapterRuleId: String? = null,
    val groupId: Long? = null,
    val sortOrder: Int = 0,
    val totalChapters: Int = 0,
    val totalChars: Long = 0,
    val addedAt: Long = 0L,
    val lastReadAt: Long = 0L,
    val isFavourite: Boolean = false,
    val dedupHash: String = "",
    val sourceId: Long? = null,
    val sourceBookKey: String? = null,
    val intro: String? = null,
    val latestChapterTitle: String? = null,
)
