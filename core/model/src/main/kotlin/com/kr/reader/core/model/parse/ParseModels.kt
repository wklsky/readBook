/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/model/src/main/kotlin/com/kr/reader/core/model/parse/ParseModels.kt
 * @Description: 文件解析层对外契约（第 4 卷 4.2.1）：三种格式的解析器统一返回本文件定义的三个类型
 */
package com.kr.reader.core.model.parse

import com.kr.reader.core.model.BookFormat

/** 书籍元数据（标题/作者/封面在导入时由 UI 可覆盖） */
data class BookMetadata(
    val title: String,
    val author: String? = null,
    val coverPath: String? = null,
    val intro: String? = null,
    val chapterRuleId: String? = null,
)

/** 章节元信息：偏移定位的根 */
data class ChapterMeta(
    val index: Int,
    val title: String,
    val startOffset: Long,
    val endOffset: Long,
    val charCount: Int,
    val isVolume: Boolean = false,
)

/** TXT 目录序号命中信息 */
data class ChapterHit(
    val title: String,
    val charOffset: Long,
    val index: Int,
)

/** 解析入口 —— 所有解析器只暴露三个方法 */
interface BookParser {
    val supportedFormat: BookFormat

    suspend fun getMetadata(input: FileInput): BookMetadata

    suspend fun extractChapterList(input: FileInput): List<ChapterMeta>

    /** 只读取某一章内容，避免整书加载导致 10MB 文件的 OOM */
    suspend fun readChapter(input: FileInput, chapter: ChapterMeta): String

    fun close(input: FileInput)
}

/** 解析输入：本地临时文件 + 可选编码 */
data class FileInput(
    val tempFilePath: String,
    val displayName: String,
    val encoding: String? = null,
    /** PDF 页码模式 */
    val pageIndex: Int? = null,
)

/** 解析结果（第 2 卷 2.5 分层错误处理） */
data class ParseResult<T>(
    val value: T,
    val messages: List<String> = emptyList(),
    val summary: Map<String, String>? = null,
    val usedChapterRuleId: String? = null,
) {
    companion object {
        fun <T> success(value: T): ParseResult<T> = ParseResult(value)
    }
}
