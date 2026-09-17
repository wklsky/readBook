/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: data/books/src/main/kotlin/com/kr/reader/data/books/BookScanner.kt
 * @Description: 目录扫描：首选 java.io.File 遍历（快），失败时退化为 SAF DocumentFile 遍历
 */
package com.kr.reader.data.books

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.kr.reader.core.common.IoDispatcher
import com.kr.reader.core.model.BookFormat
import com.kr.reader.core.model.import.ScanItem
import com.kr.reader.domain.repository.FileScannerGateway
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext

@Singleton
class BookScanner @Inject constructor(
    @ApplicationContext private val context: Context,
) : FileScannerGateway {

    override suspend fun scan(path: String, recursive: Boolean): List<ScanItem> = withContext(IoDispatcher) {
        val direct = File(path)
        if (direct.isDirectory) scanDirect(direct, recursive) else scanSaf(path, recursive)
    }

    private fun scanDirect(dir: File, recursive: Boolean): List<ScanItem> {
        val result = ArrayList<ScanItem>()
        dir.walkTopDown()
            .maxDepth(if (recursive) Int.MAX_VALUE else 1)
            .filter { it.isFile }
            .forEach { file ->
                BookFormat.fromExtension(file.name)?.let { format ->
                    result.add(ScanItem(file.absolutePath, file.name, file.length(), format))
                }
            }
        return result
    }

    /**
     * SAF 场景：用户授权的是一棵虚拟文档树，没有可直接访问的真实路径，
     * 只能走 DocumentFile API 逐个列举（比 File 慢约一个数量级，因此仅在非 file 路径时使用）。
     */
    private fun scanSaf(uri: String, recursive: Boolean): List<ScanItem> {
        val result = ArrayList<ScanItem>()
        val root = runCatching { DocumentFile.fromTreeUri(context, Uri.parse(uri)) }.getOrNull() ?: return result
        walk(root, recursive, result)
        return result
    }

    private fun walk(dir: DocumentFile, recursive: Boolean, out: MutableList<ScanItem>) {
        dir.listFiles().forEach { child ->
            if (child.isDirectory) {
                if (recursive) walk(child, true, out)
            } else {
                val name = child.name.orEmpty()
                BookFormat.fromExtension(name)?.let { format ->
                    out.add(ScanItem(child.uri.toString(), name, child.length(), format))
                }
            }
        }
    }

    companion object {
        fun supportedExtensions(): List<String> = listOf("txt", "epub", "pdf")

        fun isSupported(uri: Uri): Boolean = BookFormat.fromExtension(uri.lastPathSegment.orEmpty()) != null
    }
}
