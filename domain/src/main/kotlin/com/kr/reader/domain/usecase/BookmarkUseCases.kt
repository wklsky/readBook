/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: domain/src/main/kotlin/com/kr/reader/domain/usecase/BookmarkUseCases.kt
 * @Description: 书签增删：片段取当前页首句 enough 字数，便用户回看时知道这是哪一段
 */
package com.kr.reader.domain.usecase

import com.kr.reader.core.model.Bookmark
import com.kr.reader.core.model.ReadingProgress
import com.kr.reader.domain.repository.BookmarkRepository
import javax.inject.Inject

class AddBookmarkUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val purifyContent: PurifyContentUseCase,
) {

    suspend operator fun invoke(
        bookId: Long,
        content: String,
        progress: ReadingProgress,
        note: String? = null,
    ): Long {
        val text = purifyContent(content).substring(progress.charOffsetInChapter.coerceIn(0, content.length))
        val snippet = text.replace('\n', ' ').trim().take(SNIPPET_LENGTH)
        return bookmarkRepository.add(
            Bookmark(
                bookId = bookId,
                chapterIndex = progress.chapterIndex,
                chapterTitle = progress.chapterTitle,
                charOffset = progress.charOffsetInChapter,
                snippet = snippet,
                note = note,
                createdAt = System.currentTimeMillis(),
            ),
        )
    }

    private companion object {
        const val SNIPPET_LENGTH = 40
    }
}

class RemoveBookmarkUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
) {
    suspend operator fun invoke(id: Long) = bookmarkRepository.remove(id)
}
