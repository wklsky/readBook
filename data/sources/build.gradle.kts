/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-23 10:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-23 10:00
 * @FilePath: data/sources/build.gradle.kts
 * @Description: 书源数据层：书源 JSON 编解码与校验、规则解释引擎、健康度治理、章节离线缓存
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
    // database / network 用 api 而非 implementation：本模块的 @Inject 构造与 Hilt 绑定会把
    // Dao、OkHttp 提供器类型带进 app 模块的 DI 图，传递性不足会导致 app 编译期找不到符号
    api(projects.core.database)
    api(projects.core.network)
    implementation(projects.core.model)
    implementation(projects.core.common)

    implementation(libs.androidx.core.ktx)
    implementation(libs.jsoup)
    implementation(libs.okhttp)
    implementation(libs.work.runtime.ktx)

    // 规则引擎与匹配器是纯 Kotlin/jsoup 逻辑，放在 JVM 单测里跑，无需 Robolectric
    testImplementation(libs.junit)
    testImplementation(libs.truth)
}
