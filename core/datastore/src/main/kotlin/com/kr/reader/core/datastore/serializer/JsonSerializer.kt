/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/datastore/src/main/kotlin/com/kr/reader/core/datastore/serializer/JsonSerializer.kt
 * @Description: DataStore 通用序列化器。之所以自建而非用 Proto：protobuf 需 protoc/KSP 生成 Tiny Menge 类，
 *               会为 4 个配置文件引入整套工具链；而配置读写的 QPS 极低（秒级），JSON 开销可忽略。
 */
package com.kr.reader.core.datastore.serializer

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream

class JsonSerializer<T>(
    private val kSerializer: KSerializer<T>,
    override val defaultValue: T,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        coerceInputValues = true
    },
) : Serializer<T> {

    override suspend fun readFrom(input: InputStream): T = try {
        json.decodeFromStream(kSerializer, input)
    } catch (e: SerializationException) {
        // 配置损坏不能导致应用崩溃：宁可回退默认值让用户继续读书
        throw CorruptionException("Unable to read proto/store", e)
    } catch (e: IllegalArgumentException) {
        throw CorruptionException("Unable to read store: ${e.message}", e)
    }

    override suspend fun writeTo(t: T, output: OutputStream) {
        json.encodeToStream(kSerializer, t, output)
    }
}
