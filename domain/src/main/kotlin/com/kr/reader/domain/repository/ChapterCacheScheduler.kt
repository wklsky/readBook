/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-23 10:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-23 10:00
 * @FilePath: domain/src/main/kotlin/com/kr/reader/domain/repository/ChapterCacheScheduler.kt
 * @Description: 离线批量缓存调度端口（第 4 卷 4.8.4）：UI 只描述「缓存哪一本书的哪些章节」，不感知 WorkManager
 */
package com.kr.reader.domain.repository

interface ChapterCacheScheduler {

    /** @param toIndex 传 -1 表示一直缓存到最后一章 */
    suspend fun enqueue(bookId: Long, fromIndex: Int = 0, toIndex: Int = -1): Boolean

    suspend fun cancel(bookId: Long)

    suspend fun isRunning(bookId: Long): Boolean
}
