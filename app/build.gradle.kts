/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 15:30
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 15:30
 * @FilePath: app/build.gradle.kts
 * @Description: 壳工程：只负责组装 feature 模块、持有 Application/Activity 与全局导航，禁止承载业务逻辑
 */
plugins {
    id("kr.android.application")
    id("kr.compose")
    id("kr.hilt")
}

android {
    namespace = "com.kr.reader"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.model)
    implementation(projects.core.designsystem)
    implementation(projects.core.ui)
    implementation(projects.domain)

    // feature 之间禁止互相依赖（架构铁律），全部路由跳转由壳工程通过回调串联
    implementation(projects.feature.bookshelf)
    implementation(projects.feature.reader)
    implementation(projects.feature.search)
    implementation(projects.feature.sources)
    implementation(projects.feature.settings)
    implementation(projects.feature.about)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
}
