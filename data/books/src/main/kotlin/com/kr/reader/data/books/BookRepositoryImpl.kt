/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: data/books/src/main/kotlin/com/kr/reader/data/books/BookRepositoryImpl.kt
 * @Description: 书籍仓储实现：Room DAO 结果映射为领域模型，删除书籍时同步清理文件
 */
package com.kr.reader.data.books

import com.kr.reader.core.common.IoDispatcher
import com.kr.reader.core.database.dao.BookDao
import com.kr.reader.core.database.dao.GroupDao
import com.kr.reader.core.database.entity.BookGroupEntity
import com.kr.reader.core.model.Book
import com.kr.reader.core.model.BookGroup
import com.kr.reader.core.model.Chapter
import com.kr.reader.core.model.ShelfSort
import com.kr.reader.data.books.mapper.toColumn
import com.kr.reader.data.books.mapper.toDomain
import com.kr.reader.data.books.mapper.toEntity
import com.kr.reader.domain.repository.BookRepository
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class BookRepositoryImpl @Inject constructor(
    private val bookDao: BookDao,
    private val groupDao: GroupDao,
) : BookRepository {

    override fun observeBooks(groupId: Long?, sort: ShelfSort, onlyFavourite: Boolean): Flow<List<Book>> =
        bookDao.observeShelf(groupId, sort.toColumn(), onlyFavourite)
            .map { rows -> rows.map { it.book.toDomain() } }

    override fun observeGroups(): Flow<List<BookGroup>> =
        groupDao.observeGroups().map { list -> list.map { it.toDomain() } }

    override suspend fun getBook(id: Long): Book? = withContext(IoDispatcher) {
        bookDao.getBook(id)?.toDomain()
    }

    override suspend fun getChapters(bookId: Long): List<Chapter> = withContext(IoDispatcher) {
        bookDao.getChapters(bookId).map { it.toDomain() }
    }

    override fun observeChapters(bookId: Long): Flow<List<Chapter>> =
        bookDao.observeChapters(bookId).map { list -> list.map { it.toDomain() } }

    override suspend fun getChapter(bookId: Long, index: Int): Chapter? = withContext(IoDispatcher) {
        bookDao.getChapter(bookId, index)?.toDomain()
    }

    /**
     * 入库：书目 INSERT 用 IGNORE，命中去重键时返回 -1 由上层决定提示策略。
     * 章节表全量 REPLACE：目录是派生数据，重复导入时以最新一次为准。
     */
    override suspend fun upsertImported(book: Book, chapters: List<Chapter>): Long = withContext(IoDispatcher) {
        val existing = book.id.takeIf { it > 0 }
        val id = if (existing != null) {
            bookDao.update(book.toEntity())
            existing
        } else {
            bookDao.insert(book.toEntity())
        }
        if (id <= 0L) return@withContext id
        if (chapters.isNotEmpty()) {
            bookDao.deleteChapters(id)
            bookDao.insertChapters(chapters.map { it.toEntity() })
            bookDao.updateTotalChapters(id, chapters.size)
        }
        id
    }

    override suspend fun findByDedupHash(hash: String): Book? = withContext(IoDispatcher) {
        bookDao.findByDedupHash(hash)?.toDomain()
    }

    override suspend fun findNetBook(sourceId: Long, sourceBookKey: String): Book? = withContext(IoDispatcher) {
        val id = bookDao.findNetBook(sourceId, sourceBookKey) ?: return@withContext null
        bookDao.getBook(id)?.toDomain()
    }

    override suspend fun updateLastRead(bookId: Long, at: Long) = withContext(IoDispatcher) {
        bookDao.updateLastRead(bookId, at)
    }

    override suspend fun moveToGroup(bookIds: List<Long>, groupId: Long?) = withContext(IoDispatcher) {
        bookDao.setGroup(bookIds, groupId)
    }

    override suspend fun setFavourite(bookIds: List<Long>, favourite: Boolean) = withContext(IoDispatcher) {
        bookDao.setFavourite(bookIds, favourite)
    }

    override suspend fun updateManualOrder(bookId: Long, order: Int) = withContext(IoDispatcher) {
        bookDao.updateSortOrder(bookId, order)
    }

    /** 删除书籍：先删文件再删记录。顺序反了会导致 filepath 丢失，留下无法清理的孤儿文件 */
    override suspend fun remove(bookIds: List<Long>, deleteFile: Boolean) = withContext(IoDispatcher) {
        if (deleteFile) {
            bookDao.getFilePaths(bookIds).filterNotNull().forEach { path ->
                runCatching { File(path).delete() }
            }
        }
        bookDao.deleteBooks(bookIds)
    }

    override suspend fun locateChapterByOffset(bookId: Long, offset: Long): Chapter? = withContext(IoDispatcher) {
        bookDao.locateChapterByOffset(bookId, offset)?.toDomain()
    }

    override suspend fun updateSourceInfo(
        bookId: Long,
        newChapters: List<Chapter>,
        latestChapterTitle: String?,
        intro: String?,
    ) = withContext(IoDispatcher) {
        bookDao.updateSourceInfo(bookId, latestChapterTitle, intro)
        if (newChapters.isNotEmpty()) {
            bookDao.deleteChapters(bookId)
            bookDao.insertChapters(newChapters.map { it.toEntity() })
            bookDao.updateTotalChapters(bookId, newChapters.size)
        }
    }

    /**
     * 换源落库：先重绑书源主键，再全量替换目录。
     * 顺序不能反——目录替换会先删后插，若主键重绑失败（唯一索引冲突）应当整条回滚到旧源。
     */
    override suspend fun rebindSource(
        bookId: Long,
        sourceId: Long,
        sourceBookKey: String,
        newChapters: List<Chapter>,
        latestChapterTitle: String?,
        intro: String?,
    ) = withContext(IoDispatcher) {
        bookDao.rebindSource(bookId, sourceId, sourceBookKey)
        bookDao.updateSourceInfo(bookId, latestChapterTitle, intro)
        if (newChapters.isNotEmpty()) {
            bookDao.deleteChapters(bookId)
            bookDao.insertChapters(newChapters.map { it.toEntity() })
            bookDao.updateTotalChapters(bookId, newChapters.size)
        }
    }

    override suspend fun nextSortOrder(groupId: Long?): Int = withContext(IoDispatcher) {
        (bookDao.maxSortOrder(groupId) ?: 0) + 1
    }

    override suspend fun createGroup(name: String): Long = withContext(IoDispatcher) {
        groupDao.insert(
            BookGroupEntity(
                name = name,
                sortOrder = 0,
                isBuiltin = false,
                createdAt = System.currentTimeMillis(),
            ),
        )
    }

    override suspend fun renameGroup(id: Long, name: String) = withContext(IoDispatcher) {
        groupDao.rename(id, name)
    }

    override suspend fun deleteGroup(id: Long) = withContext(IoDispatcher) {
        groupDao.delete(id)
    }
}
