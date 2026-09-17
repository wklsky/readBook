/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 15:30
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 15:30
 * @FilePath: data/sources/build.gradle.kts
 * @Description: 书源实现层：SourceJsonCodec / SourceRepositoryImpl / SourceEngineImpl / 缓存 Worker（V0.3 起实现）
 */
plugins {
    id("kr.android.library")
    id("kr.hilt")
    id("kr.serialization")
}

android {
    namespace = "com.kr.reader.data.sources"
}

dependencies {
    api(projects.domain)
    implementation(projects.core.model)
    implementation(projects.core.common)
    implementation(projects.core.network)

    // 规则引擎基于 CSS 选择器抽取正文，jsoup 是书源规则的事实执行器
    implementation(libs.jsoup)
    implementation(libs.okhttp)
    // 离线章节缓存走 WorkManager 后台调度
    implementation(libs.work.runtime.ktx)
}
