/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: data/parser/build.gradle.kts
 * @Description: 文件解析层：TXT 编码检测/目录识别/字符偏移索引、EPUB 容器解析、PDF 页面渲染
 */
plugins {
    id("kr.android.library")
    id("kr.hilt")
    id("kr.serialization")
}

android {
    namespace = "com.kr.reader.data.parser"
}

dependencies {
    api(projects.domain)
    implementation(projects.core.model)
    implementation(projects.core.common)
    implementation(projects.core.database)

    implementation(libs.androidx.core.ktx)
    implementation(libs.jsoup)
}
