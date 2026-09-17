/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: data/pagination/build.gradle.kts
 * @Description: 分页模块：用 Compose TextMeasurer 做真实换行测量，是阅读器翻页排版的核心
 */
plugins {
    id("kr.android.library")
    id("kr.compose")
    id("kr.hilt")
}

android {
    namespace = "com.kr.reader.data.pagination"
}

dependencies {
    api(projects.domain)
    implementation(projects.core.model)
    implementation(projects.core.common)
    implementation(projects.core.designsystem)
}
