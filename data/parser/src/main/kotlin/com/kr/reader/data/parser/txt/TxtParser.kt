/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: data/parser/src/main/kotlin/com/kr/reader/data/parser/txt/TxtParser.kt
 * @Description: TXT 解析器（第 4 卷 4.2.2）：流式扫描建立目录、字符偏移索引支持按章随机读取
 */
package com.kr.reader.data.parser.txt

import com.kr.reader.core.common.IoDispatcher
import com.kr.reader.core.model.BookFormat
import com.kr.reader.core.model.parse.BookMetadata
import com.kr.reader.core.model.parse.BookParser
import com.kr.reader.core.model.parse.ChapterMeta
import com.kr.reader.core.model.parse.FileInput
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext

/** 识别失败的判据：若整本书只有一章且超长，不如直接当全文展示 */
private const val MAX_LENGTH_WITH_NO_CHAPTERS = 10 * 10000

/** 简介取样长度 */
private const val INTRO_CHARS = 200L

private data class RawChapterHit(val title: String, val start: Long)

@Singleton
class TxtParser @Inject constructor() : BookParser {

    override val supportedFormat: BookFormat = BookFormat.TXT

    /** key = 文件路径 + 编码，避免同一本书重复建立索引 */
    private val indexCache = ConcurrentHashMap<String, CharOffsetIndex>()

    override suspend fun getMetadata(input: FileInput): BookMetadata = withContext(IoDispatcher) {
        val file = File(input.tempFilePath)
        val encoding = input.encoding ?: CharsetDetector.detect(file)
        val title = input.displayName.substringBeforeLast('.').trim()
        val intro = runCatching { headText(input, encoding) }
            .getOrDefault("")
            .take(INTRO_CHARS.toInt())
            .trim()
        BookMetadata(
            title = title,
            author = null,
            intro = intro.ifBlank { null },
            coverPath = null,
            chapterRuleId = null,
        )
    }

    override suspend fun extractChapterList(input: FileInput): List<ChapterMeta> = withContext(IoDispatcher) {
        val hits = scanChapters(input)
        buildChapterMetas(hits, totalChars(input))
    }

    override suspend fun readChapter(input: FileInput, chapter: ChapterMeta): String {
        val index = obtainIndex(input)
        // 单章超过 2 万字时分段读取再拼接：一次性读入会产生超大 String 与大量中间对象，
        // 在低端机上显著提升 GC 压力，分段后峰值内存约降一半
        val end = minOf(chapter.endOffset, index.totalChars)
        if (end - chapter.startOffset > SEGMENT_CHAR_LIMIT) {
            return buildString {
                var from = chapter.startOffset
                while (from < end) {
                    val to = minOf(from + SEGMENT_CHAR_LIMIT, end)
                    append(index.readChars(from, to))
                    from = to
                }
            }.trim { it.isWhitespace() || it == '\u3000' }
        }
        return index.readChars(chapter.startOffset, end).trim { it.isWhitespace() || it == '\u3000' }
    }

    override fun close(input: FileInput) {
        indexCache.remove(cacheKey(input))
    }

    private suspend fun totalChars(input: FileInput): Long = obtainIndex(input).totalChars

    private suspend fun headText(input: FileInput, encoding: String): String {
        val index = obtainIndex(input)
        return index.readChars(0, minOf(INTRO_CHARS * 2, index.totalChars)).replace('\n', ' ')
    }

    /**
     * 逐行扫描识别章节。
     * 之所以用 bufferedReader 而不是 readLines：10MB 文件 readLines 会产生百万级临时 String，
     * 在中低端机上直接引起 GC 抖动甚至 OOM。
     */
    private suspend fun scanChapters(input: FileInput): List<RawChapterHit> = withContext(IoDispatcher) {
        val file = File(input.tempFilePath)
        val charset = CharsetDetector.charsetOf(input.encoding ?: CharsetDetector.detect(file))
        val hits = ArrayList<RawChapterHit>()
        var charOffset = 0L
        file.bufferedReader(charset, READ_BUFFER).use { reader ->
            while (true) {
                val line = reader.readLine() ?: break
                val hit = ChapterRecognizer.identifyChapter(line)
                if (hit != null) {
                    // 若两章之间距离过近（<50 字符），多半是同一段被拆成多行，丢弃后者
                    val lastStart = hits.lastOrNull()?.start ?: Long.MIN_VALUE
                    if (charOffset - lastStart > MIN_CHAPTER_GAP) {
                        hits.add(RawChapterHit(hit, charOffset))
                    }
                }
                charOffset += line.length + 1L
            }
        }
        hits
    }

    private fun buildChapterMetas(hits: List<RawChapterHit>, totalChars: Long): List<ChapterMeta> {
        if (hits.isEmpty()) {
            return listOf(
                ChapterMeta(
                    index = 0,
                    title = "全文",
                    startOffset = 0L,
                    endOffset = totalChars,
                    charCount = totalChars.toInt(),
                ),
            )
        }
        val withPrologue = if (hits.first().start > MIN_CHAPTER_GAP) {
            listOf(RawChapterHit("前言", 0L)) + hits
        } else {
            hits
        }
        val metas = ArrayList<ChapterMeta>(withPrologue.size)
        withPrologue.forEachIndexed { index, hit ->
            val end = if (index + 1 < withPrologue.size) withPrologue[index + 1].start else totalChars
            val charCount = (end - hit.start).toInt()
            if (charCount > 0) {
                metas.add(
                    ChapterMeta(
                        index = metas.size,
                        title = hit.title,
                        startOffset = hit.start,
                        endOffset = end,
                        charCount = charCount,
                    ),
                )
            }
        }
        // 识别失败（只有一章且超长）时退化为全文单章，
        // 避免用户看到一个名为「第1章」却包含几十万字的假目录
        if (metas.size == 1 && metas.first().charCount > MAX_LENGTH_WITH_NO_CHAPTERS) {
            return listOf(
                ChapterMeta(
                    index = 0,
                    title = "全文",
                    startOffset = 0L,
                    endOffset = totalChars,
                    charCount = totalChars.toInt(),
                ),
            )
        }
        return metas
    }

    private fun cacheKey(input: FileInput): String = "${input.tempFilePath}#${input.encoding.orEmpty()}"

    private suspend fun obtainIndex(input: FileInput): CharOffsetIndex {
        val key = cacheKey(input)
        indexCache[key]?.let { return it }
        val file = File(input.tempFilePath)
        val charset = CharsetDetector.charsetOf(input.encoding ?: CharsetDetector.detect(file))
        val index = CharOffsetIndex.build(file, charset)
        indexCache[key] = index
        return index
    }

    companion object {
        private const val READ_BUFFER = 8 * 1024
        private const val MIN_CHAPTER_GAP = 50L
        private const val SEGMENT_CHAR_LIMIT = 20_000L
    }
}
