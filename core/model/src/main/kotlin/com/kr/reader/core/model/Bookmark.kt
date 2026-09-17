/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/model/src/main/kotlin/com/kr/reader/core/model/Bookmark.kt
 * @Description: 书签与书分组模型。书签同样使用字符偏移，保证排版变化后仍然准确
 */
package com.kr.reader.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Bookmark(
    val id: Long = 0,
    val bookId: Long,
    val chapterIndex: Int,
    val chapterTitle: String,
    /** 与进度同构：字符偏移，而非页码 */
    val charOffset: Int,
    /** 书签处前若干字符的文本片段，用于书签列表展示 */
    val snippet: String,
    val note: String? = null,
    val createdAt: Long = 0L,
)

@Serializable
data class BookGroup(
    val id: Long = 0,
    val name: String,
    val sortOrder: Int = 0,
    /** 内置分组不可删除（如「全部」「未分组」） */
    val isBuiltin: Boolean = false,
    val createdAt: Long = 0L,
)

@Serializable
data class FontEntry(
    val id: String,
    val displayName: String,
    val path: String,
    val sizeBytes: Long,
    val importedAt: Long,
)
