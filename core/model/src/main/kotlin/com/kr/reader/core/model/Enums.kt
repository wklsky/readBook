/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/model/src/main/kotlin/com/kr/reader/core/model/Enums.kt
 * @Description: 全局枚举定义：书籍格式、书架排序、翻页模式等产品级选项集中于此，避免字符串散落各处
 */
package com.kr.reader.core.model

/** 书籍来源格式：本地三格式 + 网络书（书源体系） */
enum class BookFormat {
    TXT, EPUB, PDF, NET,

    ;

    companion object {
        fun fromExtension(name: String): BookFormat? = when (name.substringAfterLast('.', "").lowercase()) {
            "txt" -> TXT
            "epub" -> EPUB
            "pdf" -> PDF
            else -> null
        }
    }
}

/** 书架排序方式，对应 books 表的 ORDER BY 分支 */
enum class ShelfSort { LAST_READ, ADDED, TITLE, MANUAL }

/** 导入策略：归档（复制到私有目录）或引用（仅保存 URI 授权） */
enum class ImportMode { ARCHIVE, REFERENCE }

/** 五种翻页模式（第 4 卷 4.6.1） */
enum class PageTurnMode { SIMULATION, COVER, SLIDE, SCROLL, TAP }

/** 音量键行为 */
enum class VolumeKeyAction { NONE, PAGE_TURN, SCROLL, BRIGHTNESS }

/** 书源健康度：连续失败会 DEGRADED → BROKEN，聚合搜索跳过 BROKEN 源 */
enum class SourceHealth { OK, DEGRADED, BROKEN, UNKNOWN }

/** 正文对齐方式。不用 Compose 的 TextAlign 以保证 model 无 Android 依赖 */
enum class TextAlignMode { START, JUSTIFY }

/** 阅读器点击区域划分（第 4 卷 4.6.3） */
enum class TapZone { LEFT, CENTER, RIGHT, BOTTOM }

/** 界面显示模式：跟随系统 / 亮 / 暗 */
enum class ThemeMode { SYSTEM, LIGHT, DARK }
