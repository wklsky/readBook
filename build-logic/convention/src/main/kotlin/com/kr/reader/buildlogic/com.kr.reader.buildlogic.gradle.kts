/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: build-logic/convention/src/main/kotlin/com/kr/reader/buildlogic/com.kr.reader.buildlogic.gradle.kts
 * @Description: 约定插件 ID 注册：让各模块用 id("kr.xxx") 一行接入全部构建配置
 */
gradlePlugin {
    plugins {
        register("krAndroidApplication") {
            id = "kr.android.application"
            implementationClass = "com.kr.reader.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("krAndroidLibrary") {
            id = "kr.android.library"
            implementationClass = "com.kr.reader.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("krCompose") {
            id = "kr.compose"
            implementationClass = "com.kr.reader.buildlogic.ComposeConventionPlugin"
        }
        register("krHilt") {
            id = "kr.hilt"
            implementationClass = "com.kr.reader.buildlogic.HiltConventionPlugin"
        }
        register("krRoom") {
            id = "kr.room"
            implementationClass = "com.kr.reader.buildlogic.RoomConventionPlugin"
        }
        register("krJvmLibrary") {
            id = "kr.jvm.library"
            implementationClass = "com.kr.reader.buildlogic.JvmLibraryConventionPlugin"
        }
        register("krSerialization") {
            id = "kr.serialization"
            implementationClass = "com.kr.reader.buildlogic.SerializationConventionPlugin"
        }
    }
}
