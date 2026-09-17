/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: build-logic/convention/src/main/kotlin/com/kr/reader/buildlogic/KrAndroidCommon.kt
 * @Description: 约定插件共享常量与公共 Android 配置，保证所有模块 compileSdk/minSdk/Java 版本完全一致
 */
package com.kr.reader.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// 版本目录访问器：约定插件的 .kt 文件不会生成 libs 类型安全访问器（那是 .gradle.kts 的能力），
// 必须手动桥接 VersionCatalogsExtension，否则所有 findLibrary 调用点无法编译
internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

/**
 * 项目级构建常量。
 * 之所以集中在此而非常量散落各处：改 compileSdk 必须一次生效于全部 14 个模块，
 * 否则 Room 注解处理器与 Compose 编译器版本错位会导致难以定位的编译错误。
 */
object KrBuildConfig {
    const val COMPILE_SDK = 35
    const val MIN_SDK = 26
    const val TARGET_SDK = 34
    const val JVM_TARGET = "17"
}

internal fun Project.configureKotlinJvmTarget() {
    tasks.withType(KotlinCompile::class.java).configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(KrBuildConfig.JVM_TARGET))
            // 阅读器大量使用 StateFlow + 挂起函数，开启协程的上下文参数透传以获得更好的调试栈
            freeCompilerArgs.addAll(
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=kotlinx.coroutines.FlowPreview",
            )
        }
    }
}

internal fun Project.configureAndroidCommon(extension: CommonExtension<*, *, *, *, *, *>) {
    extension.compileSdk = KrBuildConfig.COMPILE_SDK
    extension.defaultConfig {
        minSdk = KrBuildConfig.MIN_SDK
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    extension.compileOptions {
        sourceCompatibility = JavaVersion.valueOf("VERSION_${KrBuildConfig.JVM_TARGET}")
        targetCompatibility = JavaVersion.valueOf("VERSION_${KrBuildConfig.JVM_TARGET}")
        isCoreLibraryDesugaringEnabled = false
    }
    // 非传递 R 类已在 gradle.properties 全局开启，禁止跨模块引用资源 id（否则 feature 之间会产生隐式耦合）
    configureKotlinJvmTarget()
}
