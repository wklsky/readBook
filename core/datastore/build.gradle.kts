/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/datastore/build.gradle.kts
 * @Description: DataStore 封装层：以 kotlinx.serialization JSON 替代 Proto DataStore，避免引入 protoc 工具链
 */
plugins {
    id("kr.android.library")
    id("kr.hilt")
    id("kr.serialization")
}

android {
    namespace = "com.kr.reader.core.datastore"
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.common)

    api(libs.datastore)
    api(libs.datastore.preferences)
    api(libs.coroutines.core)
}
