/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: data/pagination/src/main/kotlin/com/kr/reader/data/pagination/di/PaginationModule.kt
 * @Description: 分页模块 DI 绑定
 */
package com.kr.reader.data.pagination.di

import com.kr.reader.data.pagination.PaginationEngine
import com.kr.reader.domain.repository.ChapterPaginator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PaginationModule {

    @Binds
    @Singleton
    abstract fun bindChapterPaginator(impl: PaginationEngine): ChapterPaginator
}
