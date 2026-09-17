/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: data/books/src/main/kotlin/com/kr/reader/data/books/di/BooksDataModule.kt
 * @Description: 书籍数据层 DI 绑定与 Room 数据库供给
 */
package com.kr.reader.data.books.di

import android.content.Context
import androidx.room.Room
import com.kr.reader.core.database.dao.BookDao
import com.kr.reader.core.database.dao.BookmarkDao
import com.kr.reader.core.database.dao.CacheDao
import com.kr.reader.core.database.dao.GroupDao
import com.kr.reader.core.database.dao.KrDatabase
import com.kr.reader.core.database.dao.ProgressDao
import com.kr.reader.core.database.dao.SourceDao
import com.kr.reader.data.books.BookRepositoryImpl
import com.kr.reader.data.books.BookScanner
import com.kr.reader.data.books.BookmarkRepositoryImpl
import com.kr.reader.data.books.FileArchive
import com.kr.reader.data.books.ProgressRepositoryImpl
import com.kr.reader.domain.repository.BookRepository
import com.kr.reader.domain.repository.BookmarkRepository
import com.kr.reader.domain.repository.FileScannerGateway
import com.kr.reader.domain.repository.ImportFileGateway
import com.kr.reader.domain.repository.ProgressRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KrDatabase = Room.databaseBuilder(
        context,
        KrDatabase::class.java,
        DATABASE_NAME,
    )
        // 主线程写进度会被严惩：这里刻意保持严格模式，任何越线 IO 都会在调试期暴露
        .fallbackToDestructiveMigrationOnDowngrade()
        .build()

    @Provides
    fun provideBookDao(db: KrDatabase): BookDao = db.bookDao()

    @Provides
    fun provideProgressDao(db: KrDatabase): ProgressDao = db.progressDao()

    @Provides
    fun provideSourceDao(db: KrDatabase): SourceDao = db.sourceDao()

    @Provides
    fun provideBookmarkDao(db: KrDatabase): BookmarkDao = db.bookmarkDao()

    @Provides
    fun provideGroupDao(db: KrDatabase): GroupDao = db.groupDao()

    @Provides
    fun provideCacheDao(db: KrDatabase): CacheDao = db.cacheDao()

    private const val DATABASE_NAME = "kotlin_reader.db"
}

@Module
@InstallIn(SingletonComponent::class)
abstract class BooksDataModule {

    @Binds
    @Singleton
    abstract fun bindBookRepository(impl: BookRepositoryImpl): BookRepository

    @Binds
    @Singleton
    abstract fun bindProgressRepository(impl: ProgressRepositoryImpl): ProgressRepository

    @Binds
    @Singleton
    abstract fun bindBookmarkRepository(impl: BookmarkRepositoryImpl): BookmarkRepository

    @Binds
    @Singleton
    abstract fun bindImportFileGateway(impl: FileArchive): ImportFileGateway

    @Binds
    @Singleton
    abstract fun bindFileScannerGateway(impl: BookScanner): FileScannerGateway
}
