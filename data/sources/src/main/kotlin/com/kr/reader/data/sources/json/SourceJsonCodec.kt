/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-23 10:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-23 10:00
 * @FilePath: data/sources/src/main/kotlin/com/kr/reader/data/sources/json/SourceJsonCodec.kt
 * @Description: 书源 JSON 编解码：支持单对象 / 数组 / {"sources":[...]} 三种包装，
 *               并兼容第三方（legado 风格）平铺字段书源的导入
 */
package com.kr.reader.data.sources.json

import com.kr.reader.core.model.source.BookSource
import javax.inject.Inject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

class SourceJsonCodec @Inject constructor() {

    /**
     * ignoreUnknownKeys 是刚需：第三方书源 JSON 带有大量本应用不识别的字段
     * （如 bookSourceComment、exploreUrl、loginUrl），严格解析会全量失败。
     */
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
        encodeDefaults = false
        prettyPrint = true
    }

    /** 解析任意包装形式的书源文本；失败时抛出 IllegalArgumentException，由导入层转成用户话术 */
    fun decodeMany(text: String): List<BookSource> {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) throw IllegalArgumentException("内容为空")
        val root = runCatching { json.parseToJsonElement(trimmed) }
            .getOrElse { throw IllegalArgumentException("JSON 格式错误：${it.message.orEmpty()}") }

        return when (root) {
            is JsonArray -> root.mapNotNull(::decodeElement)
            is JsonObject -> {
                val wrapped = root[WRAPPED_KEY] as? JsonArray
                if (wrapped != null) wrapped.mapNotNull(::decodeElement) else listOfNotNull(decodeElement(root))
            }
            else -> throw IllegalArgumentException("顶层必须是对象或数组")
        }
    }

    fun decodeElement(element: JsonElement): BookSource? =
        (element as? JsonObject)?.let(::decodeObject)

    fun decodeObject(obj: JsonObject): BookSource {
        // 原生格式以 rules 为标志；缺 rules 的一律按第三方格式适配，避免误判为「损坏文件」
        val native = runCatching { json.decodeFromJsonElement<BookSource>(obj) }.getOrNull()
        if (native != null && obj.containsKey("rules")) return native
        return LegacySourceAdapter.convert(obj)
            ?: native
            ?: throw IllegalArgumentException("缺少 name / baseUrl 字段，无法识别为书源")
    }

    fun encode(source: BookSource): String = json.encodeToString(source)

    /** 导出：统一用 {"sources": [...]} 包装，与导入端对称，方便用户分享 */
    fun encodeMany(sources: List<BookSource>): String {
        val array = sources.map { json.encodeToJsonElement(it) }
        return json.encodeToString(JsonObject(mapOf(WRAPPED_KEY to JsonArray(array))))
    }

    private companion object {
        const val WRAPPED_KEY = "sources"
    }
}
