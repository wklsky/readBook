/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 15:30
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 15:30
 * @FilePath: feature/search/src/main/kotlin/com/kr/reader/feature/search/SearchScreen.kt
 * @Description: 搜索页（导航骨架占位）：Phase D 落地聚合搜索与结果归并
 */
package com.kr.reader.feature.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SearchScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "搜索 · Phase D 实现聚合搜索",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
