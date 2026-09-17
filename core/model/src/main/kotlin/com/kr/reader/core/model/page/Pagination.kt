/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/model/src/main/kotlin/com/kr/reader/core/model/page/Pagination.kt
 * @Description: 分页模型与纯 Kotlin 排版规格。之所以放在 model：分页结果需跨域穿透到 UI，
 *               且期望 UI 与 engine 都能引用同一份不可变快照
 */
package com.kr.reader.core.model.page

/**
 * 排版计算所需的纯数值规格。
 * 之所以拆 case 到 px 而不是直接透传 Compose TextStyle：engine 在 Default dispatcher 工作，
 * 不允许持有任何 Compose 对象（TextStyle 内部依赖 Density/Resource 环境）。
 */
data class PageStyleSpec(
    val fontSizePx: Float,
    val fontScale: Float,
    /** 行高 px（含 lineHeight 倍数） */
    val lineHeightPx: Float,
    val lineSpacingExtraPx: Float,
    val lineSpacingMultiplier: Float,
    val paragraphSpacingPx: Float,
    /** 首行缩进 px，0 表示不缩进 */
    val paragraphIndentWidth: Float,
    /** 字间距，单位 em */
    val letterSpacingEm: Float = 0f,
    /** 对齐方式：两端对齐需 Compose TextAlign.Justify */
    val align: com.kr.reader.core.model.TextAlignMode = com.kr.reader.core.model.TextAlignMode.START,
    /** 标题行额外间距倍数（目录/章节标题） */
    val titleFontSizePx: Float,
    /** 样式指纹：用于判断缓存的分页结果是否仍然有效 */
    val charStyleHash: Int,
    val fontFamilyId: String,
)

/**
 * 单行的量化描述符。
 * 样式指纹一致时可直接复用上次分页结果，避免旋转屏幕/切换章节时重复排版。
 */
data class PageLineDescriptor(
    val lineVersion: Int,
    val lineHeightPx: Float,
    val contentWidthPx: Float,
    val lineSpacingExtraPx: Float,
    val paragraphSpacingPx: Float,
    val charStyleHash: Int,
    val fontFamilyId: String,
)

data class PaginationRequest(
    val bookId: Long,
    val chapterIndex: Int,
    val content: String,
    val pageSpec: PageStyleSpec,
    val contentWidthPx: Float,
    val contentHeightPx: Float,
    /** 上一次分页得到的行描述符，用于命中缓存短路 */
    val previousDescriptor: PageLineDescriptor? = null,
    val regenerate: Boolean = false,
)

/**
 * 单页快照。displayText 为本章第 index 页的完整文本；
 * start/endCharOffset 为**章内**相对偏移，配合 ReadingProgress.charOffsetInChapter 使用。
 */
data class PageSnapshot(
    val index: Int,
    val chapterIndex: Int,
    val startCharOffset: Int,
    val endCharOffset: Int,
    val lineDescriptors: List<PageLineDescriptor>,
    val displayText: String,
    /** 超长页（>8000 字符）标记为延迟绘制，避免一次性提交超大文本造成掉帧 */
    val drawLazily: Boolean = false,
)

class PaginationException(message: String?) : Exception(message)

/** 网络书章节缓存 */
data class CachedChapter(
    val id: Long = 0,
    val bookId: Long,
    val index: Int,
    val sourceId: Long,
    val sourceUrl: String,
    val title: String,
    val content: String,
    val charCount: Int,
    val cachedAt: Long,
    val lastAccessedAt: Long,
)
