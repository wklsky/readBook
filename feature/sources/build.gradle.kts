/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 15:30
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 15:30
 * @FilePath: feature/sources/build.gradle.kts
 * @Description: 书源管理模块：V0.3 起承接书源增删改查与规则调试器（WBS 3.x）
 */
plugins {
    id("kr.android.library")
    id("kr.compose")
    id("kr.hilt")
}

android {
    namespace = "com.kr.reader.feature.sources"
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.common)
    implementation(projects.core.designsystem)
    implementation(projects.core.ui)
    implementation(projects.domain)
}
