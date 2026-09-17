/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: domain/build.gradle.kts
 * @Description: 领域层：Repository 接口 + UseCase。保持纯 Kotlin 以便 Linux-JVM 也能跑单元测试
 */
plugins {
    id("kr.jvm.library")
    id("kr.serialization")
}

dependencies {
    api(projects.core.model)
    api(projects.core.common)
}
