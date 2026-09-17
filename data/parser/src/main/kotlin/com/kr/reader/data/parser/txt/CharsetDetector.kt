/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: data/parser/src/main/kotlin/com/kr/reader/data/parser/txt/CharsetDetector.kt
 * @Description: TXT 文件编码检测（第 4 卷 4.2.4）：BOM → 尝试解码 → 统计替换字符 → UTF-16/32 启发式 → GBK兜底
 */
package com.kr.reader.data.parser.txt

import java.io.File
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

object CharsetDetector {

    private const val READ_LENGTH = 4096 * 4

    /**
     * 检测顺序刻意把 GBK 放在最后：
     * GBK 的容错性极强，几乎任何字节流都能「解码成功」，放在前面会把 UTF-8 文件误判成乱码。
     */
    fun detect(file: File): String {
        val head = readHeadBytes(file) ?: return "GBK"
        detectByBom(head)?.let { return it }

        // UTF-16 需要先做启发式检查：UTF-16 解码一个 UTF-8 文件通常也能成功，只是产生大量乱码
        detectUtf16(head)?.let { return it }

        val utf8 = standardCharsetIs(file, head, StandardCharsets.UTF_8, strictMode = true)
        return if (utf8) {
            "UTF-8"
        } else {
            val utf8IgnoreError = standardCharsetIs(file, head, StandardCharsets.UTF_8, strictMode = false)
            val gbk = standardCharsetIs(file, head, charsetOf("GBK"), strictMode = false)
            when {
                gbk && !utf8IgnoreError -> "GBK"
                utf8IgnoreError -> "UTF-8"
                gbk -> "GBK"
                else -> "GBK"
            }
        }
    }

    fun charsetCandidateList(): List<String> = listOf("UTF-8", "GBK", "GB18030", "UTF-16LE", "UTF-16BE", "BIG5")

    private const val SAMPLE_SIZE = 1024

    /**
     * 严格模式：写入侧与读出侧 byte-by-byte 一致才算通过。
     * 之所以允许非严格模式兜底：部分盗版 TXT 只有个别字节损坏，整体仍然可读，
     * 此时硬判为乱码会让用户完全打不开书。
     */
    private fun standardCharsetIs(file: File, head: ByteArray, charset: Charset, strictMode: Boolean): Boolean {
        val bytes = head.copyOfRange(0, minOf(head.size, SAMPLE_SIZE))
        val decoded = String(bytes, charset)
        if (decoded.isBlank()) return false
        if (decoded.contains('\uFFFD')) return false
        val reEncoded = decoded.toByteArray(charset)
        return if (strictMode) {
            reEncoded.contentEquals(bytes)
        } else {
            // 非严格模式允许少量损坏字节：看「能原样往返」的字节比例是否超过 80%
            val min = minOf(reEncoded.size, bytes.size)
            var same = 0
            for (i in 0 until min) {
                if (reEncoded[i] == bytes[i]) same++
            }
            same.toFloat() / min.toFloat() > 0.8f
        }
    }

    private fun detectByBom(head: ByteArray): String? = when {
        head.size >= 3 && head[0] == 0xEF.toByte() && head[1] == 0xBB.toByte() && head[2] == 0xBF.toByte() -> "UTF-8"
        head.size >= 4 && head[0] == 0xFF.toByte() && head[1] == 0xFE.toByte() &&
            head[2] == 0x00.toByte() && head[3] == 0x00.toByte() -> "UTF-32LE"
        head.size >= 4 && head[0] == 0x00.toByte() && head[1] == 0x00.toByte() &&
            head[2] == 0xFE.toByte() && head[3] == 0xFF.toByte() -> "UTF-32BE"
        head.size >= 2 && head[0] == 0xFF.toByte() && head[1] == 0xFE.toByte() -> "UTF-16LE"
        head.size >= 2 && head[0] == 0xFE.toByte() && head[1] == 0xFF.toByte() -> "UTF-16BE"
        else -> null
    }

    /**
     * UTF-16 启发式：ASCII 文本在 UTF-16LE/BE 中的表现是每隔一个字节出现 0x00。
     * 统计零字节占比即可高置信度区分。
     */
    private fun detectUtf16(head: ByteArray): String? {
        if (head.size < 4) return null
        var oddZero = 0
        var evenZero = 0
        val limit = minOf(head.size, 512) - (minOf(head.size, 512) % 2)
        for (i in 0 until limit) {
            if (head[i] == 0.toByte()) {
                if (i % 2 == 0) evenZero++ else oddZero++
            }
        }
        val half = limit / 2
        return when {
            oddZero > half * 0.6 -> "UTF-16LE"
            evenZero > half * 0.6 -> "UTF-16BE"
            else -> null
        }
    }

    private fun readHeadBytes(file: File): ByteArray? = runCatching {
        file.inputStream().use { input ->
            val buffer = ByteArray(READ_LENGTH)
            val read = input.read(buffer)
            if (read <= 0) null else buffer.copyOf(read)
        }
    }.getOrNull()

    fun charsetOf(name: String): Charset = runCatching { Charset.forName(name) }.getOrDefault(Charsets.UTF_8)
}
