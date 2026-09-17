/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: data/parser/src/main/kotlin/com/kr/reader/data/parser/txt/ChapterRecognizer.kt
 * @Description: TXT 章节标题识别器（第 4 卷 4.2.2）：行清理 → 正則 → 長さ保護 → 目录行排除
 */
package com.kr.reader.data.parser.txt

/**
 * 章节识别是 TXT 体验的根：识别多了会把正文当目录，识别少了整本书只有一章无法跳转。
 * 因此对「能被识别的行」加了三重约束：行长、必须命中章节式 indicator、排除目录点线行。
 */
object ChapterRecognizer {

    /** 参与匹配的最大行长。超过 30 字的行几乎不可能是章节标题 */
    const val MAX_LINE_LENGTH = 30

    private val NUMERALS = "0-9零一二两三四五六七八九十百千万壹贰叁肆伍陆柒捌玖拾佰仟"

    /** 常规中文序号 + 英文 Chapter + 阿拉伯数字序号 */
    val DEFAULT_PATTERNS: List<Regex> = listOf(
        Regex("^\\s*第\\s*[$NUMERALS]+\\s*[章节節回卷集部篇幕折出]"),
        Regex("^\\s*(序|楔子|引子|前言|引言|序言|自序|后记|後記|尾声|尾聲|番外|附录|附錄|内容简介|內容簡介|大纲|大綱|尾声|结end)"),
        Regex("^\\s*(?i:chapter|part|volume|book)\\s*[0-9ivxlcdm]+"),
        Regex("^\\s*[0-9]{1,5}\\s*[.、:：]\\s*\\S+"),
        Regex("^\\s*[（(\\[]?\\s*[0-9]{1,5}\\s*[)）\\]]?\\s+\\S+"),
    )

    /** 目录页表头：命中后其后的点线行都应忽略，避免把目录页当成几十个假章节 */
    private val TOC_HEADER = Regex("^\\s*(目\\s*录|目錄|contents|table\\s+of\\s+contents)\\s*$", RegexOption.IGNORE_CASE)

    /** 目录行的点线引导符（「第一章 xxx…………5」） */
    private val DOT_LEADER = Regex(".*[.．·•]{3,}.*")

    fun identifyChapter(rawLine: String, patterns: List<Regex> = DEFAULT_PATTERNS, compact: Boolean = true): String? {
        if (rawLine.isBlank()) return null
        // 超长行直接放弃：既保证语义正确，也避免正则在长行上的回溯开销
        if (rawLine.length > MAX_LINE_LENGTH * 4) return null
        if (DOT_LEADER.matches(rawLine)) return null
        val compactLine = if (compact) rawLine.replace(Regex("\\s+"), "") else rawLine.trim()
        if (compactLine.length > MAX_LINE_LENGTH) return null
        val matched = patterns.any { it.containsMatchIn(compactLine) } ||
            patterns.any { it.containsMatchIn(rawLine.trim()) }
        if (!matched) return null
        return rawLine.trim().replace(Regex("[\\s　]+"), " ").take(MAX_LINE_LENGTH * 2)
    }

    fun isTocHeader(rawLine: String): Boolean = TOC_HEADER.matches(rawLine.trim())

    /** 从 JSON 规则串解析用户自定义规则；解析失败时回退默认规则，绝不让非法规则阻塞导入 */
    fun parseChapterRules(ruleJson: String?): List<Regex> {
        if (ruleJson.isNullOrBlank()) return DEFAULT_PATTERNS
        return runCatching {
            org.json.JSONArray(ruleJson).let { array ->
                (0 until array.length()).mapNotNull { i ->
                    runCatching { Regex(array.getString(i)) }.getOrNull()
                }
            }.ifEmpty { DEFAULT_PATTERNS }
        }.getOrDefault(DEFAULT_PATTERNS)
    }
}
