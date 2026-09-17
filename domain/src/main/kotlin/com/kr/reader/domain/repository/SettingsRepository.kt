/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: domain/src/main/kotlin/com/kr/reader/domain/repository/SettingsRepository.kt
 * @Description: 配置类仓库：排版/主题/阅读偏好/全局偏好/字体，统一暴露 Flow + 原子 update
 */
package com.kr.reader.domain.repository

import com.kr.reader.core.model.FontEntry
import com.kr.reader.core.model.GlobalPrefs
import com.kr.reader.core.model.ReaderPrefs
import com.kr.reader.core.model.ReaderTheme
import com.kr.reader.core.model.TypographyConfig
import kotlinx.coroutines.flow.Flow

interface TypographyRepository {
    val config: Flow<TypographyConfig>
    suspend fun current(): TypographyConfig
    suspend fun update(mutator: TypographyConfig.() -> TypographyConfig)
}

interface ThemeRepository {
    val theme: Flow<ReaderTheme>
    suspend fun current(): ReaderTheme
    fun presets(): List<ReaderTheme>
    suspend fun update(mutator: ReaderTheme.() -> ReaderTheme)
}

interface ReaderPrefsRepository {
    val prefs: Flow<ReaderPrefs>
    suspend fun current(): ReaderPrefs
    suspend fun update(mutator: ReaderPrefs.() -> ReaderPrefs)
}

interface GlobalPrefsRepository {
    val prefs: Flow<GlobalPrefs>
    suspend fun current(): GlobalPrefs
    suspend fun update(mutator: GlobalPrefs.() -> GlobalPrefs)
}

interface FontRepository {
    suspend fun list(): List<FontEntry>
    suspend fun importFromUri(uri: String, displayName: String): Result<FontEntry>
    suspend fun remove(id: String)
    /** 返回字体文件本地路径；系统字体返回 null */
    suspend fun pathOf(fontFamilyId: String): String?
}
