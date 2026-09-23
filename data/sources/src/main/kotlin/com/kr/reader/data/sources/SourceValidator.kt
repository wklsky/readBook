/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-23 10:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-23 10:00
 * @FilePath: data/sources/src/main/kotlin/com/kr/reader/data/sources/SourceValidator.kt
 * @Description: 书源三步校验（第 4 卷 4.7.4）：字段完整性 → URL 合法性 → 选择器语法预校验。
 *               必须在导入时拦住坏书源，否则「能导入不能用」比直接拒绝更伤害用户
 */
package com.kr.reader.data.sources

import com.kr.reader.core.model.source.BookSource
import com.kr.reader.core.model.source.ContentRule
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton
import org.jsoup.Jsoup
import org.jsoup.select.Selector

data class SourceValidation(
    val ok: Boolean,
    /** 失败原因，可直接展示给用户 */
    val reason: String = "",
    /** 可疑但不致命的问题（如缺少作者规则），导入后仍可用，用于管理页黄点提示 */
    val warnings: List<String> = emptyList(),
)

@Singleton
class SourceValidator @Inject constructor() {

    fun validate(source: BookSource): SourceValidation {
        val warnings = mutableListOf<String>()

        if (source.name.isBlank()) return SourceValidation(false, "书源名称为空")
        if (source.baseUrl.isBlank()) return SourceValidation(false, "书源地址为空")
        if (!isHttpUrl(source.baseUrl)) return SourceValidation(false, "书源地址不是合法的 http(s) 链接")

        if (source.rules.search.url.isBlank() && source.rules.search.list.isBlank()) {
            return SourceValidation(false, "缺少搜索规则，无法搜索书籍")
        }
        if (source.rules.content.rule.isBlank()) {
            return SourceValidation(false, "缺少正文规则，无法读取章节内容")
        }
        if (source.rules.catalog.list.isBlank() && source.rules.catalog.url == null) {
            return SourceValidation(false, "缺少目录规则，无法获取章节列表")
        }

        // 选择器预校验：语法错误的选择器在运行时只会返回空结果，
        // 用户完全无法判断是「站点改版」还是「自己写错了」，必须在导入期就报出来
        selectorError(source.rules.search.list)?.let {
            return SourceValidation(false, "搜索列表规则语法错误：${readableSelectorError(it)}")
        }
        selectorError(source.rules.content.rule)?.let {
            return SourceValidation(false, "正文规则语法错误：${readableSelectorError(it)}")
        }
        selectorError(source.rules.catalog.list)?.let {
            return SourceValidation(false, "目录列表规则语法错误：${readableSelectorError(it)}")
        }

        if (source.rules.search.detailUrl == null && source.rules.search.list.isNotBlank()) {
            warnings += "未配置详情页链接规则，加入书架时可能拿不到目录"
        }
        if (source.rules.search.name == null) warnings += "未配置书名规则，搜索结果可能显示为空标题"
        if (source.charset.isBlank()) warnings += "未声明编码，将按 UTF-8 解码"

        return SourceValidation(true, warnings = warnings)
    }

    /** 规则调试器复用：单条规则的语法诊断 */
    fun checkSelector(rule: String): String? = selectorError(rule)?.let(::readableSelectorError)

    private fun selectorError(rule: String): Throwable? {
        val text = rule.trim()
        if (text.isEmpty()) return null
        // 非选择器语法（正则 / JsonPath / 文本节点指令）不走 Jsoup 校验
        if (text.startsWith("regex:") || text.startsWith("$") || NON_SELECTOR_TOKENS.contains(text)) return null
        return runCatching {
            // 空文档上跑一次选择：语法错误会以 SelectorParseException 抛出，语义不匹配则静默返回空
            Selector.select(text, Jsoup.parse("<html><body></body></html>").body())
            null
        }.getOrElse { it }
    }

    private fun readableSelectorError(error: Throwable): String {
        val message = error.message.orEmpty()
        return if (message.isBlank()) "无法解析的选择器" else message.take(MAX_REASON_LENGTH)
    }

    private fun isHttpUrl(value: String): Boolean = runCatching {
        val uri = URI(value)
        val scheme = uri.scheme
        (scheme == "http" || scheme == "https") && uri.host.isNullOrBlank().not()
    }.getOrDefault(false)

    /** 校验 ContentRule 是否可用（供调试器逐字段显示） */
    fun isRuleUsable(rule: ContentRule?): Boolean = rule != null && rule.rule.isNotBlank()

    private companion object {
        val NON_SELECTOR_TOKENS = setOf("text", "textNodes", "ownText", "html", "@href", "@src", "@text")
        const val MAX_REASON_LENGTH = 120
    }
}
