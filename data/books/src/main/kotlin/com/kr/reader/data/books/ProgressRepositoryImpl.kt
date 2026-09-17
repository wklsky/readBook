/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: data/books/src/main/kotlin/com/kr/reader/data/books/ProgressRepositoryImpl.kt
 * @Description: 进度与书签仓储实现：进度写入是高频操作，统一走 REPLACE 无事务以保证低延迟
 */
package com.kr.reader.data.books

import com.kr.reader.core.common.IoDispatcher
import com.kr.reader.core.database.dao.BookmarkDao
import com.kr.reader.core.database.dao.ProgressDao
import com.kr.reader.core.model.Bookmark
import com.kr.reader.core.model.ReadingProgress
import com.kr.reader.data.books.mapper.toDomain
import com.kr.reader.data.books.mapper.toEntity
import com.kr.reader.domain.repository.BookmarkRepository
import com.kr.reader.domain.repository.ProgressRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class ProgressRepositoryImpl @Inject constructor(
    private val progressDao: ProgressDao,
) : ProgressRepository {

    override fun observe(bookId: Long): Flow<ReadingProgress?> =
        progressDao.observe(bookId).map { it?.toDomain() }

    override suspend fun get(bookId: Long): ReadingProgress? = withContext(IoDispatcher) {
        progressDao.get(bookId)?.toDomain()
    }

    override suspend fun update(progress: ReadingProgress) = withContext(IoDispatcher) {
        // 每次写入都刷新 updatedAt：书架「最近阅读」排序依赖它，不能沿用传入值
        progressDao.upsert(progress.copy(updatedAt = System.currentTimeMillis()).toEntity())
    }

    override suspend fun flushNow(bookId: Long?) {
        // 去抖逻辑在 UseCase 层；此处无额外缓冲，保持幂等
    }

    override suspend fun remove(bookIds: List<Long>) = withContext(IoDispatcher) {
        progressDao.delete(bookIds)
    }
}

@Singleton
class BookmarkRepositoryImpl @Inject constructor(
    private val bookmarkDao: BookmarkDao,
) : BookmarkRepository {

    override fun observe(bookId: Long): Flow<List<Bookmark>> =
        bookmarkDao.observeByBook(bookId).map { list -> list.map { it.toDomain() } }

    override suspend fun add(bookmark: Bookmark): Long = withContext(IoDispatcher) {
        bookmarkDao.insert(bookmark.toEntity())
    }

    override suspend fun remove(id: Long) = withContext(IoDispatcher) {
        bookmarkDao.delete(id)
    }
}
