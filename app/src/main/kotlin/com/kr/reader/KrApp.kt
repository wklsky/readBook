/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 15:30
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 15:30
 * @FilePath: app/src/main/kotlin/com/kr/reader/KrApp.kt
 * @Description: 应用骨架：底部一级页签（书架/搜索/书源/设置）+ 二级沉浸页（阅读器/关于）
 */
package com.kr.reader

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kr.reader.navigation.KrNavHost
import com.kr.reader.navigation.KrRoutes

/** 一级页签模型：route 即 KrRoutes 常量，label 面向用户 */
private data class KrTab(
    val route: String,
    val label: String,
)

private val KrTabs = listOf(
    KrTab(KrRoutes.BOOKSHELF, "书架"),
    KrTab(KrRoutes.SEARCH, "搜索"),
    KrTab(KrRoutes.SOURCES, "书源"),
    KrTab(KrRoutes.SETTINGS, "设置"),
)

@Composable
fun KrApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    // 阅读器与关于页是沉浸式二级页，显示底部页签会挤占阅读区域并打断翻页手势
    val showBottomBar = KrTabs.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    KrTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    // 单一栈顶 + 状态恢复：来回切页签时保留各自滚动位置，避免返回栈无限堆积
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            label = { Text(text = tab.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        KrNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
