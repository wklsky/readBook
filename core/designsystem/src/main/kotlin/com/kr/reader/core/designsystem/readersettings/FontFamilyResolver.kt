/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/designsystem/src/main/kotlin/com/kr/reader/core/designsystem/readersettings/FontFamilyResolver.kt
 * @Description: 自定义正文字体解析：Typeface 创建是 IO 且有上限成本，必须缓存与去重
 */
package com.kr.reader.core.designsystem.readersettings

import android.graphics.Typeface
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.kr.reader.core.model.TypographyConfig
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FontFamilyResolver @Inject constructor() {

    private val cache = ConcurrentHashMap<String, FontFamily>()

    fun resolve(fontFamilyId: String, fontPathProvider: (String) -> String?): FontFamily {
        if (fontFamilyId == TypographyConfig.FONT_FAMILY_SYSTEM) return FontFamily.Default
        val cached = cache[fontFamilyId]
        if (cached != null) return cached
        val path = fontPathProvider(fontFamilyId) ?: return FontFamily.Default
        val file = File(path)
        if (!file.exists()) return FontFamily.Default
        return runCatching {
            val typeface = Typeface.createFromFile(file)
            FontFamily(Font(typeface))
        }.getOrDefault(FontFamily.Default).also { cache[fontFamilyId] = it }
    }

    fun fontFamily(fontFamilyId: String, path: String?): FontFamily = resolve(fontFamilyId) { path }

    fun evict(fontFamilyId: String) {
        cache.remove(fontFamilyId)
    }
}
