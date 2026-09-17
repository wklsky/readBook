/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/ui/build.gradle.kts
 * @Description: 通用 UI 组件层：多个 feature 共用的加载/空态/错误/弹窗与设置行，禁止承载业务逻辑
 */
plugins {
    id("kr.android.library")
    id("kr.compose")
    id("kr.hilt")
}

android {
    namespace = "com.kr.reader.core.ui"
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.common)
    implementation(projects.core.designsystem)

    implementation(libs.androidx.core.ktx)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
}
