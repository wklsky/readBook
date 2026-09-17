/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: build-logic/settings.gradle.kts
 * @Description: 复合构建 build-logic 的独立设置：声明依赖析取仓库与自身版本目录
 */
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
include(":convention")
