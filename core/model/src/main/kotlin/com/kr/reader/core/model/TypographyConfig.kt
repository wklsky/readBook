/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/model/src/main/kotlin/com/kr/reader/core/model/TypographyConfig.kt
 * @Description: 排版参数配置（第 3 卷 3.3.2 / 第 4 卷 4.5.1），范围约束由 Repository 写入侧统一夹取
 */
package com.kr.reader.core.model

import kotlinx.serialization.Serializable

/** 排版参数合法区间集中定义，UI 滑块与写入夹取共用同一份真值 */
object TypographyRange {
    const val MIN_FONT_SIZE_SP = 12f
    const val MAX_FONT_SIZE_SP = 36f
    const val MIN_LINE_HEIGHT = 1.0f
    const val MAX_LINE_HEIGHT = 3.0f
    const val MIN_PARAGRAPH_SPACING = 0f
    const val MAX_PARAGRAPH_SPACING = 2f
    const val MAX_MARGIN_H_DP = 48f
    const val MAX_MARGIN_V_DP = 96f
    const val MIN_LETTER_SPACING = -0.05f
    const val MAX_LETTER_SPACING = 0.20f
}

@Serializable
data class TypographyConfig(
    val fontSizeSp: Float = 18f,
    val lineHeightMult: Float = 1.6f,
    val paragraphSpacingEm: Float = 0.8f,
    val marginHorizontalDp: Float = 16f,
    val marginVerticalDp: Float = 24f,
    /** "system" | "custom:<hash>" */
    val fontFamilyId: String = FONT_FAMILY_SYSTEM,
    val boldEnabled: Boolean = false,
    val align: TextAlignMode = TextAlignMode.START,
    val letterSpacingEm: Float = 0f,
    val indentFirstLine: Boolean = true,
) {
    companion object {
        const val FONT_FAMILY_SYSTEM = "system"
        val Default = TypographyConfig()
    }
}
