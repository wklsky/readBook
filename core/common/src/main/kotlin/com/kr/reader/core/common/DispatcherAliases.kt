/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/common/src/main/kotlin/com/kr/reader/core/common/DispatcherAliases.kt
 * @Description: 业务语义调度器别名（第 1 卷 1.3.3）：Default=排版/TextMeasurer 专用、IO=文件与网络
 */
package com.kr.reader.core.common

import kotlinx.coroutines.Dispatchers

/**
 * 排版计算（StaticLayout / TextMeasurer 抢占式换行）是 CPU 密集任务，
 * 显式使用有限并行度的 Default.Bounded 可避免一次加载多章时把线程池打满，
 * 进而拖慢同属 Default 线程池的其他 CPU 任务。
 */
val DefaultDispatcher get() = Dispatchers.Default.limitedParallelism(2)

val IoDispatcher get() = Dispatchers.IO.limitedParallelism(4)
