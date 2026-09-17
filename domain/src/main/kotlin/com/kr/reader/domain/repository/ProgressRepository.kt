/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: domain/src/main/kotlin/com/kr/reader/domain/repository/ProgressRepository.kt
 * @Description: 阅读进度仓库：写入侧带 debounce（第 4 卷 4.4.2），必须提供强制落盘
 */
package com.kr.reader.domain.repository

import com.kr.reader.core.model.Bookmark
import com.kr.reader.core.model.ReadingProgress
import kotlinx.coroutines.flow.Flow

interface ProgressRepository {

    fun observe(bookId: Long): Flow<ReadingProgress?>

    suspend fun get(bookId: Long): ReadingProgress?

    suspend fun update(progress: ReadingProgress)

    suspend fun flushNow(bookId: Long? = null)

    suspend fun remove(bookIds: List<Long>)
}

interface BookmarkRepository {

    fun observe(bookId: Long): Flow<List<Bookmark>>

    suspend fun add(bookmark: Bookmark): Long

    suspend fun remove(id: Long)
}
