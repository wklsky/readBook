/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: build-logic/convention/src/main/kotlin/com/kr/reader/buildlogic/ComposeConventionPlugin.kt
 * @Description: Compose 约定插件：统一启用 compose buildFeature 与 Compose 编译器插件，并集中 Compose 依赖
 */
package com.kr.reader.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class ComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        extensions.configure<CommonExtension<*, *, *, *, *, *>>("android") {
            buildFeatures {
                compose = true
            }
        }

        dependencies {
            "implementation"(platform(target.libs.findLibrary("compose-bom").orElseThrow().get()))
            "implementation"(target.libs.findLibrary("compose-foundation").orElseThrow().get())
            "implementation"(target.libs.findLibrary("compose-material3").orElseThrow().get())
            "implementation"(target.libs.findLibrary("compose-ui").orElseThrow().get())
            "implementation"(target.libs.findLibrary("compose-ui-tooling-preview").orElseThrow().get())
            "implementation"(target.libs.findLibrary("androidx-lifecycle-runtime-compose").orElseThrow().get())
            "debugImplementation"(target.libs.findLibrary("compose-ui-tooling").orElseThrow().get())
        }
    }
}
