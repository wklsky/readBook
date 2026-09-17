/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/database/src/main/kotlin/com/kr/reader/core/database/KrTypeConverters.kt
 * @Description: Room 类型转换器：仅枚举到字符串，其余字段均保持 SQLite 原生类型
 */
package com.kr.reader.core.database

import androidx.room.TypeConverter
import com.kr.reader.core.model.SourceHealth

class KrTypeConverters {

    @TypeConverter
    fun healthToString(health: SourceHealth?): String = health?.name ?: SourceHealth.UNKNOWN.name

    @TypeConverter
    fun stringToHealth(value: String?): SourceHealth = runCatching {
        SourceHealth.valueOf(value.orEmpty())
    }.getOrDefault(SourceHealth.UNKNOWN)
}
