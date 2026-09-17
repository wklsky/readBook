/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: domain/src/main/kotlin/com/kr/reader/domain/repository/FileScannerGateway.kt
 * @Description: 目录扫描端口：SAF DocumentFile 遍历必须走 Android API，领域层只定义契约
 */
package com.kr.reader.domain.repository

import com.kr.reader.core.model.import.ScanItem

interface FileScannerGateway {

    suspend fun scan(path: String, recursive: Boolean): List<ScanItem>
}
