/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: data/parser/src/main/kotlin/com/kr/reader/data/parser/epub/EpubParser.kt
 * @Description: EPUB 解析器：container.xml → OPF → manifest/spine → 抽封面 → HTML 净化成纯文本
 */
package com.kr.reader.data.parser.epub

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.kr.reader.core.common.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import com.kr.reader.core.model.BookFormat
import com.kr.reader.core.model.parse.BookMetadata
import com.kr.reader.core.model.parse.BookParser
import com.kr.reader.core.model.parse.ChapterMeta
import com.kr.reader.core.model.parse.FileInput
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.parser.Parser

/** EPUB 清单项：一个 spine item 对应阅读器的一章 */
private data class EpubItem(
    val id: String,
    val href: String,
    val mediaType: String,
    /** 抽文本后的字符数，用于推算全书字符偏移 */
    var charCount: Int = 0,
)

private data class EpubStructure(
    val opfPath: String,
    val opfDir: String,
    val items: LinkedHashMap<String, EpubItem>,
    val spine: List<String>,
    val hasTooManyChapters: Boolean = false,
)

@Singleton
class EpubParser @Inject constructor(
    @ApplicationContext private val context: Context,
) : BookParser {

    override val supportedFormat: BookFormat = BookFormat.EPUB

    /** key = tmp 文件路径。同一本书的 metadata/chapter/readChapter 三次调用共享一次 OPF 解析 */
    private val structureCache = ConcurrentHashMap<String, EpubStructure>()

    override suspend fun getMetadata(input: FileInput): BookMetadata = withContext(IoDispatcher) {
        val structure = structureOf(input)
        ZipFile(input.tempFilePath).use { zip ->
            val doc = zip.parseXml(structure.opfPath)
            val title = doc?.selectFirst("metadata > dc|title")?.text().orEmpty()
            val author = doc?.selectFirst("metadata > dc|creator")?.text()?.takeIf { it.isNotBlank() }
            val description = doc?.selectFirst("metadata > dc|description")?.text()?.takeIf { it.isNotBlank() }
            val coverId = doc?.selectFirst("metadata > meta[name=cover]")?.attr("content")
            val coverHref = coverId?.let { structure.items[it]?.href }
                ?: structure.items.values.firstOrNull { it.mediaType.startsWith("image/") }?.href
            val coverPath = coverHref?.let { saveCover(zip, structure.opfDir, it, input.displayName) }
            BookMetadata(
                title = title.ifBlank { input.displayName.substringBeforeLast('.') },
                author = author,
                coverPath = coverPath,
                intro = description?.take(500),
                chapterRuleId = null,
            )
        }
    }

    override suspend fun extractChapterList(input: FileInput): List<ChapterMeta> = withContext(IoDispatcher) {
        val structure = structureOf(input)
        ZipFile(input.tempFilePath).use { zip ->
            // 逐篇读取才能知道每章字符数；这里一次性读完是因为 EPUB 章节通常只有几千字，
            // 且后续 readChapter 会复用本次计算的偏移
            var offset = 0L
            val metas = ArrayList<ChapterMeta>(structure.spine.size)
            structure.spine.forEachIndexed { index, id ->
                val item = structure.items[id] ?: return@forEachIndexed
                val html = zip.readHtml(structure.opfDir, item.href).orEmpty()
                val title = htmlTitle(html, index)
                val text = htmlToText(html)
                item.charCount = text.length
                metas.add(
                    ChapterMeta(
                        index = metas.size,
                        title = title,
                        startOffset = offset,
                        endOffset = offset + item.charCount,
                        charCount = item.charCount,
                    ),
                )
                offset += item.charCount.toLong()
            }
            metas.ifEmpty {
                listOf(ChapterMeta(0, "全文", 0, 0, 0))
            }
        }
    }

    override suspend fun readChapter(input: FileInput, chapter: ChapterMeta): String = withContext(IoDispatcher) {
        val structure = structureOf(input)
        val item = structure.spine.getOrNull(chapter.index)?.let { structure.items[it] }
            ?: structure.items.values.elementAtOrNull(chapter.index)
            ?: return@withContext ""
        ZipFile(input.tempFilePath).use { zip ->
            htmlToText(zip.readHtml(structure.opfDir, item.href).orEmpty())
        }
    }

    override fun close(input: FileInput) {
        structureCache.remove(input.tempFilePath)
    }

    private suspend fun structureOf(input: FileInput): EpubStructure = withContext(IoDispatcher) {
        structureCache[input.tempFilePath]?.let { return@withContext it }
        ZipFile(input.tempFilePath).use { zip ->
            val container = zip.parseXml("META-INF/container.xml")
            val opfPath = container?.selectFirst("rootfile")?.attr("full-path").orEmpty().ifBlank { "content.opf" }
            val opfDir = opfPath.substringBeforeLast('/', "")
            val opf = zip.parseXml(opfPath) ?: zip.parseXml("content.opf")
            val items = LinkedHashMap<String, EpubItem>()
            opf?.select("manifest > item")?.forEach { element ->
                val id = element.attr("id")
                if (id.isNotBlank()) {
                    items[id] = EpubItem(
                        id = id,
                        href = element.attr("href").trim(),
                        mediaType = element.attr("media-type").lowercase(Locale.US),
                    )
                }
            }
            // linear=no 的 item 是扉页/版权页，不应进目录
            val spine = opf?.select("spine > itemref")
                ?.mapNotNull { it.attr("idref").takeIf { id -> id.isNotBlank() } }
                ?.filter { items.containsKey(it) }
                ?.filter { id -> items[id]?.mediaType?.contains("html") == true || items[id]?.mediaType?.contains("xhtml") == true }
                .orEmpty()
            val structure = EpubStructure(
                opfPath = opfPath,
                opfDir = opfDir,
                items = items,
                spine = spine.ifEmpty { items.keys.toList() },
                hasTooManyChapters = spine.size > MAX_CHAPTERS,
            )
            structureCache[input.tempFilePath] = structure
            structure
        }
    }

    private fun htmlTitle(html: String, fallbackIndex: Int): String {
        val title = Jsoup.parse(html).selectFirst("title")?.text().orEmpty().ifBlank {
            Jsoup.parse(html).selectFirst("h1, h2, h3")?.text().orEmpty()
        }
        return title.ifBlank { "第 ${fallbackIndex + 1} 章" }
    }

    /**
     * HTML → 纯文本：段落/换行保留，其余标签丢掉。
     * 之所以自己转换而不是直接用 html.css?text: Android 的 Html.fromHtml 对 EPUB 常见标签处理不足，
     * 且会产生大量 Span 对象拖慢首屏。
     */
    fun htmlToText(html: String): String {
        val body = Jsoup.parse(html).body()
        body.select("script, style, nav").remove()
        val builder = StringBuilder()
        body.children().forEach { child ->
            val text = child.text().replace(Regex("[\\s　]+"), " ").trim()
            if (text.isNotBlank()) {
                if (builder.isNotEmpty()) builder.append('\n')
                builder.append(text)
            }
        }
        val result = builder.toString().ifBlank { body.text().replace(Regex("[\\s　]+"), " ").trim() }
        return result
    }

    private fun ZipFile.readHtml(opfDir: String, href: String): String? {
        val path = if (opfDir.isBlank()) href else "$opfDir/$href"
        return runCatching {
            getInputStream(getEntry(path) ?: return@runCatching null)
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
        }.getOrNull()
    }

    private fun ZipFile.parseXml(path: String) = runCatching {
        val entry: ZipEntry = getEntry(path) ?: return@runCatching null
        getInputStream(entry).use { Jsoup.parse(it, null, "", Parser.xmlParser()) }
    }.getOrNull()

    /** 保存封面到 filesDir/covers 并返回绝对路径 */
    private suspend fun saveCover(zip: ZipFile, opfDir: String, href: String, name: String): String? = withContext(IoDispatcher) {
        runCatching {
            val path = if (opfDir.isBlank()) href else "$opfDir/$href"
            val entry = zip.getEntry(path) ?: return@runCatching null
            val bitmap = BitmapFactory.decodeStream(zip.getInputStream(entry)) ?: return@runCatching null
            val dir = File(context.filesDir, "covers")
            if (!dir.exists()) dir.mkdirs()
            val out = File(dir, "${name.hashCode()}.jpg")
            FileOutputStream(out).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos)
            }
            out.absolutePath
        }.getOrNull()
    }

    companion object {
        private const val MAX_CHAPTERS = 5000
    }
}
