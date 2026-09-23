/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-23 10:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-23 10:00
 * @FilePath: data/sources/src/main/kotlin/com/kr/reader/data/sources/di/SourcesDataModule.kt
 * @Description: 书源数据层 DI 绑定与 Worker 依赖入口点
 */
package com.kr.reader.data.sources.di

import com.kr.reader.data.sources.CacheRepositoryImpl
import com.kr.reader.data.sources.ChapterCacheSchedulerImpl
import com.kr.reader.data.sources.SourceEngineImpl
import com.kr.reader.data.sources.SourceImporter
import com.kr.reader.data.sources.SourceRepositoryImpl
import com.kr.reader.domain.repository.BookRepository
import com.kr.reader.domain.repository.CacheRepository
import com.kr.reader.domain.repository.ChapterCacheScheduler
import com.kr.reader.domain.repository.SourceEngine
import com.kr.reader.domain.repository.SourceImportRepository
import com.kr.reader.domain.repository.SourceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SourcesDataModule {

    @Binds
    @Singleton
    abstract fun bindSourceRepository(impl: SourceRepositoryImpl): SourceRepository

    @Binds
    @Singleton
    abstract fun bindSourceEngine(impl: SourceEngineImpl): SourceEngine

    @Binds
    @Singleton
    abstract fun bindCacheRepository(impl: CacheRepositoryImpl): CacheRepository

    @Binds
    @Singleton
    abstract fun bindSourceImportRepository(impl: SourceImporter): SourceImportRepository

    @Binds
    @Singleton
    abstract fun bindChapterCacheScheduler(impl: ChapterCacheSchedulerImpl): ChapterCacheScheduler
}

/**
 * Worker 的依赖入口点。
 * 之所以用 EntryPoint 而不是 @HiltWorker：@HiltWorker 需要额外的 androidx.hilt 注解处理器，
 * 会为单个 Worker 再拉一条 KSP 链路；本工程只有一个 Worker，EntryPoint 成本更低且同样受 DI 管理。
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface CacheWorkerDeps {

    fun bookRepository(): BookRepository

    fun sourceRepository(): SourceRepository

    fun sourceEngine(): SourceEngine

    fun cacheRepository(): CacheRepository
}
