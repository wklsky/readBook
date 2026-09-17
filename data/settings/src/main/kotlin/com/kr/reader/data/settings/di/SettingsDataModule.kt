/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: data/settings/src/main/kotlin/com/kr/reader/data/settings/di/SettingsDataModule.kt
 * @Description: 设置数据层 DI 绑定
 */
package com.kr.reader.data.settings.di

import com.kr.reader.data.settings.FontRepositoryImpl
import com.kr.reader.data.settings.GlobalPrefsRepositoryImpl
import com.kr.reader.data.settings.ReaderPrefsRepositoryImpl
import com.kr.reader.data.settings.ThemeRepositoryImpl
import com.kr.reader.data.settings.TypographyRepositoryImpl
import com.kr.reader.domain.repository.FontRepository
import com.kr.reader.domain.repository.GlobalPrefsRepository
import com.kr.reader.domain.repository.ReaderPrefsRepository
import com.kr.reader.domain.repository.ThemeRepository
import com.kr.reader.domain.repository.TypographyRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsDataModule {

    @Binds
    @Singleton
    abstract fun bindTypographyRepository(impl: TypographyRepositoryImpl): TypographyRepository

    @Binds
    @Singleton
    abstract fun bindThemeRepository(impl: ThemeRepositoryImpl): ThemeRepository

    @Binds
    @Singleton
    abstract fun bindReaderPrefsRepository(impl: ReaderPrefsRepositoryImpl): ReaderPrefsRepository

    @Binds
    @Singleton
    abstract fun bindGlobalPrefsRepository(impl: GlobalPrefsRepositoryImpl): GlobalPrefsRepository

    @Binds
    @Singleton
    abstract fun bindFontRepository(impl: FontRepositoryImpl): FontRepository
}
