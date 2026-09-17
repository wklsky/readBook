/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: build-logic/convention/src/main/kotlin/com/kr/reader/buildlogic/JvmLibraryConventionPlugin.kt
 * @Description: 纯 Kotlin 模块约定插件：core:model / core:common 必须是无 Android 依赖的 java-library，
 *               这条是被架构守护（第 2 卷 2.7）机械保证的铁律
 */
package com.kr.reader.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("java-library")
        pluginManager.apply("org.jetbrains.kotlin.jvm")

        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.valueOf("VERSION_${KrBuildConfig.JVM_TARGET}")
            targetCompatibility = JavaVersion.valueOf("VERSION_${KrBuildConfig.JVM_TARGET}")
        }
        extensions.configure<KotlinJvmProjectExtension> {
            compilerOptions {
                jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(KrBuildConfig.JVM_TARGET))
            }
        }
    }
}
