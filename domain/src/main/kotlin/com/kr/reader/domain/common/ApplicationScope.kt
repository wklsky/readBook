/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: domain/src/main/kotlin/com/kr/reader/domain/common/ApplicationScope.kt
 * @Description: 应用级 CoroutineScope 限定符。之所以存在：进度写入需要在 ViewModel 销毁后继续完成消息并
 */
package com.kr.reader.domain.common

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
