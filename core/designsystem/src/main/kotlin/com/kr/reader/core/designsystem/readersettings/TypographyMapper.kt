/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/designsystem/src/main/kotlin/com/kr/reader/core/designsystem/readersettings/TypographyMapper.kt
 * @Description: 排版配置 → Compose TextStyle 映射（第 4 卷 4.5.1）：Swift 版旁白复制了一份实现，需同步修改
 */
package com.kr.reader.core.designsystem.readersettings

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.sp
import com.kr.reader.core.model.TextAlignMode
import com.kr.reader.core.model.TypographyConfig

/** 段间距：百分比 × 字号，取 1.6 倍行高做基准，视觉上等于「半个空行」到「一个空行」 */
fun TypographyConfig.paragraphSpacingPx(screenScale: Float = 1f): Float =
    fontSizeSp * (paragraphSpacingEm + 0.31f) * screenScale

fun TypographyConfig.letterSpacing(): TextUnit = TextUnit(letterSpacingEm, TextUnitType.Em)

fun TypographyConfig.toTextStyle(fontFamily: FontFamily = FontFamily.Default): TextStyle {
    val fontSize = fontSizeSp.sp
    return TextStyle(
        fontFamily = fontFamily,
        fontSize = fontSize,
        lineHeight = TextUnit(fontSizeSp * lineHeightMult, TextUnitType.Sp),
        letterSpacing = if (letterSpacingEm != 0f) {
            TextUnit(letterSpacingEm, TextUnitType.Em)
        } else {
            TextUnit.Unspecified
        },
        fontWeight = FontWeight.Normal,
        textAlign = if (align == TextAlignMode.JUSTIFY) TextAlign.Justify else TextAlign.Start,
        textIndent = if (indentFirstLine) TextIndent(firstLine = fontSize * 2) else TextIndent.None,
        // 段间距靠行高留白实现时会把首行也撑开，用 CENTER alignment + NONE trim 保证行盒竖直居中
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None,
        ),
    )
}
