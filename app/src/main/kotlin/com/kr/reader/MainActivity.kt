/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 15:30
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 15:30
 * @FilePath: app/src/main/kotlin/com/kr/reader/MainActivity.kt
 * @Description: 单 Activity 宿主：全局唯一的 ComponentActivity，所有页面均为 Compose 路由
 */
package com.kr.reader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kr.reader.core.designsystem.theme.KrTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // 阅读器正文另有 ReaderTheme 独立配色（ContrastGuard 把关），这里只管非阅读界面
            KrTheme {
                KrApp()
            }
        }
    }
}
