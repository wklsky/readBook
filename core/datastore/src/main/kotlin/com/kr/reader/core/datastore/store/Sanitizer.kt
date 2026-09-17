/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/datastore/src/main/kotlin/com/kr/reader/core/datastore/store/Sanitizer.kt
 * @Description: 配置写盘前的合法化夹取：所有 Store 的 update 都过一遍，避免旧版本/手工修改产生脏配置
 */
package com.kr.reader.core.datastore.store

import com.kr.reader.core.model.GlobalPrefs
import com.kr.reader.core.model.ReaderPrefs
import com.kr.reader.core.model.ReaderTheme
import com.kr.reader.core.model.TypographyConfig
import com.kr.reader.core.model.TypographyRange

internal fun sanitize(config: TypographyConfig): TypographyConfig = config.copy(
    fontSizeSp = config.fontSizeSp.coerceIn(TypographyRange.MIN_FONT_SIZE_SP, TypographyRange.MAX_FONT_SIZE_SP),
    lineHeightMult = config.lineHeightMult.coerceIn(TypographyRange.MIN_LINE_HEIGHT, TypographyRange.MAX_LINE_HEIGHT),
    paragraphSpacingEm = config.paragraphSpacingEm.coerceIn(
        TypographyRange.MIN_PARAGRAPH_SPACING,
        TypographyRange.MAX_PARAGRAPH_SPACING,
    ),
    marginHorizontalDp = config.marginHorizontalDp.coerceIn(0f, TypographyRange.MAX_MARGIN_H_DP),
    marginVerticalDp = config.marginVerticalDp.coerceIn(0f, TypographyRange.MAX_MARGIN_V_DP),
    letterSpacingEm = config.letterSpacingEm.coerceIn(
        TypographyRange.MIN_LETTER_SPACING,
        TypographyRange.MAX_LETTER_SPACING,
    ),
)

internal fun sanitize(theme: ReaderTheme): ReaderTheme = theme.copy(
    // 亮度叠加上限 80%：超过后正文与背景对比度已低于可读阈值，继续压暗只会伤眼
    backgroundDimming = theme.backgroundDimming.coerceIn(0f, 0.8f),
)

internal fun sanitize(prefs: ReaderPrefs): ReaderPrefs = prefs.copy(
    autoScrollSpeed = prefs.autoScrollSpeed.coerceIn(1f, 10f),
    brightnessOverride = prefs.brightnessOverride.coerceIn(-1f, 1f),
    dimmingAlpha = prefs.dimmingAlpha.coerceIn(0f, 0.8f),
)

internal fun sanitize(prefs: GlobalPrefs): GlobalPrefs = prefs.copy(
    cacheLimitMb = prefs.cacheLimitMb.coerceIn(50, 2048),
    searchConcurrency = prefs.searchConcurrency.coerceIn(1, 12),
)
