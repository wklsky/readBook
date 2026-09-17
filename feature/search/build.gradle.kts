/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 15:30
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 15:30
 * @FilePath: feature/search/build.gradle.kts
 * @Description: 搜索模块：V0.3 起接入聚合搜索 → 详情 → 加入书架（WBS 3.x / 4.x）
 */
plugins {
    id("kr.android.library")
    id("kr.compose")
    id("kr.hilt")
}

android {
    namespace = "com.kr.reader.feature.search"
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.common)
    implementation(projects.core.designsystem)
    implementation(projects.core.ui)
    implementation(projects.domain)
}
