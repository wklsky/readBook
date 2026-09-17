/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/datastore/src/main/kotlin/com/kr/reader/core/datastore/store/ConfigStores.kt
 * @Description: 主题、阅读偏好、全局偏好三类存储，统一提供 Flow + 原子 update 接口
 */
package com.kr.reader.core.datastore.store

import androidx.datastore.core.DataStore
import com.kr.reader.core.model.GlobalPrefs
import com.kr.reader.core.model.ReaderPrefs
import com.kr.reader.core.model.ReaderTheme
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ThemeStore @Inject constructor(private val dataStore: DataStore<ReaderTheme>) {

    val theme: Flow<ReaderTheme> = dataStore.data

    suspend fun update(mutator: ReaderTheme.() -> ReaderTheme) {
        dataStore.updateData { current -> sanitize(mutator(current)) }
    }

    suspend fun setTheme(theme: ReaderTheme) = update { theme }

    suspend fun setDimming(alpha: Float) = update { copy(backgroundDimming = alpha.coerceIn(0f, 0.8f)) }

    suspend fun setInvert(enabled: Boolean) = update { copy(invertEnabled = enabled) }
}

class ReaderPrefsStore @Inject constructor(private val dataStore: DataStore<ReaderPrefs>) {

    val prefs: Flow<ReaderPrefs> = dataStore.data

    suspend fun update(mutator: ReaderPrefs.() -> ReaderPrefs) {
        dataStore.updateData { current -> sanitize(mutator(current)) }
    }

    suspend fun reset() = update { ReaderPrefs.Default }
}

class GlobalPrefsStore @Inject constructor(private val dataStore: DataStore<GlobalPrefs>) {

    val prefs: Flow<GlobalPrefs> = dataStore.data

    suspend fun update(mutator: GlobalPrefs.() -> GlobalPrefs) {
        dataStore.updateData { current -> sanitize(mutator(current)) }
    }
}
