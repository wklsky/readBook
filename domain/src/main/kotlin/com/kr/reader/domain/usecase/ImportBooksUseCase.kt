/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: domain/src/main/kotlin/com/kr/reader/domain/usecase/ImportBooksUseCase.kt
 * @Description: 书籍导入（第 4 卷 4.1.3）：体积校验 → 临时复制 → 去重 → 编码检测 → 元数据 → 目录 → 入库
 */
package com.kr.reader.domain.usecase

import com.kr.reader.core.model.Book
import com.kr.reader.core.model.Chapter
import com.kr.reader.core.model.ReadingProgress
import com.kr.reader.core.model.import.ImportFailure
import com.kr.reader.core.model.import.ImportFailureReason
import com.kr.reader.core.model.import.ImportMode
import com.kr.reader.core.model.import.ImportProgress
import com.kr.reader.core.model.import.ImportRequest
import com.kr.reader.core.model.parse.BookMetadata
import com.kr.reader.core.model.parse.FileInput
import com.kr.reader.domain.repository.BookInfoExtractor
import com.kr.reader.domain.repository.BookRepository
import com.kr.reader.domain.repository.ImportFileGateway
import com.kr.reader.domain.repository.ProgressRepository
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** 单书体积上限：超过后即使 4KB 缓冲也会让低端机 UI 明显卡顿 */
const val MAX_FILE_SIZE_BYTES = 100L * 1024 * 1024

class ImportBooksUseCase @Inject constructor(
    private val bookRepository: BookRepository,
    private val progressRepository: ProgressRepository,
    private val fileGateway: ImportFileGateway,
    private val infoExtractor: BookInfoExtractor,
) {

    private val progress = MutableStateFlow(ImportProgress(0, 0, emptyList()))

    fun observeProgress(): Flow<ImportProgress> = progress.asStateFlow()

    suspend operator fun invoke(requests: List<ImportRequest>): List<Long> {
        val total = requests.size
        val failures = mutableListOf<ImportFailure>()
        val resultIds = mutableListOf<Long>()
        requests.forEachIndexed { i, request ->
            progress.value = ImportProgress(total, i, failures.toList(), request.displayName)
            val id = importOne(request)
            if (id > 0) {
                resultIds += id
            } else {
                failures += ImportFailure(request.displayName, ImportFailureReason.Unknown())
            }
        }
        progress.value = ImportProgress(total, total, failures.toList(), "")
        fileGateway.cleanupTemp()
        return resultIds
    }

    private suspend fun importOne(request: ImportRequest): Long {
        // 1. 体积校验
        if (request.sizeBytes > MAX_FILE_SIZE_BYTES) return -1L
        // 2. SAF → temp
        val tempPath = fileGateway.toTempFile(request).getOrElse {
            return -1L
        }
        val tempFile = File(tempPath)
        // 3. 去重哈希：先算哈希再决定是否继续，避免重复书浪费一次完整解析
        val hash = fileGateway.computeDedupHash(tempPath, request.sizeBytes)
        if (bookRepository.findByDedupHash(hash) != null) return -1L
        // 4. REFERENCE 模式保存 URI 授权，后续每次都从原始位置读
        val finalPath = when (request.mode ?: recommendedMode(request.sizeBytes)) {
            ImportMode.REFERENCE -> {
                fileGateway.persistUriPermission(request)
                tempPath
            }
            ImportMode.ARCHIVE -> fileGateway.archiveToPrivate(tempPath, request.displayName).getOrElse { return -1L }
        }
        // 5. 编码检测（失败用文件名.lua 编码兜底，绝不中断导入）
        val encoding = runCatching { infoExtractor.detectEncoding(tempFile) }
            .getOrDefault(infoExtractor.getFallbackEncoding(request.displayName))
        val input = FileInput(tempFilePath = finalPath, displayName = request.displayName, encoding = encoding)

        val metadata: BookMetadata = runCatching { infoExtractor.metadata(input) }
            .getOrDefault(BookMetadata(title = request.displayName.substringBeforeLast('.')))

        // 6. 目录抽取：抽不出目录时退化为「全文」单章，保证用户至少能读
        val chapterMetas = runCatching { infoExtractor.chapters(input) }
            .getOrDefault(emptyList())
        val title = metadata.title.ifBlank { request.displayName.substringBeforeLast('.') }

        val sortOrder = bookRepository.nextSortOrder(null)
        val now = System.currentTimeMillis()
        val book = Book(
            title = title,
            author = metadata.author,
            coverPath = metadata.coverPath,
            format = request.format,
            filePath = finalPath,
            fileSize = request.sizeBytes,
            originalName = request.displayName,
            encoding = encoding,
            chapterRuleId = metadata.chapterRuleId,
            sortOrder = sortOrder,
            totalChapters = chapterMetas.size.coerceAtLeast(1),
            totalChars = chapterMetas.sumOf { it.charCount.toLong() },
            addedAt = now,
            lastReadAt = now,
            dedupHash = hash,
            intro = metadata.intro,
        )
        val bookId = bookRepository.upsertImported(book, emptyList())
        if (bookId <= 0L) return -1L
        val chapters = chapterMetas.ifEmpty {
            listOf(
                Chapter(
                    bookId = bookId,
                    index = 0,
                    title = "全文",
                    startOffset = 0,
                    endOffset = 0,
                    charCount = 0,
                ),
            )
        }.map { it.copy(bookId = bookId) }
        bookRepository.upsertImported(book.copy(id = bookId, totalChapters = chapters.size), chapters)
        val first = chapters.firstOrNull()
        progressRepository.update(ReadingProgress.initial(bookId, first))
        bookRepository.updateLastRead(bookId, now)
        return bookId
    }

    private suspend fun recommendedMode(sizeBytes: Long): ImportMode {
        // 剩余空间 < 500MB 时默认走引用模式，避免大文件把用户存储占满
        val free = fileGateway.freeSpaceMb()
        return if (free < 500 && sizeBytes > 20 * 1024 * 1024) ImportMode.REFERENCE else ImportMode.ARCHIVE
    }
}
