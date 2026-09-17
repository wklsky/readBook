/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: data/settings/src/main/kotlin/com/kr/reader/data/settings/FontRepositoryImpl.kt
 * @Description: 自定义正文字体管理：校验文件头 → 复制到私有目录 → 维护清单
 */
package com.kr.reader.data.settings

import android.content.Context
import android.net.Uri
import com.kr.reader.core.common.IoDispatcher
import com.kr.reader.core.model.FontEntry
import com.kr.reader.domain.repository.FontRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext

@Singleton
class FontRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : FontRepository {

    override suspend fun list(): List<FontEntry> = withContext(IoDispatcher) {
        fontDir().listFiles()
            ?.filter { it.isFile && it.extension.lowercase(Locale.US) in SUPPORTED_EXTENSIONS }
            ?.map { file ->
                FontEntry(
                    id = file.nameWithoutExtension,
                    displayName = file.nameWithoutExtension.substringAfter('_', file.nameWithoutExtension),
                    path = file.absolutePath,
                    sizeBytes = file.length(),
                    importedAt = file.lastModified(),
                )
            }
            .orEmpty()
            .sortedBy { it.displayName }
    }

    override suspend fun importFromUri(uri: String, displayName: String): Result<FontEntry> = withContext(IoDispatcher) {
        runCatching {
            val dir = fontDir()
            val parsed = Uri.parse(uri)
            val temp = File(context.cacheDir, "font_import_${System.currentTimeMillis()}")
            context.contentResolver.openInputStream(parsed)?.use { input ->
                temp.outputStream().use { output -> input.copyTo(output, 8192) }
            } ?: throw java.io.FileNotFoundException(uri)

            // TTF 文件必须以 0x00010000 / 'true' / 'ttcf' 开头；OTF 以 'OTTO' 开头。
            // 这一步能挡住绝大多数「用户选错文件」的场景，比 Typeface 解析失败后再报错体验好得多
            val headerValid = validateFontHeader(temp)
            if (!headerValid) {
                temp.delete()
                throw IllegalArgumentException("不是有效的字体文件")
            }
            val id = "${
                displayName.substringBeforeLast('.', displayName).hashCode()
            }_${displayName.substringBeforeLast('.', displayName)}"
            val target = File(dir, "$id.${parsed.lastPathSegment?.substringAfterLast('.') ?: "ttf"}")
            temp.copyTo(target, overwrite = true)
            temp.delete()
            FontEntry(
                id = id,
                displayName = displayName.substringBeforeLast('.', displayName),
                path = target.absolutePath,
                sizeBytes = target.length(),
                importedAt = System.currentTimeMillis(),
            )
        }
    }

    override suspend fun remove(id: String) = withContext(IoDispatcher) {
        fontDir().listFiles()?.firstOrNull { it.nameWithoutExtension == id }?.delete()
        Unit
    }

    override suspend fun pathOf(fontFamilyId: String): String? = withContext(IoDispatcher) {
        fontDir().listFiles()?.firstOrNull { it.nameWithoutExtension == fontFamilyId }?.absolutePath
    }

    private fun fontDir(): File = File(context.filesDir, FONT_DIR).apply { if (!exists()) mkdirs() }

    private fun validateFontHeader(file: File): Boolean {
        if (file.length() < 4) return false
        val header = ByteArray(4)
        file.inputStream().use { it.read(header) }
        val tag = String(header, Charsets.US_ASCII)
        return tag == "OTTO" ||
            (header[0] == 0x00.toByte() && header[1] == 0x01.toByte() && header[2] == 0x00.toByte() && header[3] == 0x00.toByte()) ||
            tag.startsWith("true") ||
            tag.startsWith("ttcf")
    }

    companion object {
        private const val FONT_DIR = "fonts"
        private val SUPPORTED_EXTENSIONS = listOf("ttf", "otf", "ttc")
    }
}
