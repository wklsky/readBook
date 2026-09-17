/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/model/src/main/kotlin/com/kr/reader/core/model/ReadingProgress.kt
 * @Description: 字符级阅读进度模型（第 3 卷 3.1.2，全项目最重要的设计决策）
 */
package com.kr.reader.core.model

import kotlinx.serialization.Serializable

/**
 * @property chapterIndex 章节序号，仅用于快速定位，不用于精确定位
 * @property chapterTitle 章节标题快照，换源时作为匹配依据，也是用户的心智锚点
 * @property charOffsetInChapter 章内字符偏移（恢复定位的唯一黄金标准）
 * @property globalCharOffset 全书绝对偏移缓存值，仅用于本地书快速跳转与百分比展示
 * @property percent 全书百分比，仅用于 UI 展示，绝不用于恢复定位
 * @property chapterPercent 本章已读比例，用于状态栏「本章 42%」
 */
@Serializable
data class ReadingProgress(
    val bookId: Long,
    val chapterIndex: Int,
    val chapterTitle: String,
    val charOffsetInChapter: Int,
    val globalCharOffset: Long,
    val percent: Float,
    val chapterPercent: Float,
    val readingSeconds: Long = 0L,
    val updatedAt: Long = 0L,
) {
    companion object {
        /** 首次打开书籍时的初始进度 */
        fun initial(bookId: Long, chapter: Chapter?): ReadingProgress = ReadingProgress(
            bookId = bookId,
            chapterIndex = chapter?.index ?: 0,
            chapterTitle = chapter?.title.orEmpty(),
            charOffsetInChapter = 0,
            globalCharOffset = chapter?.startOffset ?: 0L,
            percent = 0f,
            chapterPercent = 0f,
            updatedAt = 0L,
        )
    }
}
