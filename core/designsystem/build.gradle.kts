/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/designsystem/build.gradle.kts
 * @Description: 设计系統层：Material3 主题、阅读器配色、排版映射与自定义字体解析
 */
plugins {
    id("kr.android.library")
    id("kr.compose")
    id("kr.hilt")
    id("kr.serialization")
}

android {
    namespace = "com.kr.reader.core.designsystem"
}

dependencies {
    api(projects.core.model)
    implementation(projects.core.common)

    implementation(libs.androidx.core.ktx)
}
