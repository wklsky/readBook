/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/model/src/main/kotlin/com/kr/reader/core/model/import/ImportModels.kt
 * @Description: 书籍导入相关模型。Uri 以 String 传递以保证 model 层无 Android 依赖
 */
package com.kr.reader.core.model.import

import com.kr.reader.core.model.BookFormat
import com.kr.reader.core.model.ImportMode

/** 目录扫描出的候选文件 */
data class ScanItem(
    val path: String,
    val displayName: String,
    val sizeBytes: Long,
    val format: BookFormat,
    /** true 表示本地同名书已存在，UI 需给出去重提示 */
    val existsInShelf: Boolean = false,
)

/** 用户勾选后的导入请求 */
data class ImportRequest(
    val uri: String,
    val displayName: String,
    val sizeBytes: Long,
    val format: BookFormat,
    val mimeType: String? = null,
    /** 未显式指定时由体积与环境推荐 */
    val mode: ImportMode? = null,
)

/** 推荐导入模式时的环境输入 */
data class StorageContext(
    val freeSpaceMb: Long,
    val sizeBytes: Long,
)

/** 单文件导入失败原因 */
sealed interface ImportFailureReason {
    val message: String

    data class FileTooLarge(override val message: String = "文件超过 100MB，暂不支持") : ImportFailureReason
    data class UnsupportedFormat(override val message: String = "不支持的文件类型") : ImportFailureReason
    data class PermissionDenied(override val message: String = "URI 临时授权失效") : ImportFailureReason
    data class CopyFailed(override val message: String = "文件复制失败") : ImportFailureReason
    data class DedupHit(override val message: String = "重复文件") : ImportFailureReason
    data class Unknown(override val message: String = "导入失败") : ImportFailureReason
}

data class ImportFailure(
    val name: String,
    val reason: ImportFailureReason,
)

/** 导入/预缓存进度 */
data class ImportProgress(
    val total: Int,
    val completed: Int,
    val failed: List<ImportFailure>,
    val currentName: String = "",
)
