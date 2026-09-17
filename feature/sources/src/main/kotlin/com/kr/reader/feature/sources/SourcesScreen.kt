/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 15:30
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 15:30
 * @FilePath: feature/sources/src/main/kotlin/com/kr/reader/feature/sources/SourcesScreen.kt
 * @Description: 书源管理页（导航骨架占位）：Phase D 落地书源列表与规则调试器
 */
package com.kr.reader.feature.sources

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
fun SourcesScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "书源管理 · Phase D 实现",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
