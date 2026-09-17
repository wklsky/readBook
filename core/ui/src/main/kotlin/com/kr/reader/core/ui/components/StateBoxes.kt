/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/ui/src/main/kotlin/com/kr/reader/core/ui/components/StateBoxes.kt
 * @Description: 加载 / 空 / 错误三种标准状态容器，统一各页面的出错话术与重试入口
 */
package com.kr.reader.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kr.reader.core.model.AppError

@Composable
fun LoadingBox(modifier: Modifier = Modifier, message: String? = null) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            if (message != null) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }
    }
}

@Composable
fun EmptyBox(
    message: String,
    ctaText: String? = null,
    onCta: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = message, style = MaterialTheme.typography.bodyLarge)
            if (ctaText != null && onCta != null) {
                Button(onClick = onCta, modifier = Modifier.padding(top = 16.dp)) {
                    Text(ctaText)
                }
            }
        }
    }
}

/**
 * 错误统一展示：AppError.recoverable 决定是否给重试按钮。
 * 非 recoverable（如文件损坏）给按钮只会让用户反复失败。
 */
@Composable
fun ErrorBox(
    error: AppError,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = error.userMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
            )
            if (error.recoverable && onRetry != null) {
                TextButton(onClick = onRetry, modifier = Modifier.padding(top = 8.dp)) {
                    Text("重试")
                }
            }
        }
    }
}
