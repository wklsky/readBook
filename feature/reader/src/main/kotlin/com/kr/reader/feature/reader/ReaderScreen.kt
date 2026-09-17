/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 15:30
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 15:30
 * @FilePath: feature/reader/src/main/kotlin/com/kr/reader/feature/reader/ReaderScreen.kt
 * @Description: 阅读器页（导航骨架占位）：Phase B 落地分页渲染、翻页手势与字符级进度恢复
 */
package com.kr.reader.feature.reader

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
fun ReaderScreen(
    // 路由参数：由壳工程从 reader/{bookId} 解析后传入
    bookId: Long,
    // 返回书架回调：阅读器内部不持有 NavController
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "阅读器", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "Phase B 实现 · bookId = $bookId",
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onBack) {
            Text(text = "返回书架")
        }
    }
}
