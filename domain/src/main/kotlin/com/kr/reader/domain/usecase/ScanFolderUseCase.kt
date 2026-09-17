/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: domain/src/main/kotlin/com/kr/reader/domain/usecase/ScanFolderUseCase.kt
 * @Description: 目录扫描用例：按后缀过滤 + 50MB 体积门槛 + 1000 条上限，防止用户误选磁盘根目录
 */
package com.kr.reader.domain.usecase

import com.kr.reader.core.model.BookFormat
import com.kr.reader.core.model.import.ScanItem
import com.kr.reader.domain.repository.FileScannerGateway
import javax.inject.Inject

/** 扫描结果的硬上限：再多用户也不会逐条勾选，只会拖慢 UI */
const val MAX_SCAN_ITEMS = 1000

/** 小于 1KB 的文件基本是空文件或占位文件，直接过滤 */
const val MIN_SCAN_SIZE_BYTES = 1024L

class ScanFolderUseCase @Inject constructor(
    private val scanner: FileScannerGateway,
) {

    suspend operator fun invoke(path: String, recursive: Boolean = true): List<ScanItem> =
        scanner.scan(path, recursive)
            .asSequence()
            .filter { it.sizeBytes >= MIN_SCAN_SIZE_BYTES }
            .filter { it.format != BookFormat.NET }
            .take(MAX_SCAN_ITEMS)
            .toList()
}
