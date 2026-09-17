/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: domain/src/main/kotlin/com/kr/reader/domain/usecase/PaginateChapterUseCase.kt
 * @Description: 章节分页（第 4 卷 4.3.1）：净化 → 进入 PaginationEngine → 结果写入 states ar** Android
 */
package com.kr.reader.domain.usecase

import com.kr.reader.core.model.page.PageSnapshot
import com.kr.reader.core.model.page.PaginationRequest
import com.kr.reader.core.model.page.PageLineDescriptor
import com.kr.reader.core.model.page.PageStyleSpec
import com.kr.reader.domain.repository.ChapterPaginator
import javax.inject.Inject

class PaginateChapterUseCase @Inject constructor(
    private val paginator: ChapterPaginator,
    private val purifyContent: PurifyContentUseCase,
) {

    suspend operator fun invoke(
        bookId: Long,
        chapterIndex: Int,
        content: String,
        pageSpec: PageStyleSpec,
        contentWidthPx: Float,
        contentHeightPx: Float,
        previousDescriptor: PageLineDescriptor? = null,
        regenerate: Boolean = false,
    ): List<PageSnapshot> {
        if (contentWidthPx <= 0f || contentHeightPx <= 0f) return emptyList()
        val safe = purifyContent(content)
        return paginator.paginate(
            request = PaginationRequest(
                bookId = bookId,
                chapterIndex = chapterIndex,
                content = safe,
                pageSpec = pageSpec,
                contentWidthPx = contentWidthPx,
                contentHeightPx = contentHeightPx,
                previousDescriptor = previousDescriptor,
                regenerate = regenerate,
            ),
        )
    }
}
