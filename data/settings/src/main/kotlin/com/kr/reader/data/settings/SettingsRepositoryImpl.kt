/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: data/settings/src/main/kotlin/com/kr/reader/data/settings/SettingsRepositoryImpl.kt
 * @Description: 配置仓储实现：DataStore Flow 直出，首次读取因未缓存需要 fallback 到默认值
 */
package com.kr.reader.data.settings

import com.kr.reader.core.datastore.store.GlobalPrefsStore
import com.kr.reader.core.datastore.store.ReaderPrefsStore
import com.kr.reader.core.datastore.store.ThemeStore
import com.kr.reader.core.datastore.store.TypographyStore
import com.kr.reader.core.model.GlobalPrefs
import com.kr.reader.core.model.ReaderPrefs
import com.kr.reader.core.model.ReaderTheme
import com.kr.reader.core.model.TypographyConfig
import com.kr.reader.domain.repository.GlobalPrefsRepository
import com.kr.reader.domain.repository.ReaderPrefsRepository
import com.kr.reader.domain.repository.ThemeRepository
import com.kr.reader.domain.repository.TypographyRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first

@Singleton
class TypographyRepositoryImpl @Inject constructor(
    private val store: TypographyStore,
) : TypographyRepository {

    override val config: Flow<TypographyConfig> = store.data
        // DataStore 首次 IO 失败（如磁盘满）时回退默认值，绝不让阅读器进不去
        .catch { emit(TypographyConfig.Default) }

    override suspend fun current(): TypographyConfig =
        runCatching { config.first() }.getOrDefault(TypographyConfig.Default)

    override suspend fun update(mutator: TypographyConfig.() -> TypographyConfig) = store.update(mutator)
}

@Singleton
class ThemeRepositoryImpl @Inject constructor(
    private val store: ThemeStore,
) : ThemeRepository {

    override val theme: Flow<ReaderTheme> = store.theme.catch { emit(ReaderTheme.Paper) }

    override suspend fun current(): ReaderTheme =
        runCatching { theme.first() }.getOrDefault(ReaderTheme.Paper)

    override fun presets(): List<ReaderTheme> = ReaderTheme.Presets

    override suspend fun update(mutator: ReaderTheme.() -> ReaderTheme) = store.update(mutator)
}

@Singleton
class ReaderPrefsRepositoryImpl @Inject constructor(
    private val store: ReaderPrefsStore,
) : ReaderPrefsRepository {

    override val prefs: Flow<ReaderPrefs> = store.prefs.catch { emit(ReaderPrefs.Default) }

    override suspend fun current(): ReaderPrefs = runCatching { prefs.first() }.getOrDefault(ReaderPrefs.Default)

    override suspend fun update(mutator: ReaderPrefs.() -> ReaderPrefs) = store.update(mutator)
}

@Singleton
class GlobalPrefsRepositoryImpl @Inject constructor(
    private val store: GlobalPrefsStore,
) : GlobalPrefsRepository {

    override val prefs: Flow<GlobalPrefs> = store.prefs.catch { emit(GlobalPrefs.Default) }

    override suspend fun current(): GlobalPrefs = runCatching { prefs.first() }.getOrDefault(GlobalPrefs.Default)

    override suspend fun update(mutator: GlobalPrefs.() -> GlobalPrefs) = store.update(mutator)
}
