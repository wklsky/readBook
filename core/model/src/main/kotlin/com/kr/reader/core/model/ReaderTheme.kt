/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/model/src/main/kotlin/com/kr/reader/core/model/ReaderTheme.kt
 * @Description: 阅读主题与阅读偏好。颜色以 ARGB Long 存储，避免 model 层依赖 Compose Color
 */
package com.kr.reader.core.model

import kotlinx.serialization.Serializable

@Serializable
data class ReaderTheme(
    val id: String = THEME_PAPER,
    /** ARGB 打包值，UI 层用 Color(backgroundColor) 还原 */
    val backgroundColor: Long = 0xFFF5F1E8,
    val textColor: Long = 0xFF2B2622,
    val accentColor: Long = 0xFF8B6F47,
    /** PDF 夜间反色（滤镜） */
    val invertEnabled: Boolean = false,
    /** 叠加暗化 0.0~0.8，用于应用内亮度，不需要修改系统亮度 */
    val backgroundDimming: Float = 0f,
    val backgroundImagePath: String? = null,
) {
    companion object {
        const val THEME_PAPER = "paper"
        const val THEME_EYE_CARE = "eye_care"
        const val THEME_PARCHMENT = "parchment"
        const val THEME_NIGHT = "night"
        const val THEME_NIGHT_GRAY = "night_gray"
        const val THEME_HIGH_CONTRAST = "high_contrast"
        const val THEME_CUSTOM = "custom"

        // 夜间模式正文刻意不用纯白：纯白配纯黑会产生光晕扩散(halation)，长时间阅读更易疲劳
        val Paper = ReaderTheme(THEME_PAPER, 0xFFF5F1E8, 0xFF2B2622, 0xFF8B6F47)
        val EyeCare = ReaderTheme(THEME_EYE_CARE, 0xFFCCE8CF, 0xFF1F2D20, 0xFF3E6B45)
        val Parchment = ReaderTheme(THEME_PARCHMENT, 0xFFE8D9B5, 0xFF3B2F1E, 0xFF8A6A3B)
        val Night = ReaderTheme(THEME_NIGHT, 0xFF000000, 0xFFB8B8B8, 0xFF5A7A5A)
        val NightGray = ReaderTheme(THEME_NIGHT_GRAY, 0xFF121212, 0xFFC5C5C5, 0xFF7A9E7A)
        val HighContrast = ReaderTheme(THEME_HIGH_CONTRAST, 0xFFFFFFFF, 0xFF000000, 0xFF0000CC)

        val Presets: List<ReaderTheme> = listOf(Paper, EyeCare, Parchment, Night, NightGray, HighContrast)
    }
}

@Serializable
data class ReaderPrefs(
    val pageTurnMode: PageTurnMode = PageTurnMode.COVER,
    val showBattery: Boolean = true,
    val showClock: Boolean = true,
    val showChapterPercent: Boolean = true,
    val showPageNumber: Boolean = true,
    /** 0 表示跟随系统息屏时间 */
    val screenTimeoutOverride: Int = 0,
    val keepScreenOn: Boolean = false,
    /** 自动滚屏速度 1.0 ~ 10.0 */
    val autoScrollSpeed: Float = 5f,
    val autoScrollEnabled: Boolean = false,
    val tapZoneEnabled: Boolean = true,
    val swapTapZone: Boolean = false,
    val vibrationOnTurn: Boolean = false,
    val volumeKeyAction: VolumeKeyAction = VolumeKeyAction.NONE,
    /** -1 表示跟随系统亮度 */
    val brightnessOverride: Float = -1f,
    val dimmingEnabled: Boolean = false,
    val dimmingAlpha: Float = 0f,
    val fullScreenImmersive: Boolean = true,
) {
    companion object {
        val Default = ReaderPrefs()
    }
}

@Serializable
data class GlobalPrefs(
    val language: String = "zh-CN",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val shelfGridMode: Boolean = true,
    val shelfSort: ShelfSort = ShelfSort.LAST_READ,
    val cacheLimitMb: Int = 200,
    val cacheWifiOnly: Boolean = true,
    val searchConcurrency: Int = 6,
    val autoPurify: Boolean = true,
    /** 默认关闭崩溃上报，尊重隐私红线 */
    val crashReportEnabled: Boolean = false,
    val onboardingDone: Boolean = false,
    val importMode: ImportMode = ImportMode.ARCHIVE,
) {
    companion object {
        val Default = GlobalPrefs()
    }
}
