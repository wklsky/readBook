/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: data/settings/build.gradle.kts
 * @Description: 设置仓储实现：配置 DataStore + 自定义字体文件管理
 */
plugins {
    id("kr.android.library")
    id("kr.hilt")
    id("kr.serialization")
}

android {
    namespace = "com.kr.reader.data.settings"
}

dependencies {
    api(projects.domain)
    implementation(projects.core.datastore)
    implementation(projects.core.model)
    implementation(projects.core.common)

    implementation(libs.androidx.core.ktx)
}
