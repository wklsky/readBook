/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: core/common/src/main/kotlin/com/kr/reader/core/common/Hash.kt
 * @Description: 内容哈希工具：书籍去重键与字->字节映射 Cache 键的生成都需要稳定且快速的摘要
 */
package com.kr.reader.core.common

import java.security.MessageDigest

/** 内容去重哈希原文：[sizeBytes]:[sha1(前 N 字节)] */
private const val DEDUP_SAMPLE_BYTES = 65536

fun ByteArray.sha1Hex(): String {
    val digest = MessageDigest.getInstance("SHA-1").digest(this)
    return digest.joinToString(separator = "") { b -> "%02x".format(b) }
}

fun String.sha1Hex(): String = toByteArray().sha1Hex()

fun dedupHash(sizeBytes: Long, firstChunk: ByteArray): String {
    // 只取前 64KB：全文件哈希对 10MB 书籍会造成明显导入卡顿，且前 64KB 已足够区分绝大多数副本
    val sample = firstChunk.copyOf(minOf(firstChunk.size, DEDUP_SAMPLE_BYTES))
    return "$sizeBytes:${sample.sha1Hex()}"
}
