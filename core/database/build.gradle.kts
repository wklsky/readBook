/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/database/build.gradle.kts
 * @Description: Room 持久层：Schema mappers 的 entities/DAOs 都必须在此识别
 */
plugins {
    id("kr.android.library")
    id("kr.hilt")
    id("kr.room")
    id("kr.serialization")
}

android {
    namespace = "com.kr.reader.core.database"
}

dependencies {
    api(projects.core.model)
    implementation(projects.core.common)
}
