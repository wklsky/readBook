/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 15:30
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 15:30
 * @FilePath: feature/bookshelf/src/main/kotlin/com/kr/reader/feature/bookshelf/BookshelfScreen.kt
 * @Description: 书架页（导航骨架占位）：Phase B 落地书籍列表、排序与 SAF 导入入口
 */
package com.kr.reader.feature.bookshelf

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BookshelfScreen(
    // 点击书籍回调：回传书籍 ID，由壳工程导航到 reader/{bookId}
    onBookClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "书架", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "Phase B 实现：列表 / 排序 / 导入",
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(12.dp))
        // 临时入口：仅用于首编译后冒烟验证书架 → 阅读器的路由链路
        Button(onClick = { onBookClick(0L) }) {
            Text(text = "打开阅读器骨架")
        }
    }
}
