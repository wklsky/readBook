/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 15:30
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 15:30
 * @FilePath: feature/settings/build.gradle.kts
 * @Description: 设置模块：V0.1 最小版（主题切换）→ V0.2 排版 12 项参数面板（WBS 1.18 / 2.x）
 */
plugins {
    id("kr.android.library")
    id("kr.compose")
    id("kr.hilt")
}

android {
    namespace = "com.kr.reader.feature.settings"
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.common)
    implementation(projects.core.designsystem)
    implementation(projects.core.ui)
    implementation(projects.domain)
}
