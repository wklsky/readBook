/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-23 10:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-23 10:00
 * @FilePath: core/model/src/main/kotlin/com/kr/reader/core/model/source/SourceImportModels.kt
 * @Description: 书源导入报表模型：批量导入必须逐条给出成败与原因，否则用户无法定位是哪一条书源有问题
 */
package com.kr.reader.core.model.source

/** 单条书源的导入结果 */
data class SourceImportItem(
    /** 书源名；JSON 解析失败时用来源标识（URL 或「第 N 条」）兜底 */
    val name: String,
    val ok: Boolean,
    /** 失败原因，必须是可直接展示的话术 */
    val reason: String = "",
)

data class SourceImportReport(
    val items: List<SourceImportItem>,
    /** 整个输入连 JSON 都算不上时的提示；此时 items 为空 */
    val invalidJson: String? = null,
) {
    val successCount: Int get() = items.count { it.ok }
    val failCount: Int get() = items.count { !it.ok }
}
