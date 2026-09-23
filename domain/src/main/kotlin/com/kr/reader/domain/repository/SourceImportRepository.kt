/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-23 10:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-23 10:00
 * @FilePath: domain/src/main/kotlin/com/kr/reader/domain/repository/SourceImportRepository.kt
 * @Description: 书源导入端口（第 4 卷 4.7.4）。之所以走接口而非让 UI 直接依赖 data：
 *               导入既要读 SAF Uri 又要发网络请求，domain 只暴露纯 Kotlin 契约（Uri 以字符串传递）
 */
package com.kr.reader.domain.repository

import com.kr.reader.core.model.source.SourceImportReport

interface SourceImportRepository {

    /** 单条/多条 JSON 文本导入，支持对象、数组与 {"sources": [...]} 三种包装 */
    suspend fun importFromText(text: String): SourceImportReport

    /** 批量 URL 导入：并发 3 拉取，逐条校验 */
    suspend fun importFromUrls(urls: List<String>): SourceImportReport

    /** 从 SAF 选择的本地文件导入 */
    suspend fun importFromUri(uriString: String): SourceImportReport
}
