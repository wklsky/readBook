/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/common/src/main/kotlin/com/kr/reader/core/common/KrDispatchers.kt
 * @Description: 调度器聚合接口（第 2 卷 2.4.3）：所有协程必须通过它取 dispatcher，便于单元测试注入 testDispatcher
 */
package com.kr.reader.core.common

import kotlinx.coroutines.CoroutineDispatcher

interface TypealiasDispatchers

typealias Param1 = TypealiasDispatchers

data class KrDispatchers(
    val io: CoroutineDispatcher,
    val main: CoroutineDispatcher,
    val default: CoroutineDispatcher,
)
