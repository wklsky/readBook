/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-23 10:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-23 10:00
 * @FilePath: data/sources/src/main/kotlin/com/kr/reader/data/sources/ChapterCacheSchedulerImpl.kt
 * @Description: 缓存任务调度：同一本书只保留一个任务，断网自动暂停，失败线性退避重试
 */
package com.kr.reader.data.sources

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.kr.reader.core.common.IoDispatcher
import com.kr.reader.data.sources.ChapterCacheWorker.Companion.KEY_BOOK_ID
import com.kr.reader.data.sources.ChapterCacheWorker.Companion.KEY_FROM
import com.kr.reader.data.sources.ChapterCacheWorker.Companion.KEY_TO
import com.kr.reader.data.sources.ChapterCacheWorker.Companion.WORK_STATUS_TIMEOUT_MS
import com.kr.reader.domain.repository.ChapterCacheScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext

@Singleton
class ChapterCacheSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : ChapterCacheScheduler {

    private val workManager: WorkManager get() = WorkManager.getInstance(context)

    override suspend fun enqueue(bookId: Long, fromIndex: Int, toIndex: Int): Boolean = withContext(IoDispatcher) {
        if (bookId <= 0) return@withContext false
        val request = OneTimeWorkRequestBuilder<ChapterCacheWorker>()
            // 必须要求联网：断网时让任务挂起，而不是抓一堆失败把书源健康度打到 BROKEN
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .setInputData(
                workDataOf(
                    KEY_BOOK_ID to bookId,
                    KEY_FROM to fromIndex,
                    KEY_TO to toIndex,
                ),
            )
            .setBackoffCriteria(BackoffPolicy.LINEAR, BACKOFF_INTERVAL_MS, TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniqueWork(uniqueName(bookId), ExistingWorkPolicy.REPLACE, request)
        true
    }

    override suspend fun cancel(bookId: Long): Unit = withContext(IoDispatcher) {
        workManager.cancelUniqueWork(uniqueName(bookId))
    }

    override suspend fun isRunning(bookId: Long): Boolean = withContext(IoDispatcher) {
        // 同步短等待取状态：调用方只想知道「是否在跑」，引入 LiveData 观察会让调用链复杂化
        runCatching {
            val future = workManager.getWorkInfosForUniqueWork(uniqueName(bookId))
            val infos = future.get(WORK_STATUS_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            infos.any { !it.state.isFinished }
        }.getOrDefault(false)
    }

    private fun uniqueName(bookId: Long): String = "chapter_cache_$bookId"

    private companion object {
        const val BACKOFF_INTERVAL_MS = 10_000L
    }
}
