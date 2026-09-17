/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/datastore/src/main/kotlin/com/kr/reader/core/datastore/store/TypographyStore.kt
 * @Description: 排版参数存储：写入侧统一执行范围夹取，保证 Stored 数据永远落在 TypographyRange 内
 */
package com.kr.reader.core.datastore.store

import androidx.datastore.core.DataStore
import com.kr.reader.core.model.TextAlignMode
import com.kr.reader.core.model.TypographyConfig
import com.kr.reader.core.model.TypographyRange
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class TypographyStore @Inject constructor(
    private val dataStore: DataStore<TypographyConfig>,
) {

    val data: Flow<TypographyConfig> = dataStore.data

    suspend fun setFontSizeSp(value: Float) = update { copy(fontSizeSp = value.clampFont()) }

    suspend fun setLineHeight(value: Float) = update {
        copy(lineHeightMult = value.coerceIn(TypographyRange.MIN_LINE_HEIGHT, TypographyRange.MAX_LINE_HEIGHT))
    }

    suspend fun setParagraphSpacing(value: Float) = update {
        copy(
            paragraphSpacingEm = value.coerceIn(
                TypographyRange.MIN_PARAGRAPH_SPACING,
                TypographyRange.MAX_PARAGRAPH_SPACING,
            ),
        )
    }

    suspend fun setMarginHorizontal(dp: Float) = update { copy(marginHorizontalDp = dp.coerceIn(0f, TypographyRange.MAX_MARGIN_H_DP)) }

    suspend fun setMarginVertical(dp: Float) = update { copy(marginVerticalDp = dp.coerceIn(0f, TypographyRange.MAX_MARGIN_V_DP)) }

    suspend fun setFontFamily(id: String) = update { copy(fontFamilyId = id) }

    suspend fun setBold(enabled: Boolean) = update { copy(boldEnabled = enabled) }

    suspend fun setAlign(mode: TextAlignMode) = update { copy(align = mode) }

    suspend fun setLetterSpacing(em: Float) = update {
        copy(letterSpacingEm = em.coerceIn(TypographyRange.MIN_LETTER_SPACING, TypographyRange.MAX_LETTER_SPACING))
    }

    suspend fun setIndentFirstLine(enabled: Boolean) = update { copy(indentFirstLine = enabled) }

    suspend fun reset() = update { TypographyConfig.Default }

    suspend fun update(mutator: TypographyConfig.() -> TypographyConfig) {
        dataStore.updateData { current -> sanitize(mutator(current)) }
    }

    private fun Float.clampFont(): Float = coerceIn(TypographyRange.MIN_FONT_SIZE_SP, TypographyRange.MAX_FONT_SIZE_SP)
}
