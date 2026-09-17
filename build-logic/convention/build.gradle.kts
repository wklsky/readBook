/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: build-logic/convention/build.gradle.kts
 * @Description: 约定插件模块自身依赖：AGP / Kotlin / KSP / Room / Hilt 的 Gradle 插件 API
 */
plugins {
    `kotlin-dsl`
}

group = "com.kr.reader.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.compose.gradle.plugin)
    compileOnly(libs.ksp.gradle.plugin)
    compileOnly(libs.room.gradle.plugin)
    implementation(libs.detekt.gradle.plugin)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        optIn.add("org.jetbrains.kotlin.gradle.plugin.ExperimentalKotlinGradlePluginApi")
    }
}
