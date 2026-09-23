/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-23 10:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-23 10:00
 * @FilePath: data/sources/src/main/kotlin/com/kr/reader/data/sources/json/SearchUrlTemplate.kt
 * @Description: 搜索 URL 模板解析与渲染：处理第三方书源的 `url,{"method":...}` 写法与关键字编码声明
 */
package com.kr.reader.data.sources.json

import java.net.URLEncoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class ParsedSearchUrl(
    val url: String,
    val method: String = "GET",
    val body: String = "",
    val headers: Map<String, String> = emptyMap(),
    /** 关键字编码：第三方站点大量仍用 GBK，写错编码搜出来全是乱码 */
    val charset: String = "UTF-8",
)

object SearchUrlTemplate {

    private val lenient = Json { ignoreUnknownKeys = true; isLenient = true }

    /** 编码声明形态：`|char=gbk|`、`|charset=gbk|` */
    private val INLINE_CHARSET = Regex("""\|?\s*char(?:set)?\s*=\s*([\w-]+)\s*\|?""", RegexOption.IGNORE_CASE)

    fun parse(raw: String): ParsedSearchUrl {
        val text = raw.trim()
        if (text.isEmpty()) return ParsedSearchUrl(url = "")

        // 第三方书源把请求配置挂在 URL 之后：形如 `https://a/b,{"method":"POST","body":"kw={{key}}"}`
        val splitAt = findConfigSplit(text)
        val urlPart = if (splitAt >= 0) text.substring(0, splitAt) else text
        val configPart = if (splitAt >= 0) text.substring(splitAt + 1) else ""

        val config = runCatching { lenient.parseToJsonElement(configPart).jsonObject }.getOrNull()

        val declared = config?.get("charset")?.jsonPrimitive?.contentOrNull
            ?: INLINE_CHARSET.find(urlPart)?.groupValues?.get(1)
            ?: "UTF-8"

        // 声明段本身不是 URL 的一部分，必须剥掉，否则会被拼进请求里
        val cleanUrl = INLINE_CHARSET.replace(urlPart, "").trim().trimEnd(',', '&', '?')

        return ParsedSearchUrl(
            url = cleanUrl,
            method = config?.get("method")?.jsonPrimitive?.contentOrNull?.uppercase() ?: "GET",
            body = config?.get("body")?.jsonPrimitive?.contentOrNull.orEmpty(),
            headers = parseHeaders(config),
            charset = declared,
        )
    }

    /** 关键字/页码占位符渲染：{{key}} {{keyword}} {{page}} 以及第三方的 {{searchKey}} / <page> */
    fun render(url: String, keyword: String, page: Int, charset: String): String {
        val encoded = encode(keyword, charset)
        return url
            .replace("{{key}}", encoded)
            .replace("{{keyword}}", encoded)
            .replace("{{searchKey}}", encoded)
            .replace("{{query}}", encoded)
            .replace("{{page}}", page.toString())
            .replace("{{p}}", page.toString())
            .replace("<page>", page.toString())
    }

    fun renderBody(body: String, keyword: String, page: Int, charset: String): String {
        if (body.isBlank()) return ""
        return body
            .replace("{{key}}", encode(keyword, charset))
            .replace("{{keyword}}", encode(keyword, charset))
            .replace("{{page}}", page.toString())
    }

    private fun encode(value: String, charset: String): String = runCatching {
        URLEncoder.encode(value, charset)
    }.getOrElse { URLEncoder.encode(value, "UTF-8") }

    private fun findConfigSplit(text: String): Int {
        val idx = text.indexOf(",{")
        if (idx < 0) return -1
        return if (text.trimEnd().endsWith("}")) idx else -1
    }

    private fun parseHeaders(config: JsonObject?): Map<String, String> {
        val node = config?.get("headers") as? JsonObject ?: return emptyMap()
        return node.mapNotNull { (key, value) ->
            val content = (value as? JsonPrimitive)?.contentOrNull ?: return@mapNotNull null
            key to content
        }.toMap()
    }
}
