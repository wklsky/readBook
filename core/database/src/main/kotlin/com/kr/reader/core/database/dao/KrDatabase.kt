/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/database/src/main/kotlin/com/kr/reader/core/database/dao/KrDatabase.kt
 * @Description: Room Database 定义：这里实体包含 books/chapters 等 8 张表，exportSchema=true 便于迁移审计
 */
package com.kr.reader.core.database.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kr.reader.core.database.KrTypeConverters
import com.kr.reader.core.database.entity.BookEntity
import com.kr.reader.core.database.entity.BookGroupEntity
import com.kr.reader.core.database.entity.BookSourceEntity
import com.kr.reader.core.database.entity.BookmarkEntity
import com.kr.reader.core.database.entity.ChapterCacheEntity
import com.kr.reader.core.database.entity.ChapterEntity
import com.kr.reader.core.database.entity.ReadingProgressEntity
import com.kr.reader.core.database.entity.ReadingSessionEntity

@Database(
    entities = [
        BookEntity::class,
        ChapterEntity::class,
        ReadingProgressEntity::class,
        BookmarkEntity::class,
        BookGroupEntity::class,
        BookSourceEntity::class,
        ChapterCacheEntity::class,
        ReadingSessionEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(KrTypeConverters::class)
abstract class KrDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun progressDao(): ProgressDao
    abstract fun sourceDao(): SourceDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun groupDao(): GroupDao
    abstract fun cacheDao(): CacheDao
    abstract fun sessionDao(): SessionDao
}
