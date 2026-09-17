/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/common/build.gradle.kts
 * @Description: 纯 Kotlin 通用工具模块：调度器、安全调用、哈希工具，确保可被任意层依赖而不引入 Android
 */
plugins {
    id("kr.jvm.library")
}

dependencies {
    api(libs.coroutines.core)
}
