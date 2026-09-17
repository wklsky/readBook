/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/datastore/src/main/kotlin/com/kr/reader/core/datastore/di/DataStoreModule.kt
 * @Description: DataStore 实例供给：四个配置文件独立 DataStore，写冲突互不影响（阅读器改排版时不影响全局设置）
 */
package com.kr.reader.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.kr.reader.core.datastore.serializer.JsonSerializer
import com.kr.reader.core.model.GlobalPrefs
import com.kr.reader.core.model.ReaderPrefs
import com.kr.reader.core.model.ReaderTheme
import com.kr.reader.core.model.TypographyConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideTypographyDataStore(@ApplicationContext context: Context): DataStore<TypographyConfig> =
        DataStoreFactory.create(
            serializer = JsonSerializer(TypographyConfig.serializer(), TypographyConfig.Default),
            produceFile = { context.dataStoreFile("typography.json") },
        )

    @Provides
    @Singleton
    fun provideThemeDataStore(@ApplicationContext context: Context): DataStore<ReaderTheme> =
        DataStoreFactory.create(
            serializer = JsonSerializer(ReaderTheme.serializer(), ReaderTheme.Paper),
            produceFile = { context.dataStoreFile("theme.json") },
        )

    @Provides
    @Singleton
    fun provideReaderPrefsDataStore(@ApplicationContext context: Context): DataStore<ReaderPrefs> =
        DataStoreFactory.create(
            serializer = JsonSerializer(ReaderPrefs.serializer(), ReaderPrefs.Default),
            produceFile = { context.dataStoreFile("reader_prefs.json") },
        )

    @Provides
    @Singleton
    fun provideGlobalPrefsDataStore(@ApplicationContext context: Context): DataStore<GlobalPrefs> =
        DataStoreFactory.create(
            serializer = JsonSerializer(GlobalPrefs.serializer(), GlobalPrefs.Default),
            produceFile = { context.dataStoreFile("global_prefs.json") },
        )
}
