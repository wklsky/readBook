/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-23 10:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-23 10:00
 * @FilePath: data/sources/src/main/kotlin/com/kr/reader/data/sources/SourceEngineImpl.kt
 * @Description: 书源引擎实现：搜索/详情/目录/正文四段抓取链路，含健康度打点与换源进度迁移
 */
package com.kr.reader.data.sources

import android.util.Log
import com.kr.reader.core.common.IoDispatcher
import com.kr.reader.core.model.Book
import com.kr.reader.core.model.Chapter
import com.kr.reader.core.model.SourceRuleBroken
import com.kr.reader.core.model.source.BookSource
import com.kr.reader.core.model.source.CatalogItem
import com.kr.reader.core.model.source.ContentRule
import com.kr.reader.core.model.source.MergedBook
import com.kr.reader.core.model.source.SourceBookDiff
import com.kr.reader.core.model.source.SourceBookRef
import com.kr.reader.core.model.source.SourceSearchItem
import com.kr.reader.data.sources.json.SearchUrlTemplate
import com.kr.reader.domain.repository.BookRepository
import com.kr.reader.domain.repository.ProgressRepository
import com.kr.reader.domain.repository.SourceEngine
import com.kr.reader.domain.repository.SourceRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

@Singleton
class SourceEngineImpl @Inject constructor(
    private val http: SourceHttpClient,
    private val ruleEngine: RuleEngine,
    private val sourceRepository: SourceRepository,
    private val bookRepository: BookRepository,
    private val progressRepository: ProgressRepository,
    private val matcher: ChapterMatcher,
) : SourceEngine {

    override suspend fun search(source: BookSource, keyword: String): List<SourceSearchItem> = withHealth(source) {
        val rule = source.rules.search
        val parsed = SearchUrlTemplate.parse(rule.url)
        val method = if (parsed.method != "GET") parsed.method else rule.method.ifBlank { "GET" }
        // 三级 headers 都要带上：站点级（常见的 Cookie/防盗链）→ 搜索规则级 → URL 模板内联级。
        // 漏掉站点级会让配了 needCookie 的源每次都被判定为「书源失效」
        val headers = source.rules.headers + rule.headers + parsed.headers
        val charset = parsed.charset.ifBlank { source.charset }

        val results = mutableListOf<SourceSearchItem>()
        var page = 1
        val startUrl = SearchUrlTemplate.render(parsed.url, keyword, page, charset)
        if (startUrl.isBlank()) throw SourceRuleBroken("搜索地址为空，请检查书源的搜索规则")
        var url: String? = startUrl
        while (url != null && page <= MAX_SEARCH_PAGES) {
            val html = http.fetch(
                source = source,
                url = url,
                method = method,
                body = SearchUrlTemplate.renderBody(parsed.body.ifBlank { rule.body }, keyword, page, charset)
                    .takeIf { it.isNotBlank() },
                headers = headers,
            )
            val doc = Jsoup.parse(html, url)
            val parsedPage = parseSearchPage(source, doc)
            if (parsedPage.isEmpty()) break
            results += parsedPage

            page++
            val next = rule.nextPage.takeIf { it.isNotBlank() }?.let {
                ruleEngine.extract(ContentRule(rule = it), doc.body(), url, wantUrl = true)
            }?.takeIf { it.isNotBlank() && it != url }
            url = next?.takeIf { page <= MAX_SEARCH_PAGES }
        }
        results
    }

    override suspend fun fetchDetail(source: BookSource, detailUrl: String): MergedBook = withHealth(source) {
        // 空地址交给 OkHttp 只会抛出难以理解的 IllegalArgumentException，
        // 提前拦成书源规则错误，用户在调试器里能直接看到原因
        if (detailUrl.isBlank()) throw SourceRuleBroken("详情页地址为空")
        val html = http.fetch(source, detailUrl)
        val doc = Jsoup.parse(html, detailUrl)
        val rule = source.rules.detail

        val title = ruleEngine.extract(rule.title, doc.body(), detailUrl)
            .ifBlank { doc.title().trim() }
        val catalogUrl = resolveCatalogUrl(source, detailUrl, doc)
        val latestChapter = ruleEngine.extract(rule.latestChapter, doc.body(), detailUrl).takeIf { it.isNotBlank() }

        MergedBook(
            // bookId 为 0 表示「尚未加入书架」，详情页加入书架时才会落库并回填
            bookId = 0,
            title = title.ifBlank { doc.title().trim() },
            author = ruleEngine.extract(rule.author, doc.body(), detailUrl),
            coverUrl = ruleEngine.extract(rule.cover, doc.body(), detailUrl, wantUrl = true).takeIf { it.isNotBlank() },
            intro = ruleEngine.extract(rule.intro, doc.body(), detailUrl).takeIf { it.isNotBlank() },
            sources = listOf(
                SourceBookRef(
                    sourceId = source.id,
                    sourceName = source.name,
                    detailUrl = catalogUrl,
                    latestChapter = latestChapter,
                    reliability = source.healthScore,
                    totalChapters = 0,
                ),
            ),
            selectedIndex = 0,
            latestChapterTitle = latestChapter,
        )
    }

    override suspend fun fetchCatalog(source: BookSource, bookKey: String): List<CatalogItem> = withHealth(source) {
        if (bookKey.isBlank()) throw SourceRuleBroken("目录页地址为空")
        val rule = source.rules.catalog
        val items = mutableListOf<CatalogItem>()
        var page = 1
        var url: String? = bookKey
        while (url != null && page <= MAX_CATALOG_PAGES) {
            val html = http.fetch(source, url)
            val doc = Jsoup.parse(html, url)
            val pageItems = parseCatalogPage(source, doc, url)
            if (pageItems.isEmpty()) break
            items += pageItems

            page++
            val next = rule.nextPage.takeIf { it.isNotBlank() }?.let {
                ruleEngine.extract(ContentRule(rule = it), doc.body(), url, wantUrl = true)
            }?.takeIf { it.isNotBlank() && it != url }
            url = next?.takeIf { page <= MAX_CATALOG_PAGES }
        }
        if (rule.reverse) items.reversed() else items
    }

    override suspend fun fetchContent(source: BookSource, chapterUrl: String): String = withHealth(source) {
        if (chapterUrl.isBlank()) throw SourceRuleBroken("章节地址为空")
        val html = http.fetch(source, chapterUrl)
        val doc = Jsoup.parse(html, chapterUrl)
        val text = ruleEngine.parseContent(source.rules.content, doc)
        // 正文过短几乎一定是「规则匹配到了导航栏/推荐位」而非真的就这么短，
        // 明确报错能让用户在调试器里直接看到原因，而不是面对一个空白章节无从下手
        if (text.length < MIN_CONTENT_CHARS) {
            throw SourceRuleBroken("正文长度异常（${text.length} 字），content 规则可能匹配到了非正文容器")
        }
        text
    }

    override suspend fun compareSources(bookId: Long): List<SourceBookDiff> = withContext(IoDispatcher) {
        val book = bookRepository.getBook(bookId) ?: return@withContext emptyList()
        val localChapters = bookRepository.getChapters(bookId)
        if (localChapters.isEmpty()) return@withContext emptyList()
        // 卷行不是章节，计入差异会凭空多出一堆「目标源缺失」
        val localTitles = localChapters.filter { !it.isVolume }.map { it.title }.toSet()

        sourceRepository.enabledSourcesSortedByHealth()
            .filter { it.id != book.sourceId }
            .mapNotNull { source ->
                // 单个源失败不影响整体审计：这里的目的是「给用户一张对比表」，不是强一致流程
                val diff = runCatching { diffAgainst(source, book, localTitles) }
                    .onFailure { Log.w(TAG, "书源对比失败：${source.name} ${it.message}") }
                    .getOrNull()
                diff
            }
            .sortedByDescending { it.reliabilityScore }
    }

    override suspend fun switchSource(bookId: Long, targetSourceId: Long): Book {
        val book = bookRepository.getBook(bookId) ?: throw IllegalStateException("书籍不存在")
        val target = sourceRepository.get(targetSourceId)
            ?: throw SourceRuleBroken("目标书源不存在或已被删除")

        val found = findInSource(target, book.title)
            ?: throw SourceRuleBroken("目标书源未收录《${book.title}》")
        val catalog = fetchCatalog(target, resolveCatalogKey(target, found))
        if (catalog.isEmpty()) throw SourceRuleBroken("目标书源返回了空目录")

        val oldChapters = bookRepository.getChapters(bookId)
        val indexMap = matcher.buildIndexMap(oldChapters, catalog)
        val newChapters = catalog.mapIndexed { index, item ->
            Chapter(
                bookId = bookId,
                index = index,
                title = item.title,
                // 网络书没有稳定的全局字符流，startOffset 一律记 0，定位完全依赖章节下标 + 章内偏移
                startOffset = 0L,
                endOffset = 0L,
                sourceUrl = item.url,
                isVolume = item.isVolume,
            )
        }

        /*
         * 进度迁移的核心：章内字符偏移原样保留，只把章节下标换成新目录里的对应章。
         * 这样换源后用户仍然停在「同一章的同一个字」，而不是被丢回开头。
         */
        val progress = progressRepository.get(bookId)
        if (progress != null && indexMap.isNotEmpty()) {
            val newIndex = indexMap.getOrElse(progress.chapterIndex.coerceIn(0, indexMap.lastIndex)) { 0 }
            val newTitle = newChapters.getOrNull(newIndex)?.title ?: progress.chapterTitle
            progressRepository.update(progress.copy(chapterIndex = newIndex, chapterTitle = newTitle))
            progressRepository.flushNow(bookId)
        }

        val latest = catalog.lastOrNull { !it.isVolume }?.title
        bookRepository.rebindSource(
            bookId = bookId,
            sourceId = targetSourceId,
            sourceBookKey = found.detailUrl,
            newChapters = newChapters,
            latestChapterTitle = latest,
            intro = book.intro,
        )
        return bookRepository.getBook(bookId) ?: book
    }

    // ── 解析 ──────────────────────────────────────────────────────

    private fun parseSearchPage(source: BookSource, doc: Document): List<SourceSearchItem> {
        val rule = source.rules.search
        val baseUrl = doc.baseUri().ifBlank { source.baseUrl }
        return ruleEngine.selectList(rule.list, doc.body()).mapNotNull { element ->
            val name = ruleEngine.extract(rule.name, element, baseUrl)
            val detailUrl = ruleEngine.extract(rule.detailUrl, element, baseUrl, wantUrl = true)
            // 书名与详情页链接缺一不可：没有链接的书进不了书架，直接丢弃比显示出来更诚实
            if (name.isBlank() || detailUrl.isBlank()) return@mapNotNull null
            SourceSearchItem(
                name = name,
                author = ruleEngine.extract(rule.author, element, baseUrl),
                coverUrl = ruleEngine.extract(rule.cover, element, baseUrl, wantUrl = true).takeIf { it.isNotBlank() },
                intro = ruleEngine.extract(rule.intro, element, baseUrl).takeIf { it.isNotBlank() },
                detailUrl = detailUrl,
                latestChapter = ruleEngine.extract(rule.latestChapter, element, baseUrl).takeIf { it.isNotBlank() },
                sourceId = source.id,
                sourceName = source.name,
            )
        }
    }

    private fun parseCatalogPage(source: BookSource, doc: Document, baseUri: String): List<CatalogItem> {
        val rule = source.rules.catalog
        return ruleEngine.selectList(rule.list, doc.body()).mapNotNull { element ->
            val title = ruleEngine.extract(rule.name, element, baseUri)
                .ifBlank { element.text().trim() }
            val url = ruleEngine.extract(rule.url, element, baseUri, wantUrl = true)
            if (title.isBlank()) return@mapNotNull null
            val isVolume = rule.volumePattern.isNotBlank() &&
                runCatching { Regex(rule.volumePattern).containsMatchIn(title) }.getOrDefault(false)
            CatalogItem(title = title, url = url, isVolume = isVolume)
        }
    }

    /**
     * 目录页地址裁决：详情页规则里显式给了 tocUrl 就用它，
     * 否则目录就长在详情页本身（大量小站没有独立目录页）。
     */
    private fun resolveCatalogUrl(source: BookSource, detailUrl: String, doc: Document): String {
        val pattern = source.rules.detail.catalog?.pattern?.trim().orEmpty()
        if (pattern.isEmpty()) return detailUrl
        if (pattern.startsWith("http://") || pattern.startsWith("https://")) return pattern
        if (pattern.startsWith("{{")) {
            // 第三方书源常用 {{bookUrl}} 占位表示「用详情页地址当目录页」
            return pattern.replace("{{bookUrl}}", detailUrl).replace("{{url}}", detailUrl)
        }
        val selected = runCatching { doc.selectFirst(pattern)?.attr("abs:href") }.getOrNull()
        return selected?.takeIf { it.isNotBlank() }
            ?: ruleEngine.resolveUrl(source.baseUrl.ifBlank { detailUrl }, pattern)
    }

    /**
     * 目录页地址解析：站点普遍把目录单独放在 toc 页（详情页只放简介），
     * 直接用详情页当目录会抓到空目录——换源与多源对比都必须先走这一步。
     */
    private suspend fun resolveCatalogKey(source: BookSource, found: SourceSearchItem): String {
        val pattern = source.rules.detail.catalog?.pattern?.trim()
        if (pattern.isNullOrBlank()) return found.detailUrl
        return runCatching { fetchDetail(source, found.detailUrl).sources.firstOrNull()?.detailUrl }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: found.detailUrl
    }

    /** 在指定源里按书名找最匹配的一本，用于换源与多源对比 */
    private suspend fun findInSource(source: BookSource, title: String): SourceSearchItem? {
        val results = runCatching { search(source, title) }.getOrDefault(emptyList())
        if (results.isEmpty()) return null
        val target = matcher.normalize(title)
        return results.firstOrNull { matcher.normalize(it.name) == target }
            ?: results.firstOrNull { matcher.normalize(it.name).contains(target) || target.contains(matcher.normalize(it.name)) }
    }

    private suspend fun diffAgainst(
        source: BookSource,
        book: Book,
        localTitles: Set<String>,
    ): SourceBookDiff? {
        val found = findInSource(source, book.title) ?: return null
        val catalog = fetchCatalog(source, resolveCatalogKey(source, found))
        if (catalog.isEmpty()) return null
        val remoteTitles = catalog.map { it.title }.toSet()

        val extra = remoteTitles.filter { it !in localTitles }
        val missing = localTitles.filter { it !in remoteTitles }
        return SourceBookDiff(
            sourceId = source.id,
            sourceName = source.name,
            extraChapterCount = extra.size,
            extraChapters = extra.take(PREVIEW_CHAPTER_LIMIT),
            missingChapterCount = missing.size,
            missingChapters = missing.take(PREVIEW_CHAPTER_LIMIT),
            latestChapterTitle = found.latestChapter ?: catalog.lastOrNull { !it.isVolume }?.title,
            latestUpdateMs = System.currentTimeMillis(),
            reliabilityScore = source.healthScore,
        )
    }

    /**
     * 健康度打点：成功归零失败计数、失败累加。
     * 必须包裹每一个抓取入口，否则 BROKEN 源永远不会被自动摘除，聚合搜索会一遍遍等它超时。
     */
    private suspend fun <T> withHealth(source: BookSource, block: suspend () -> T): T {
        val startedAt = System.currentTimeMillis()
        return try {
            block().also {
                sourceRepository.recordSuccess(source.id, (System.currentTimeMillis() - startedAt).toInt())
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (t: Throwable) {
            sourceRepository.recordFailure(source.id)
            throw t
        }
    }

    private companion object {
        const val TAG = "SourceEngine"
        const val MAX_SEARCH_PAGES = 3
        const val MAX_CATALOG_PAGES = 10
        const val MIN_CONTENT_CHARS = 100
        const val PREVIEW_CHAPTER_LIMIT = 5
    }
}
