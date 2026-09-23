/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-23 10:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-23 10:00
 * @FilePath: data/sources/src/main/kotlin/com/kr/reader/data/sources/json/LegacySourceAdapter.kt
 * @Description: 第三方（legado 风格）书源适配：把平铺字段与 rule* 结构转换为本应用的 SourceRules
 */
package com.kr.reader.data.sources.json

import com.kr.reader.core.model.source.BookSource
import com.kr.reader.core.model.source.CatalogRule
import com.kr.reader.core.model.source.ContentRule
import com.kr.reader.core.model.source.DetailRule
import com.kr.reader.core.model.source.SearchRule
import com.kr.reader.core.model.source.SourceRules
import com.kr.reader.core.model.source.UrlRule
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * 只做「尽力转换」：第三方书源生态字段极多，
 * 这里覆盖搜索/详情/目录/正文四段必需字段，其余一律忽略，
 * 未覆盖的站点由用户用规则调试器微调（这也是第 4 卷 4.7.5 要求做调试器的原因）。
 */
internal object LegacySourceAdapter {

    fun convert(obj: JsonObject): BookSource? {
        val name = obj.str("bookSourceName", "sourceName", "name")
        val baseUrl = obj.str("bookSourceUrl", "baseUrl", "url")
        if (name.isBlank() || baseUrl.isBlank()) return null

        // 第三方书源的 charset 常写成 "UTF-8#gbk" 这类带注释的形态，取注释前的真实编码名
        val charset = obj.str("bookSourceCharset", "charset")
            .substringBefore('#')
            .trim()
            .ifBlank { DEFAULT_CHARSET }

        val searchUrl = obj.str("searchUrl", "searchUrlPattern")
        val searchBlock = obj.obj("ruleSearch")
        val detailBlock = obj.obj("ruleBookInfo")
        val tocBlock = obj.obj("ruleToc")
        val contentBlock = obj.obj("ruleContent")

        val parsed = SearchUrlTemplate.parse(searchUrl)
        /*
         * 第三方书源把 Cookie / 防盗链头放在 header 字段里（常见两种写法：JSON 对象或逐行 key:value）。
         * 不解析它，needCookie 的站点一律返回「请开启 JavaScript」或登录页，
         * 用户会误判成书源失效，实际只是请求没带 cookie。
         */
        val mergedHeaders = parseHeaders(obj.str("header", "headers")) + parsed.headers

        return BookSource(
            name = name,
            groupName = obj.str("bookSourceGroup", "group", "groupName").ifBlank { DEFAULT_GROUP },
            baseUrl = baseUrl.trimEnd('/'),
            enabled = obj.bool("enabled", "enable") ?: true,
            sortOrder = obj.int("customOrder", "sortOrder") ?: 0,
            charset = charset,
            customUserAgent = obj.str("userAgent", "ua", "customUserAgent").takeIf { it.isNotBlank() },
            timeoutMs = DEFAULT_TIMEOUT_MS,
            needCookie = obj.bool("needCookie") ?: false,
            rules = SourceRules(
                search = SearchRule(
                    url = parsed.url,
                    method = parsed.method,
                    body = parsed.body,
                    headers = mergedHeaders,
                    list = normalizeSelector(searchBlock.str("bookList", "list")),
                    name = toContentRule(searchBlock.str("name")),
                    author = toContentRule(searchBlock.str("author")),
                    cover = toContentRule(searchBlock.str("coverUrl", "cover")),
                    intro = toContentRule(searchBlock.str("intro", "description")),
                    detailUrl = toContentRule(searchBlock.str("bookUrl", "detailUrl")),
                    latestChapter = toContentRule(searchBlock.str("lastChapter", "latestChapter")),
                    nextPage = normalizeSelector(searchBlock.str("nextPage", "nextUrl")),
                ),
                detail = DetailRule(
                    title = toContentRule(detailBlock.str("name", "title")),
                    author = toContentRule(detailBlock.str("author")),
                    cover = toContentRule(detailBlock.str("coverUrl", "cover")),
                    intro = toContentRule(detailBlock.str("intro", "description")),
                    catalog = detailBlock.str("tocUrl", "catalogUrl")
                        .takeIf { it.isNotBlank() }
                        ?.let { UrlRule(pattern = it, absolute = true) },
                    latestChapter = toContentRule(detailBlock.str("lastChapter", "latestChapter")),
                ),
                catalog = CatalogRule(
                    list = normalizeSelector(tocBlock.str("chapterList", "list")),
                    name = toContentRule(tocBlock.str("chapterName", "name")),
                    url = toContentRule(tocBlock.str("chapterUrl", "url")),
                    reverse = tocBlock.bool("reverse", "isReverse") ?: false,
                    volumePattern = tocBlock.str("volumePattern"),
                    nextPage = normalizeSelector(tocBlock.str("nextTocUrl", "nextPage")),
                ),
                content = toContentRule(
                    contentBlock.str("content"),
                    extraProcessing = buildProcessing(contentBlock.str("replaceRegex")),
                ) ?: ContentRule(rule = "", processing = ""),
                headers = mergedHeaders,
            ),
        )
    }

    /**
     * 规则串 → ContentRule。
     * 第三方规则的替换表达式形如 `selector##pattern##replacement`，
     * 必须拆出来转成 processing 管道，否则替换规则会被当成选择器的一部分。
     */
    private fun toContentRule(raw: String, extraProcessing: String = ""): ContentRule? {
        val text = raw.trim()
        if (text.isEmpty()) return null
        val parts = text.split("##")
        val rule = normalizeSelector(parts.first().trim())
        if (rule.isEmpty()) return null
        val inlineProcessing = buildProcessing(parts.drop(1).joinToString("\n"))
        val processing = listOf(inlineProcessing, extraProcessing)
            .filter { it.isNotBlank() }
            .joinToString("|")
        return ContentRule(rule = rule, processing = processing)
    }

    /** 替换表达式 → processing 管道（regex:pattern=>replacement） */
    private fun buildProcessing(replaceRegex: String): String {
        val lines = replaceRegex.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toList()
        val steps = mutableListOf<String>()
        var i = 0
        while (i < lines.size) {
            val inline = lines[i].split("→", "=>", "###")
            when {
                inline.size >= 2 -> {
                    steps += "regex:${inline[0].trim()}=>${inline[1].trim()}"
                    i++
                }
                i + 1 < lines.size -> {
                    steps += "regex:${lines[i]}=>${lines[i + 1]}"
                    i += 2
                }
                else -> {
                    steps += "regex:${lines[i]}=>"
                    i++
                }
            }
        }
        return steps.joinToString("|")
    }

    /**
     * 选择器语法归一化：把第三方常见的 tag./class./id. 与 @css: 前缀转成标准 Jsoup 选择器。
     * `tag.li.0` 这类「标签+下标」写法等价 `li:eq(0)`。
     */
    private fun normalizeSelector(raw: String): String {
        var text = raw.trim()
        if (text.isEmpty()) return ""
        if (text.startsWith("@css:")) text = text.removePrefix("@css:")
        // 第三方书源习惯写 `h3@text` 显式声明「取文本」，而本引擎默认就取文本，直接剥掉后缀
        if (text.endsWith("@text")) text = text.removeSuffix("@text")
        // 正文容器等场景下第三方常写 "textNodes"/"text" 表示取文本节点，保留原样交给 RuleEngine 解释
        if (text.startsWith("tag.")) {
            val segments = text.removePrefix("tag.").split('.')
            val tag = segments.firstOrNull().orEmpty()
            val index = segments.getOrNull(1)?.toIntOrNull()
            text = if (index != null) "$tag:eq($index)" else tag
        } else if (text.startsWith("class.")) {
            text = "." + text.removePrefix("class.").replace('.', ' ')
        } else if (text.startsWith("id.")) {
            text = "#" + text.removePrefix("id.").split('.').first()
        }
        return text.trim()
    }

    /** header 字段两种写法都支持：JSON 对象，或逐行 `Key: value` */
    private fun parseHeaders(raw: String): Map<String, String> {
        val text = raw.trim()
        if (text.isEmpty()) return emptyMap()
        val fromJson = runCatching {
            Json.parseToJsonElement(text).jsonObject.mapNotNull { (key, value) ->
                (value as? JsonPrimitive)?.contentOrNull?.let { key to it }
            }.toMap()
        }.getOrNull()
        if (!fromJson.isNullOrEmpty()) return fromJson
        return text.lineSequence()
            .mapNotNull { line ->
                val splitAt = line.indexOf(':')
                if (splitAt <= 0) return@mapNotNull null
                line.substring(0, splitAt).trim() to line.substring(splitAt + 1).trim()
            }
            .toMap()
    }

    private fun JsonObject.obj(vararg keys: String): JsonObject =
        keys.firstNotNullOfOrNull { key -> this[key] as? JsonObject } ?: JsonObject(emptyMap())

    private fun JsonObject.str(vararg keys: String): String =
        keys.firstNotNullOfOrNull { key ->
            (this[key] as? JsonPrimitive)?.contentOrNull
        }?.trim().orEmpty()

    private fun JsonObject.bool(vararg keys: String): Boolean? =
        keys.firstNotNullOfOrNull { key -> (this[key] as? JsonPrimitive)?.booleanOrNull }

    private fun JsonObject.int(vararg keys: String): Int? =
        keys.firstNotNullOfOrNull { key -> (this[key] as? JsonPrimitive)?.intOrNull }

    private const val DEFAULT_CHARSET = "UTF-8"
    private const val DEFAULT_GROUP = "默认"
    private const val DEFAULT_TIMEOUT_MS = 15_000
}
