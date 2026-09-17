/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: data/books/build.gradle.kts
 * @Description: 书籍仓储实现层：Room DAO → 领域模型映射，SAF 文件归档与目录扫描
 */
plugins {
    id("kr.android.library")
    id("kr.hilt")
    id("kr.serialization")
}

android {
    namespace = "com.kr.reader.data.books"
}

dependencies {
    api(projects.domain)
    implementation(projects.core.database)
    implementation(projects.core.model)
    implementation(projects.core.common)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.documentfile)
}
