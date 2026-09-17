/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/model/src/main/kotlin/com/kr/reader/core/model/Book.kt
 * @Description: 书籍领域模型：书架条目的一行记录，同时覆盖本地书与网络书两类来源
 */
package com.kr.reader.core.model

data class Book(
    val id: Long = 0,
    val title: String,
    val author: String? = null,
    /** 本地封面文件路径（EPUB 抽取 / PDF 首页渲染 / 网络下载后落地） */
    val coverPath: String? = null,
    /** 网络书封面 URL */
    val coverUrl: String? = null,
    val format: BookFormat,
    /** 归档后的私有路径；REFERENCE 导入模式或网络书为 null */
    val filePath: String? = null,
    val fileSize: Long = 0,
    val originalName: String = "",
    val encoding: String? = null,
    val chapterRuleId: String? = null,
    val groupId: Long? = null,
    val sortOrder: Int = 0,
    val totalChapters: Int = 0,
    /** 全书字符数（TXT/EPUB）；PDF 无稳定文本层，记为 0 */
    val totalChars: Long = 0,
    val addedAt: Long = 0L,
    val lastReadAt: Long = 0L,
    val isFavourite: Boolean = false,
    /** 内容去重哈希：SHA1(前 64KB 内容 + 文件大小)，同书不同副本只保留一份 */
    val dedupHash: String = "",
    // ── 网络书专属字段 ────────────────────────────────
    val sourceId: Long? = null,
    val sourceBookKey: String? = null,
    val intro: String? = null,
    val latestChapterTitle: String? = null,
) {
    /** 是否网络书（需要走书源抓取链路，而非本地文件读取） */
    val isNetBook: Boolean get() = sourceId != null && sourceBookKey != null
}
