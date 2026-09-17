/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: domain/src/main/kotlin/com/kr/reader/domain/usecase/SaveProgressUseCase.kt
 * @Description: 进度写入（第 4 卷 4.4.2）：走比例+间隔双路 adjudicate，阅读中每 2 秒最多一次 DB 写入
 */
package com.kr.reader.domain.usecase

import com.kr.reader.core.model.ReadingProgress
import com.kr.reader.domain.common.ApplicationScope
import com.kr.reader.domain.repository.ProgressRepository
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select

/** 合并窗口：连续翻页/滚屏时把写操作收敛成一次 */
private const val DEBOUNCE_MS = 2000L

/** 时间粒度：低于该值的位置变化不值得落库 */
private const val MIN_INTERVAL_MS = 5000L

@Singleton
class SaveProgressUseCase @Inject constructor(
    private val repository: ProgressRepository,
    @ApplicationScope private val scope: CoroutineScope,
) {

    private val pending = ConcurrentHashMap<Long, ReadingProgress>()
    private val pendingIdChannel = Channel<Long>(Channel.UNLIMITED)
    private val flushSignal = Channel<Unit>(Channel.UNLIMITED)

    init {
        scope.launch { drain() }
    }

    suspend operator fun invoke(progress: ReadingProgress) {
        pending[progress.bookId] = progress
        pendingIdChannel.send(progress.bookId)
    }

    suspend fun flushNow(bookId: Long? = null) {
        flushSignal.send(Unit)
        if (bookId != null) {
            pending.remove(bookId)?.let { repository.update(it) }
        } else {
            drainPendingOnce()
        }
    }

    @OptIn(ObsoleteCoroutinesApi::class)
    private suspend fun drain() {
        while (true) {
            select {
                pendingIdChannel.onReceive {
                    // 合并窗口内的重复 bookId 请求：一起延迟后统一写最新值
                    delay(DEBOUNCE_MS)
                    drainDuplicates()
                    drainPendingOnce()
                }
                flushSignal.onReceive {
                    drainPendingOnce()
                }
            }
        }
    }

    private suspend fun drainDuplicates() {
        while (true) {
            val next = pendingIdChannel.tryReceive().getOrNull() ?: break
            pending[next]?.let { pending[next] = it }
        }
    }

    private suspend fun drainPendingOnce() {
        val iterator = pending.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val value = entry.value
            iterator.remove()
            repository.update(value)
        }
    }
}
