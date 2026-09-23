/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-23 10:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-23 10:00
 * @FilePath: data/sources/src/main/kotlin/com/kr/reader/data/sources/ChapterCacheWorker.kt
 * @Description: 离线批量缓存 Worker（第 4 卷 4.8.4）：按章抓取落库并上报进度，单章失败不影响整批
 */
package com.kr.reader.data.sources

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.kr.reader.core.model.page.CachedChapter
import com.kr.reader.data.sources.di.CacheWorkerDeps
import dagger.hilt.android.EntryPointAccessors
import java.util.concurrent.TimeUnit

class ChapterCacheWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val bookId = inputData.getLong(KEY_BOOK_ID, INVALID_ID)
        // 只判断 INVALID_ID 会漏掉「调用方传了 0」的情况，0 不是合法的书主键
        if (bookId <= 0) return Result.failure()

        // TODO: 长任务未提升为前台服务（需要通知渠道与图标资源），国内 ROM 上可能被后台限制掐断。
        // V0.4 阶段先用普通后台任务 + 设置页提示「锁定后台」，待补齐通知资源后改为 setForeground()。
        val deps = EntryPointAccessors.fromApplication(applicationContext, CacheWorkerDeps::class.java)
        val bookRepository = deps.bookRepository()
        val book = bookRepository.getBook(bookId) ?: return Result.failure()
        val sourceId = book.sourceId ?: return Result.failure()
        val source = deps.sourceRepository().get(sourceId) ?: return Result.failure()

        val chapters = bookRepository.getChapters(bookId).filter { !it.isVolume }
        if (chapters.isEmpty()) return Result.failure()

        val fromIndex = inputData.getInt(KEY_FROM, 0)
        val requestedTo = inputData.getInt(KEY_TO, ALL_REMAINING)
        val endIndex = if (requestedTo < 0) chapters.lastIndex else requestedTo.coerceAtMost(chapters.lastIndex)
        val targets = chapters.filter { it.index in fromIndex..endIndex }
        if (targets.isEmpty()) return Result.failure()

        val cacheRepository = deps.cacheRepository()
        val sourceEngine = deps.sourceEngine()

        var done = 0
        var failed = 0
        for (chapter in targets) {
            if (isStopped) break
            val url = chapter.sourceUrl
            if (url.isNullOrBlank()) continue
            if (cacheRepository.get(bookId, chapter.index) != null) {
                done++
                continue
            }
            val content = runCatching { sourceEngine.fetchContent(source, url) }
                .onFailure { Log.w(TAG, "缓存章节失败：${chapter.title} ${it.message}") }
                .getOrNull()
            if (content.isNullOrBlank()) {
                failed++
                continue
            }
            val now = System.currentTimeMillis()
            cacheRepository.put(
                CachedChapter(
                    bookId = bookId,
                    index = chapter.index,
                    sourceId = source.id,
                    sourceUrl = url,
                    title = chapter.title,
                    content = content,
                    charCount = content.length,
                    cachedAt = now,
                    lastAccessedAt = now,
                ),
            )
            done++
            setProgress(workDataOf(KEY_PROGRESS to done, KEY_TOTAL to targets.size))
            // 每章都检查一次容量：整批缓存动辄几百章，攒到最后再淘汰会先撑爆磁盘
            cacheRepository.evictIfNeeded(CACHE_LIMIT_MB)
        }

        return if (done == 0 && failed > 0) {
            Result.failure(workDataOf(KEY_FAILED to failed))
        } else {
            Result.success(workDataOf(KEY_DONE to done, KEY_FAILED to failed))
        }
    }

    companion object {
        private const val TAG = "ChapterCacheWorker"
        private const val INVALID_ID = -1L
        private const val ALL_REMAINING = -1
        const val CACHE_LIMIT_MB = 200
        const val KEY_BOOK_ID = "cache_book_id"
        const val KEY_FROM = "cache_from"
        const val KEY_TO = "cache_to"
        const val KEY_PROGRESS = "cache_progress"
        const val KEY_TOTAL = "cache_total"
        const val KEY_DONE = "cache_done"
        const val KEY_FAILED = "cache_failed"
        val WORK_STATUS_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(2)
    }
}
