/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: data/books/src/main/kotlin/com/kr/reader/data/books/mapper/BookMappers.kt
 * @Description: 实体 ↔ 领域模型映射。enum 以字符串落库，未知值统一降级为安全默认值而非抛异常
 */
package com.kr.reader.data.books.mapper

import com.kr.reader.core.database.entity.BookEntity
import com.kr.reader.core.database.entity.BookGroupEntity
import com.kr.reader.core.database.entity.BookmarkEntity
import com.kr.reader.core.database.entity.ChapterEntity
import com.kr.reader.core.database.entity.ReadingProgressEntity
import com.kr.reader.core.model.Book
import com.kr.reader.core.model.BookFormat
import com.kr.reader.core.model.BookGroup
import com.kr.reader.core.model.Bookmark
import com.kr.reader.core.model.Chapter
import com.kr.reader.core.model.ReadingProgress
import com.kr.reader.core.model.ShelfSort

fun BookEntity.toDomain(): Book = Book(
    id = id,
    title = title,
    author = author,
    coverPath = coverPath,
    coverUrl = coverUrl,
    format = runCatching { BookFormat.valueOf(format) }.getOrDefault(BookFormat.TXT),
    filePath = filePath,
    fileSize = fileSize,
    originalName = originalName,
    encoding = encoding,
    chapterRuleId = chapterRuleId,
    groupId = groupId,
    sortOrder = sortOrder,
    totalChapters = totalChapters,
    totalChars = totalChars,
    addedAt = addedAt,
    lastReadAt = lastReadAt,
    isFavourite = isFavourite,
    dedupHash = dedupHash,
    sourceId = sourceId,
    sourceBookKey = sourceBookKey,
    intro = intro,
    latestChapterTitle = latestChapterTitle,
)

fun Book.toEntity(): BookEntity = BookEntity(
    id = id,
    title = title,
    author = author,
    coverPath = coverPath,
    coverUrl = coverUrl,
    format = format.name,
    filePath = filePath,
    fileSize = fileSize,
    originalName = originalName,
    encoding = encoding,
    chapterRuleId = chapterRuleId,
    groupId = groupId,
    sortOrder = sortOrder,
    totalChapters = totalChapters,
    totalChars = totalChars,
    addedAt = addedAt,
    lastReadAt = lastReadAt,
    isFavourite = isFavourite,
    dedupHash = dedupHash,
    sourceId = sourceId,
    sourceBookKey = sourceBookKey,
    intro = intro,
    latestChapterTitle = latestChapterTitle,
)

fun ChapterEntity.toDomain(): Chapter = Chapter(
    id = id,
    bookId = bookId,
    index = index,
    title = title,
    startOffset = startOffset,
    endOffset = endOffset,
    charCount = charCount,
    sourceUrl = sourceUrl,
    isVolume = isVolume,
)

fun Chapter.toEntity(): ChapterEntity = ChapterEntity(
    id = id,
    bookId = bookId,
    index = index,
    title = title,
    startOffset = startOffset,
    endOffset = endOffset,
    charCount = charCount,
    sourceUrl = sourceUrl,
    isVolume = isVolume,
)

fun ReadingProgressEntity.toDomain(): ReadingProgress = ReadingProgress(
    bookId = bookId,
    chapterIndex = chapterIndex,
    chapterTitle = chapterTitle,
    charOffsetInChapter = charOffsetInChapter,
    globalCharOffset = globalCharOffset,
    percent = percent,
    chapterPercent = chapterPercent,
    readingSeconds = readingSeconds,
    updatedAt = updatedAt,
)

fun ReadingProgress.toEntity(): ReadingProgressEntity = ReadingProgressEntity(
    bookId = bookId,
    chapterIndex = chapterIndex,
    chapterTitle = chapterTitle,
    charOffsetInChapter = charOffsetInChapter,
    globalCharOffset = globalCharOffset,
    percent = percent,
    chapterPercent = chapterPercent,
    readingSeconds = readingSeconds,
    updatedAt = updatedAt,
)

fun BookmarkEntity.toDomain(): Bookmark = Bookmark(
    id = id,
    bookId = bookId,
    chapterIndex = chapterIndex,
    chapterTitle = chapterTitle,
    charOffset = charOffset,
    snippet = snippet,
    note = note,
    createdAt = createdAt,
)

fun Bookmark.toEntity(): BookmarkEntity = BookmarkEntity(
    id = id,
    bookId = bookId,
    chapterIndex = chapterIndex,
    chapterTitle = chapterTitle,
    charOffset = charOffset,
    snippet = snippet,
    note = note,
    createdAt = createdAt,
)

fun BookGroupEntity.toDomain(): BookGroup = BookGroup(
    id = id,
    name = name,
    sortOrder = sortOrder,
    isBuiltin = isBuiltin,
    createdAt = createdAt,
)

/** 书架排序 → SQL 排序列，必须与 BookDao.observeShelf 的 CASE 分支严格对应 */
fun ShelfSort.toColumn(): String = when (this) {
    ShelfSort.LAST_READ -> "lastReadAt"
    ShelfSort.ADDED -> "addedAt"
    ShelfSort.TITLE -> "title"
    ShelfSort.MANUAL -> "sortOrder"
}
