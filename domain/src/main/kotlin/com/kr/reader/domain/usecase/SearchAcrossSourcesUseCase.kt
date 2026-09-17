/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: domain/src/main/kotlin/com/kr/reader/domain/usecase/SearchAcrossSourcesUseCase.kt
 * @Description: 聚合搜索（第 4 卷 4.8.1）：并发书源 → 结果合并去重 → 按健康度排序。
 *               单个源超时/异常不能影响其它源，因此每个 async 内部自带 supervisorScope。
 */
package com.kr.reader.domain.usecase

import com.kr.reader.core.model.source.SourceSearchItem
import com.kr.reader.domain.repository.SourceEngine
import com.kr.reader.domain.repository.SourceRepository
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.supervisorScope

class SearchAcrossSourcesUseCase @Inject constructor(
    private val sourceRepository: SourceRepository,
    private val sourceEngine: SourceEngine,
    private val resultMerger: ResultMerger,
) {

    suspend operator fun invoke(
        keyword: String,
        concurrency: Int = DEFAULT_CONCURRENCY,
    ): List<MergedSearchResult> {
        val query = keyword.trim()
        if (query.length < MIN_KEYWORD_LENGTH) return emptyList()
        val sources = sourceRepository.enabledSourcesSortedByHealth()
        if (sources.isEmpty()) return emptyList()

        val results: List<SourceSearchItem> = coroutineScope {
            sources.chunked(concurrency.coerceIn(1, MAX_CONCURRENCY)).flatMap { batch ->
                batch.map { source ->
                    async {
                        supervisorScope {
                            runCatching { sourceEngine.search(source, query) }.getOrDefault(emptyList())
                        }
                    }
                }.awaitAll().flatten()
            }
        }
        return resultMerger.merge(results)
    }

    suspend fun raw(keyword: String, concurrency: Int = DEFAULT_CONCURRENCY): List<SourceSearchItem> {
        val merged = invoke(keyword, concurrency)
        return merged.flatMap { it.sources }
    }

    companion object {
        const val DEFAULT_CONCURRENCY = 6
        const val MAX_CONCURRENCY = 12
        const val MIN_KEYWORD_LENGTH = 1
    }
}

class SwitchSourceUseCase @Inject constructor(
    private val sourceEngine: SourceEngine,
) {
    suspend operator fun invoke(bookId: Long, targetSourceId: Long) =
        sourceEngine.switchSource(bookId, targetSourceId)
}
