/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/ui/src/main/kotlin/com/kr/reader/core/ui/components/BookCover.kt
 * @Description: 书籍封面：本地文件优先、其次网络 URL，都缺失时用书名首字做生成式封面
 */
package com.kr.reader.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

private const val COVER_ASPECT_RATIO = 3f / 4f

@Composable
fun BookCover(
    title: String,
    coverPath: String?,
    coverUrl: String?,
    modifier: Modifier = Modifier,
) {
    val model = when {
        !coverPath.isNullOrBlank() -> "file://$coverPath"
        !coverUrl.isNullOrBlank() -> coverUrl
        else -> null
    }
    Surface(
        modifier = modifier
            .aspectRatio(COVER_ASPECT_RATIO)
            .clip(RoundedCornerShape(6.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        if (model == null) {
            GeneratedCover(title = title)
        } else {
            AsyncImage(
                model = model,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/** 无封面时的降级封面：取标题首字，保证书架不会出现大量相同灰块 */
@Composable
private fun GeneratedCover(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = title.firstOrNull()?.toString().orEmpty(),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}
