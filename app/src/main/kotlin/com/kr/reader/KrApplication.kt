/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 15:30
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 15:30
 * @FilePath: app/src/main/kotlin/com/kr/reader/KrApplication.kt
 * @Description: 应用入口：仅承担 Hilt 依赖图根节点职责，V0.3 接入 WorkManager 时在此追加 Configuration.Provider
 */
package com.kr.reader

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class KrApplication : Application()
