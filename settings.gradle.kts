/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: settings.gradle.kts
 * @Description: 根工程模块清单与依赖仓库声明，按第 1 卷 1.2 的 14 模块结构组织
 */
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// includeBuild：把 build-logic 作为复合构建引入，约定插件才能被 root 的 build.gradle.kts 解析
includeBuild("build-logic")

rootProject.name = "KotlinReader"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")

include(":core:model")
include(":core:common")
include(":core:database")
include(":core:datastore")
include(":core:network")
include(":core:designsystem")
include(":core:ui")

include(":domain")

include(":data:books")
include(":data:parser")
include(":data:pagination")
include(":data:sources")
include(":data:settings")

include(":feature:bookshelf")
include(":feature:reader")
include(":feature:search")
include(":feature:sources")
include(":feature:settings")
include(":feature:about")
