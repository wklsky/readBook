/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-23 10:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-23 10:00
 * @FilePath: data/sources/src/main/kotlin/com/kr/reader/data/sources/RuleEngine.kt
 * @Description: 书源规则解释引擎（第 4 卷 4.7.2）：支持 CSS 选择器 / JsonPath / 正则三类提取语法与后处理管道
 */
package com.kr.reader.data.sources

import com.kr.reader.core.model.source.ContentRule
import com.kr.reader.domain.usecase.PurifyContentUseCase
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

@Singleton
class RuleEngine @Inject constructor(
    private val purify: PurifyContentUseCase,
) {

    /** 列表容器：规则为空时把整页当一个条目，很多搜索结果页的容器就是 <ul> 本身 */
    fun selectList(rule: String, root: Element): List<Element> {
        val spec = rule.trim()
        if (spec.isEmpty()) return listOf(root)
        val elements = root.select(spec)
        if (elements.isEmpty()) return emptyList()
        return elements.toList()
    }

    /**
     * 单字段提取。
     * @param wantUrl true 表示这一字段是链接，结果会被解析为绝对 URL（站点普遍写相对路径）
     */
    fun extract(
        rule: ContentRule?,
        root: Element,
        baseUri: String = "",
        wantUrl: Boolean = false,
    ): String {
        val spec = rule?.rule?.trim().orEmpty()
        if (spec.isEmpty()) return ""
        val raw = extractRaw(spec, root)
        val resolved = if (wantUrl) resolveUrl(baseUri, raw) else raw
        return applyProcessing(resolved, rule?.processing.orEmpty())
    }

    /** 正文抽取：容器 → 去广告节点 → 按段落取文本 → 净化 */
    fun parseContent(rule: ContentRule, doc: Document): String {
        val spec = rule.rule.trim()
        val container: Element = if (spec.isEmpty()) {
            doc.body()
        } else {
            runCatching { doc.selectFirst(spec) }.getOrNull()
                ?: throw IllegalStateException("正文规则未匹配到任何元素（或选择器语法非法）")
        }

        // 广告节点必须在取文本之前移除，否则它们会以「正文」形式混进章节
        container.select(AD_SELECTOR).remove()

        val text = buildString {
            val paragraphs = container.select("p")
            if (!paragraphs.isEmpty()) {
                paragraphs.forEach { p ->
                    val line = p.text().trim()
                    if (line.isNotEmpty()) append(line).append('\n')
                }
            } else {
                // 没有 <p> 的站点用 <br> 分行：先把 br 换成换行，再让 Jsoup 帮我们去标签
                val withBreaks = container.html().replace(BR_REGEX, "\n")
                val plain = Jsoup.parse(withBreaks).text()
                plain.lineSequence().forEach { line ->
                    val trimmed = line.trim()
                    if (trimmed.isNotEmpty()) append(trimmed).append('\n')
                }
            }
        }.trim()

        val purified = purify.purify(text)
        return applyProcessing(purified, rule.processing)
    }

    /** 相对链接 → 绝对链接；已经是绝对的则原样返回 */
    fun resolveUrl(baseUri: String, href: String): String {
        val value = href.trim()
        if (value.isEmpty()) return ""
        if (value.startsWith("http://", ignoreCase = true) || value.startsWith("https://", ignoreCase = true)) {
            return value
        }
        if (baseUri.isBlank()) return value
        return runCatching { URI(baseUri).resolve(value).toString() }.getOrDefault(value)
    }

    // ── 三类提取语法 ──────────────────────────────────────────────

    private fun extractRaw(spec: String, root: Element): String = when {
        spec.startsWith("regex:") -> applyRegex(spec.removePrefix("regex:"), root.text())
        spec.startsWith("$") -> extractJson(spec, root.text())
        spec.startsWith("@") -> attrOf(spec, root)
        spec == TOKEN_TEXT_NODES -> root.textNodes().joinToString("\n") { it.text().trim() }
        spec == TOKEN_OWN_TEXT -> root.ownText()
        spec == TOKEN_HTML -> root.html()
        spec == TOKEN_TEXT -> root.text()
        else -> {
            // 选择器可能是用户手写的非法语法：这里吞掉异常返回空，
            // 否则一次聚合搜索里某个源写错规则会把整条搜索链路打断
            val element = runCatching { root.selectFirst(spec) }.getOrNull() ?: return ""
            val value = element.attr("abs:href").takeIf { it.isNotBlank() }
                ?: element.text().trim()
            value
        }
    }

    /** 语法 `pattern$1`：取第 1 捕获组；不带 $ 时取整段匹配 */
    private fun applyRegex(spec: String, input: String): String {
        if (input.isEmpty()) return ""
        val splitAt = spec.lastIndexOf('$')
        val pattern = if (splitAt > 0) spec.substring(0, splitAt) else spec
        val group = if (splitAt > 0) spec.substring(splitAt + 1).trim().toIntOrNull() ?: 1 else 0
        if (!isSafePattern(pattern)) return ""
        val match = runCatching { Regex(pattern).find(input) }.getOrNull() ?: return ""
        return match.groupValues.getOrElse(group) { "" }
    }

    /** JsonPath 子集：`$.data.list[0].title` 与第三方书源的 `$1`（数组下标，从 1 开始） */
    private fun extractJson(spec: String, input: String): String {
        val text = input.trim()
        if (text.isEmpty()) return ""
        val root = runCatching { Json.parseToJsonElement(text) }.getOrNull() ?: return ""
        val path = spec.removePrefix("$")
        if (path.isEmpty()) return primitiveOf(root)

        // `$1` / `$1-5` 这类写法是「取第 N 个元素」，与 $. 路径语法完全不同
        val indexOnly = path.toIntOrNull()
        if (indexOnly != null) {
            val array = root as? JsonArray ?: return ""
            return primitiveOf(array.getOrNull(indexOnly - 1) ?: return "")
        }

        var current: JsonElement = root
        for (token in tokenizeJsonPath(path)) {
            current = if (token.startsWith("[")) {
                val index = token.trim('[', ']').toIntOrNull() ?: return ""
                (current as? JsonArray)?.getOrNull(index) ?: return ""
            } else {
                (current as? JsonObject)?.get(token) ?: return ""
            }
        }
        return primitiveOf(current)
    }

    private fun tokenizeJsonPath(path: String): List<String> {
        val tokens = mutableListOf<String>()
        var buffer = StringBuilder()
        var inBracket = false
        path.forEach { ch ->
            when {
                ch == '[' -> {
                    if (buffer.isNotEmpty()) { tokens += buffer.toString(); buffer = StringBuilder() }
                    inBracket = true
                    buffer.append(ch)
                }
                ch == ']' -> { buffer.append(ch); tokens += buffer.toString(); buffer = StringBuilder(); inBracket = false }
                ch == '.' && !inBracket -> {
                    if (buffer.isNotEmpty()) { tokens += buffer.toString(); buffer = StringBuilder() }
                }
                else -> buffer.append(ch)
            }
        }
        if (buffer.isNotEmpty()) tokens += buffer.toString()
        return tokens
    }

    private fun primitiveOf(element: JsonElement): String = when (element) {
        is JsonPrimitive -> element.content
        is JsonArray -> element.firstOrNull()?.let(::primitiveOf).orEmpty()
        else -> element.toString()
    }

    private fun attrOf(spec: String, root: Element): String {
        val name = when {
            spec == "@href" -> "href"
            spec == "@src" -> "src"
            spec.startsWith("@attr:") -> spec.removePrefix("@attr:")
            else -> spec.removePrefix("@")
        }
        val own = root.attr(name)
        if (own.isNotBlank()) return own
        return root.selectFirst("[$name]")?.attr(name).orEmpty()
    }

    // ── 后处理管道 ────────────────────────────────────────────────

    fun applyProcessing(value: String, processing: String): String {
        if (processing.isBlank()) return value
        var result = value
        processing.split('|').forEach { rawStep ->
            val step = rawStep.trim()
            if (step.isEmpty()) return@forEach
            result = when {
                step == STEP_TRIM -> result.trim()
                step == STEP_UNESCAPE -> unescapeEntities(result)
                step.startsWith("regex:") -> {
                    val (pattern, replacement) = splitArrow(step.removePrefix("regex:"))
                    safeReplace(result, pattern, replacement)
                }
                step.startsWith("replace:") -> {
                    val (from, to) = splitArrow(step.removePrefix("replace:"))
                    result.replace(from, to)
                }
                step.startsWith("prepend:") -> step.removePrefix("prepend:") + result
                step.startsWith("append:") -> result + step.removePrefix("append:")
                step.startsWith("default:") -> result.ifBlank { step.removePrefix("default:") }
                step.startsWith("substring:") -> substringOf(result, step.removePrefix("substring:"))
                else -> result
            }
        }
        return result
    }

    /**
     * ReDoS 防护：用户从网上抄来的正则质量不可控，
     * 嵌套量词（如 (a+)+）与超长模式在正文（动辄上万字）上极易把主线程打满。
     */
    private fun isSafePattern(pattern: String): Boolean {
        if (pattern.length > MAX_PATTERN_LENGTH) return false
        var depth = 0
        var quantifierInGroup = false
        pattern.forEach { ch ->
            when (ch) {
                '(' -> depth++
                ')' -> { if (quantifierInGroup) return false; depth = 0; quantifierInGroup = false }
                '+', '*' -> if (depth > 0) quantifierInGroup = true
            }
        }
        return !quantifierInGroup
    }

    private fun safeReplace(input: String, pattern: String, replacement: String): String {
        if (input.isEmpty() || pattern.isEmpty()) return input
        if (input.length > MAX_REPLACE_INPUT || !isSafePattern(pattern)) return input
        return runCatching { Regex(pattern).replace(input, replacement) }.getOrDefault(input)
    }

    private fun splitArrow(raw: String): Pair<String, String> {
        val parts = raw.split("=>", limit = 2)
        return parts[0] to parts.getOrElse(1) { "" }
    }

    private fun substringOf(value: String, spec: String): String {
        val parts = spec.split(',')
        val from = parts[0].trim().toIntOrNull() ?: return value
        val to = parts.getOrNull(1)?.trim()?.toIntOrNull()
        if (from < 0 || from > value.length) return value
        val end = (to ?: value.length).coerceAtMost(value.length)
        if (end <= from) return ""
        return value.substring(from, end)
    }

    /** 手写实体解码：避免依赖 jsoup 内部 API 的版本差异 */
    private fun unescapeEntities(value: String): String {
        if ('&' !in value) return value
        return ENTITY_PATTERN.replace(value) { match ->
            val body = match.groupValues[1]
            when {
                body.startsWith("#x", ignoreCase = true) -> body.drop(2).toIntOrNull(16)?.toChar()?.toString().orEmpty()
                body.startsWith("#") -> body.drop(1).toIntOrNull()?.toChar()?.toString().orEmpty()
                else -> NAMED_ENTITIES[body] ?: match.value
            }
        }
    }

    private companion object {
        const val TOKEN_TEXT = "text"
        const val TOKEN_TEXT_NODES = "textNodes"
        const val TOKEN_OWN_TEXT = "ownText"
        const val TOKEN_HTML = "html"
        const val STEP_TRIM = "trim"
        const val STEP_UNESCAPE = "unescape"
        const val MAX_PATTERN_LENGTH = 200
        const val MAX_REPLACE_INPUT = 200_000
        val AD_SELECTOR = "script, style, iframe, noscript, ins, .ads, [class*=ad-], [id*=ad-]"
        val BR_REGEX = Regex("""<br\s*/?>""", RegexOption.IGNORE_CASE)
        val ENTITY_PATTERN = Regex("""&(#?\w+);""")
        val NAMED_ENTITIES = mapOf(
            "amp" to "&", "lt" to "<", "gt" to ">", "quot" to "\"", "apos" to "'",
            "nbsp" to " ", "#39" to "'", "#34" to "\"",
        )
    }
}
