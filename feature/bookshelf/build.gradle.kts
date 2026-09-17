/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 15:30
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 15:30
 * @FilePath: feature/bookshelf/build.gradle.kts
 * @Description: 书架模块：懒列表 + 封面占位 + 排序 + 导入入口（WBS 1.16）
 */
plugins {
    id("kr.android.library")
    id("kr.compose")
    id("kr.hilt")
}

android {
    namespace = "com.kr.reader.feature.bookshelf"
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.common)
    implementation(projects.core.designsystem)
    implementation(projects.core.ui)
    implementation(projects.domain)
}
