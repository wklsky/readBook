/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/designsystem/src/main/kotlin/com/kr/reader/core/designsystem/theme/KrTheme.kt
 * @Description: APP 级 Material3 主题。非阅读界面统一走这里，阅读器正文用 ReaderTheme 独立控制
 */
package com.kr.reader.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.kr.reader.core.model.ThemeMode

private val LightColors = lightColorScheme(
    primary = Color(0xFF8B6F47),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF6B8E23),
    background = Color(0xFFFAF8F5),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1C1B1A),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFD4A76A),
    onPrimary = Color(0xFF2B2216),
    secondary = Color(0xFF9BBF6B),
    background = Color(0xFF12100E),
    surface = Color(0xFF1C1A18),
    onBackground = Color(0xFFE6E1D8),
)

@Composable
fun KrTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = Typography(),
        content = content,
    )
}
