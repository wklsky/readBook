/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 15:30
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 15:30
 * @FilePath: app/src/main/kotlin/com/kr/reader/navigation/KrRoutes.kt
 * @Description: 全局路由表：路由字符串与参数名集中定义，禁止在页面内硬编码跳转路径
 */
package com.kr.reader.navigation

object KrRoutes {
    const val BOOKSHELF = "bookshelf"
    const val READER = "reader/{$ARG_BOOK_ID}"
    const val SEARCH = "search"
    const val SOURCES = "sources"
    const val SETTINGS = "settings"
    const val ABOUT = "about"

    // 阅读进度按字符偏移恢复（红线约定），路由只携带 bookId，章节/偏移由进度仓储在阅读器内部解析
    const val ARG_BOOK_ID = "bookId"

    fun reader(bookId: Long): String = "reader/$bookId"
}
