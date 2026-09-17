/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/network/build.gradle.kts
 * @Description: 网络基础层：OkHttp 单例、UA 演进、域名压测器
 */
plugins {
    id("kr.android.library")
    id("kr.hilt")
}

android {
    namespace = "com.kr.reader.core.network"
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.common)

    api(libs.okhttp)
    api(libs.okhttp.logging)
}
