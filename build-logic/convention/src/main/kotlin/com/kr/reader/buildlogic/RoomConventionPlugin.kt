/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: build-logic/convention/src/main/kotlin/com/kr/reader/buildlogic/RoomConventionPlugin.kt
 * @Description: Room 约定插件：强制导出 schema 到版本控制目录，迁移可审计（第 3 卷 3.5.1 迁移纪律）
 */
package com.kr.reader.buildlogic

import androidx.room.gradle.RoomExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class RoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("androidx.room")
        pluginManager.apply("com.google.devtools.ksp")

        extensions.configure<RoomExtension> {
            schemaDirectory("$projectDir/schemas")
        }

        dependencies {
            "implementation"(target.libs.findLibrary("room-runtime").orElseThrow().get())
            "implementation"(target.libs.findLibrary("room-ktx").orElseThrow().get())
            "ksp"(target.libs.findLibrary("room-compiler").orElseThrow().get())
        }
    }
}
