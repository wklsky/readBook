/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 15:30
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 15:30
 * @FilePath: app/src/main/kotlin/com/kr/reader/navigation/KrNavHost.kt
 * @Description: 导航图：feature 之间零依赖，跨页跳转全部通过壳工程注入的回调串联
 */
package com.kr.reader.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.kr.reader.feature.about.AboutScreen
import com.kr.reader.feature.bookshelf.BookshelfScreen
import com.kr.reader.feature.reader.ReaderScreen
import com.kr.reader.feature.search.SearchScreen
import com.kr.reader.feature.settings.SettingsScreen
import com.kr.reader.feature.sources.SourcesScreen

@Composable
fun KrNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = KrRoutes.BOOKSHELF,
        modifier = modifier,
    ) {
        composable(KrRoutes.BOOKSHELF) {
            BookshelfScreen(
                onBookClick = { bookId -> navController.navigate(KrRoutes.reader(bookId)) },
            )
        }

        composable(
            route = KrRoutes.READER,
            arguments = listOf(navArgument(KrRoutes.ARG_BOOK_ID) { type = NavType.LongType }),
        ) { entry ->
            // 缺省 0L 仅在脏路由下出现；正常入口由书架点击回调携带真实 id
            val bookId = entry.arguments?.getLong(KrRoutes.ARG_BOOK_ID) ?: 0L
            ReaderScreen(
                bookId = bookId,
                onBack = navController::popBackStack,
            )
        }

        composable(KrRoutes.SEARCH) {
            SearchScreen()
        }

        composable(KrRoutes.SOURCES) {
            SourcesScreen()
        }

        composable(KrRoutes.SETTINGS) {
            SettingsScreen(
                onAboutClick = { navController.navigate(KrRoutes.ABOUT) },
            )
        }

        composable(KrRoutes.ABOUT) {
            AboutScreen(
                onBack = navController::popBackStack,
            )
        }
    }
}
