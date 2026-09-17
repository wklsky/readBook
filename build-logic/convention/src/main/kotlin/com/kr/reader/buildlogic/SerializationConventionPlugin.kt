/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: build-logic/convention/src/main/kotlin/com/kr/reader/buildlogic/SerializationConventionPlugin.kt
 * @Description: kotlinx.serialization 约定插件，用于书源 JSON 规则与 DataStore 配置序列化
 */
package com.kr.reader.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class SerializationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")
        dependencies {
            "implementation"(target.libs.findLibrary("kotlinx-serialization-json").orElseThrow().get())
        }
    }
}
