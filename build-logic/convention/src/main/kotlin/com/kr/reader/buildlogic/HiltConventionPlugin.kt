/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: build-logic/convention/src/main/kotlin/com/kr/reader/buildlogic/HiltConventionPlugin.kt
 * @Description: Hilt 约定插件：统一 DI 依赖注入编译链，避免各模块自行配置 KSP 的版本错位
 */
package com.kr.reader.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class HiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.google.devtools.ksp")
        pluginManager.apply("com.google.dagger.hilt.android")

        dependencies {
            "implementation"(target.libs.findLibrary("hilt-android").orElseThrow().get())
            "ksp"(target.libs.findLibrary("hilt-compiler").orElseThrow().get())
        }
    }
}
