/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: domain/src/main/kotlin/com/kr/reader/domain/repository/BookRepository.kt
 * @Description: 书籍仓库契约：书架列表走 Flow 直连 Room，修改走挂起函数
 */
package com.kr.reader.domain.repository

import com.kr.reader.core.model.Book
import com.kr.reader.core.model.BookGroup
import com.kr.reader.core.model.Chapter
import com.kr.reader.core.model.ShelfSort
import kotlinx.coroutines.flow.Flow

interface BookRepository {

    /** 书架列表；groupId = null 表示全部 */
    fun observeBooks(
        groupId: Long? = null,
        sort: ShelfSort = ShelfSort.LAST_READ,
        onlyFavourite: Boolean = false,
    ): Flow<List<Book>>

    fun observeGroups(): Flow<List<BookGroup>>

    suspend fun getBook(id: Long): Book?

    suspend fun getChapters(bookId: Long): List<Chapter>

    fun observeChapters(bookId: Long): Flow<List<Chapter>>

    suspend fun getChapter(bookId: Long, index: Int): Chapter?

    /** 导入落库：返回新书 id；-1 表示被去重命中 */
    suspend fun upsertImported(book: Book, chapters: List<Chapter>): Long

    suspend fun findByDedupHash(hash: String): Book?

    suspend fun findNetBook(sourceId: Long, sourceBookKey: String): Book?

    suspend fun updateLastRead(bookId: Long, at: Long = System.currentTimeMillis())

    suspend fun moveToGroup(bookIds: List<Long>, groupId: Long?)

    suspend fun setFavourite(bookIds: List<Long>, favourite: Boolean)

    suspend fun updateManualOrder(bookId: Long, order: Int)

    suspend fun remove(bookIds: List<Long>, deleteFile: Boolean = true)

    /** 全局字符偏移 → 章节 */
    suspend fun locateChapterByOffset(bookId: Long, offset: Long): Chapter?

    suspend fun updateSourceInfo(bookId: Long, newChapters: List<Chapter>, latestChapterTitle: String?, intro: String?)

    suspend fun nextSortOrder(groupId: Long?): Int

    suspend fun createGroup(name: String): Long

    suspend fun renameGroup(id: Long, name: String)

    suspend fun deleteGroup(id: Long)
}
