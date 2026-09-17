/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: data/pagination/src/main/kotlin/com/kr/reader/data/pagination/TextMeasurerHolder.kt
 * @Description: TextMeasurer 持有者：必须在首次 composition 完成后初始化，否则拿不到 Density
 */
package com.kr.reader.data.pagination

import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.Density
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 存在的理由：分页必须在 Composition tree 之外（Default Dispatcher）执行，
 * 而 TextMeasurer 只能在 @Composable 中通过 rememberTextMeasurer 获取，
 * 于是用一个全局 Holder 把两者桥接起来。
 *
 * 注意：measure 使用的 Density 必须来自同一个 composition，否则 sp→px 换算会与屏幕不符，
 * 导致排版出来的页数与实际渲染不一致。
 */
@Singleton
class TextMeasurerHolder @Inject constructor() {

    @Volatile
    private var holder: Pair<TextMeasurer, Density>? = null

    fun attach(textMeasurer: TextMeasurer, density: Density) {
        holder = textMeasurer to density
    }

    fun current(): Pair<TextMeasurer, Density>? = holder

    fun isReady(): Boolean = holder != null

    fun clear() {
        holder = null
    }
}
