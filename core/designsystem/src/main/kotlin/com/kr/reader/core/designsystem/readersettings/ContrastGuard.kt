/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/designsystem/src/main/kotlin/com/kr/reader/core/designsystem/readersettings/ContrastGuard.kt
 * @Description: 阅读正文对比度守护（第 2 卷 2.6.3 无障碍底线）：低于 WCAG AA 时向黑/白混合，绝不使用异常色
 */
package com.kr.reader.core.designsystem.readersettings

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.math.pow

/** 对比度阈值：WCAG AA 正文 4.5:1，大字号 3:1 */
const val CONTRAST_RATIO_THRESHOLD = 4.5f

object ContrastGuard {

    fun ensureContrast(content: Color, background: Color): Color {
        if (contrastIsAcceptable(content, background)) return content
        val isDarkBg = background.luminance() < 0.5f
        val target = if (isDarkBg) Color.White else Color.Black
        return mix(content, target, 0.20f)
    }

    /**
     * WCAG 相对亮度公式不能直接用 Compose 的 luminance()（后者是线性 sRGB 版），
     * 这里严格按 W3C 建议重算伽马校正分量。
     */
    fun contrastRatio(a: Color, b: Color): Float {
        val l1 = wcagLuminance(a)
        val l2 = wcagLuminance(b)
        val lighter = maxOf(l1, l2)
        val darker = minOf(l1, l2)
        return (lighter + 0.05f) / (darker + 0.05f)
    }

    fun contrastIsAcceptable(content: Color, background: Color): Boolean =
        contrastRatio(content, background) >= CONTRAST_RATIO_THRESHOLD

    fun mix(from: Color, to: Color, ratio: Float): Color = Color(
        red = from.red + (to.red - from.red) * ratio,
        green = from.green + (to.green - from.green) * ratio,
        blue = from.blue + (to.blue - from.blue) * ratio,
        alpha = from.alpha,
    )

    private fun wcagLuminance(color: Color): Float {
        val r = channel(color.red)
        val g = channel(color.green)
        val b = channel(color.blue)
        return 0.2126f * r + 0.7152f * g + 0.0722f * b
    }

    private fun channel(value: Float): Float = if (value <= 0.03928f) {
        value / 12.92f
    } else {
        ((value + 0.055f) / 1.055f).pow(2.4f)
    }
}
