/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-23 10:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-23 10:00
 * @FilePath: data/sources/src/main/kotlin/com/kr/reader/data/sources/SourceImporter.kt
 * @Description: 书源导入（第 4 卷 4.7.4）：JSON 文本 / 批量 URL / 本地文件三种入口，逐条校验并给出失败原因
 */
package com.kr.reader.data.sources

import android.content.Context
import android.net.Uri
import com.kr.reader.core.common.IoDispatcher
import com.kr.reader.core.model.source.BookSource
import com.kr.reader.core.model.source.SourceImportItem
import com.kr.reader.core.model.source.SourceImportReport
import com.kr.reader.data.sources.json.SourceJsonCodec
import com.kr.reader.domain.repository.SourceImportRepository
import com.kr.reader.domain.repository.SourceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

@Singleton
class SourceImporter @Inject constructor(
    private val repository: SourceRepository,
    private val validator: SourceValidator,
    private val codec: SourceJsonCodec,
    private val http: SourceHttpClient,
    @ApplicationContext private val context: Context,
) : SourceImportRepository {

    override suspend fun importFromText(text: String): SourceImportReport {
        val sources = runCatching { codec.decodeMany(text) }.getOrElse {
            // 连 JSON 都解析不出来时，items 为空 + invalidJson 有值，UI 才能区分「文件坏」与「书源不合法」
            return SourceImportReport(emptyList(), invalidJson = "JSON 格式错误：${it.message.orEmpty()}")
        }
        if (sources.isEmpty()) {
            return SourceImportReport(emptyList(), invalidJson = "没有解析到任何书源")
        }
        return SourceImportReport(sources.map { importOne(it) })
    }

    override suspend fun importFromUrls(urls: List<String>): SourceImportReport = coroutineScope {
        val items = urls
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .chunked(URL_CONCURRENCY)
            .flatMap { chunk ->
                chunk.map { url ->
                    async(IoDispatcher) {
                        runCatching {
                            val json = http.fetchPlain(url)
                            val report = importFromText(json)
                            when {
                                report.invalidJson != null -> SourceImportItem(url, false, report.invalidJson!!)
                                report.items.isEmpty() -> SourceImportItem(url, false, "该链接未返回任何书源")
                                report.failCount > 0 -> SourceImportItem(
                                    url,
                                    report.successCount > 0,
                                    report.items.firstOrNull { !it.ok }?.reason.orEmpty(),
                                )
                                else -> SourceImportItem(url, true)
                            }
                        }.getOrElse { SourceImportItem(url, false, "下载失败：${it.message.orEmpty()}") }
                    }
                }.awaitAll()
            }
        SourceImportReport(items)
    }

    override suspend fun importFromUri(uriString: String): SourceImportReport {
        val text = runCatching { readUriText(uriString) }.getOrNull()
            ?: return SourceImportReport(emptyList(), invalidJson = "无法读取所选文件")
        return importFromText(text)
    }

    private suspend fun importOne(source: BookSource): SourceImportItem {
        val validation = validator.validate(source)
        if (!validation.ok) return SourceImportItem(source.name, false, validation.reason)
        // 同名拒绝而不是覆盖：书源是用户手工调过的资产，静默覆盖等于丢失配置
        if (repository.existsByName(source.name)) {
            return SourceImportItem(source.name, false, "已存在同名书源")
        }
        repository.insert(source)
        return SourceImportItem(source.name, true)
    }

    /** 逐行读取而非 readText：书源文件可能有几 MB，一次性读入既慢又受 detekt 的大文件规则约束 */
    private suspend fun readUriText(uriString: String): String = withContext(IoDispatcher) {
        val uri = Uri.parse(uriString)
        context.contentResolver.openInputStream(uri)?.use { stream ->
            val reader = BufferedReader(InputStreamReader(stream, Charsets.UTF_8))
            buildString {
                var line = reader.readLine()
                while (line != null) {
                    append(line).append('\n')
                    line = reader.readLine()
                }
            }
        }.orEmpty()
    }

    private companion object {
        const val URL_CONCURRENCY = 3
    }
}
