/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: data/books/src/main/kotlin/com/kr/reader/data/books/FileArchive.kt
 * @Description: 文件归档：SAF URI → 临时文件 → 私有目录，同时负责去重哈希与剩余空间探测
 */
package com.kr.reader.data.books

import android.content.Context
import android.net.Uri
import com.kr.reader.core.common.IoDispatcher
import com.kr.reader.core.common.dedupHash
import com.kr.reader.core.model.import.ImportRequest
import com.kr.reader.domain.repository.ImportFileGateway
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext

@Singleton
class FileArchive @Inject constructor(
    @ApplicationContext private val context: Context,
) : ImportFileGateway {

    override suspend fun toTempFile(request: ImportRequest): Result<String> = withContext(IoDispatcher) {
        runCatching {
            val uri = Uri.parse(request.uri)
            val sourceName = request.displayName.ifBlank { UUID.randomUUID().toString() }
            val temp = File(context.cacheDir, "import/${UUID.randomUUID()}_$sourceName")
            temp.parentFile?.mkdirs()
            context.contentResolver.openInputStream(uri)?.use { input ->
                temp.outputStream().use { output -> input.copyTo(output, COPY_BUFFER) }
            } ?: throw java.io.FileNotFoundException(uri.toString())
            temp.absolutePath
        }
    }

    override suspend fun archiveToPrivate(tempFile: String, displayName: String): Result<String> = withContext(IoDispatcher) {
        runCatching {
            val dir = File(context.filesDir, BOOKS_DIR)
            if (!dir.exists()) dir.mkdirs()
            val target = File(dir, "${UUID.randomUUID()}_$displayName")
            File(tempFile).inputStream().use { input ->
                target.outputStream().use { output -> input.copyTo(output, COPY_BUFFER) }
            }
            target.absolutePath
        }
    }

    /**
     * 引用模式必须申请持久化授权：SAF 授予的临时读权限在进程重启后失效，
     * 没有这行会导致「重启 App 后所有引用导入的书都读不了」。
     */
    override suspend fun persistUriPermission(request: ImportRequest) = withContext(IoDispatcher) {
        runCatching {
            val flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(Uri.parse(request.uri), flags)
        }.getOrDefault(Unit)
    }

    override suspend fun computeDedupHash(tempFile: String, sizeBytes: Long): String = withContext(IoDispatcher) {
        val file = File(tempFile)
        val buffer = ByteArray(DEDUP_SAMPLE_BYTES)
        val read = file.inputStream().use { input -> input.read(buffer) }
        dedupHash(sizeBytes, buffer.copyOf(maxOf(read, 0)))
    }

    override suspend fun cleanupTemp() = withContext(IoDispatcher) {
        File(context.cacheDir, "import").deleteRecursively()
        Unit
    }

    override suspend fun freeSpaceMb(): Long = withContext(IoDispatcher) {
        context.filesDir.usableSpace / (1024 * 1024)
    }

    companion object {
        private const val COPY_BUFFER = 8192
        private const val BOOKS_DIR = "books"
        private const val DEDUP_SAMPLE_BYTES = 65536
    }
}
