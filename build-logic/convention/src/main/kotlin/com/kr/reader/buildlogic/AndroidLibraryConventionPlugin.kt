/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: build-logic/convention/src/main/kotlin/com/kr/reader/buildlogic/AndroidLibraryConventionPlugin.kt
 * @Description: Android Library 约定插件：所有 data/core/feature 模块的基线配置
 */
package com.kr.reader.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        pluginManager.apply("org.jetbrains.kotlin.android")

        extensions.configure<LibraryExtension>("android") {
            configureAndroidCommon(this)
            // 单元测试默认返回 stub 值，避免 Robolectric 缺席导致 Log/Uri 调用直接抛异常
            testOptions {
                unitTests.isReturnDefaultValues = true
            }
        }
    }
}
