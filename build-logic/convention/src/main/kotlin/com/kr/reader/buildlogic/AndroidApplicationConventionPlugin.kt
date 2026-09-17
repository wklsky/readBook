/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: build-logic/convention/src/main/kotlin/com/kr/reader/buildlogic/AndroidApplicationConventionPlugin.kt
 * @Description: app 壳工程约定插件：统一 Android Application 配置与构建变体（含 benchmark 变体与 ABI 拆分）
 */
package com.kr.reader.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        pluginManager.apply("org.jetbrains.kotlin.android")

        extensions.configure<ApplicationExtension>("android") {
            defaultConfig {
                applicationId = "com.kr.reader"
                targetSdk = KrBuildConfig.TARGET_SDK
                versionCode = 1
                versionName = "1.0.0"
                resourceConfigurations += setOf("zh-rCN", "en")
            }

            buildTypes {
                debug {
                    applicationIdSuffix = ".debug"
                    versionNameSuffix = "-debug"
                    isMinifyEnabled = false
                    buildConfigField("boolean", "LOG_ENABLED", "true")
                    buildConfigField("boolean", "SOURCE_DEBUGGER", "true")
                }
                release {
                    isMinifyEnabled = true
                    isShrinkResources = true
                    proguardFiles(
                        getDefaultProguardFile("proguard-android-optimize.txt"),
                        "proguard-rules.pro",
                    )
                    buildConfigField("boolean", "LOG_ENABLED", "false")
                    buildConfigField("boolean", "SOURCE_DEBUGGER", "false")
                }
                create("benchmark") {
                    initWith(getByName("release"))
                    signingConfig = signingConfigs.getByName("debug")
                    matchingFallbacks += listOf("release")
                    isDebuggable = false
                    applicationIdSuffix = ".benchmark"
                }
            }

            // 按 ABI 拆分：APK ≤ 12MB 预算的关键手段
            splits {
                abi {
                    isEnable = true
                    reset()
                    include("armeabi-v7a", "arm64-v8a")
                    isUniversalApk = false
                }
            }

            packaging {
                resources {
                    excludes += setOf(
                        "/META-INF/{AL2.0,LGPL2.1}",
                        "META-INF/DEPENDENCIES",
                        "META-INF/LICENSE*",
                    )
                }
            }

            configureAndroidCommon(this)
        }
    }
}
