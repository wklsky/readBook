/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/model/src/main/kotlin/com/kr/reader/core/model/Chapter.kt
 * @Description: 章节模型。start/endOffset 本地书为字符偏移，PDF 退化为页码，网络书为 0
 */
package com.kr.reader.core.model

data class Chapter(
    val id: Long = 0,
    val bookId: Long,
    val index: Int,
    val title: String,
    /** 相对全书文本流的起始字符偏移；PDF 中语义为页码 */
    val startOffset: Long,
    val endOffset: Long,
    val charCount: Int = 0,
    val isCached: Boolean = false,
    /** 网络书章节详情页 URL */
    val sourceUrl: String? = null,
    /** 卷标题（仅目录分组，无正文） */
    val isVolume: Boolean = false,
)
