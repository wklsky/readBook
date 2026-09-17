/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: data/parser/src/main/kotlin/com/kr/reader/data/parser/txt/CharOffsetIndex.kt
 * @Description: 字符偏移 → 字节偏移索引：单次流式扫描建立锚点，支持按字符偏移随机读取任意章节
 */
package com.kr.reader.data.parser.txt

import java.io.File
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.nio.charset.CoderResult
import java.nio.charset.CodingErrorAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 为什么需要它：TXT 按「字符偏移」定位（第 3 卷 3.1.2），而文件 IO 只能按字节 seek，
 * 中文在不同编码下每字 2~4 字节不等，必须建立真实映射而不是简单乘除。
 *
 * 为什么记录每个批次的锚点而不是全量索引：10MB 书籍约 1000 万字符，
 * 全量 IntArray 索引要约 40MB 内存，低端机直接 OOM；按批次记录只需几十 KB。
 */
class CharOffsetIndex private constructor(
    private val file: File,
    private val charset: Charset,
    /** 交错的 [charIndex, byteIndex] 锚点数组，按 charIndex 升序 */
    private val anchors: LongArray,
    val totalChars: Long,
    val totalBytes: Long,
) {

    companion object {

        private const val READ_BUFFER_BYTES = 8192
        private const val DECODE_BUFFER_CHARS = 8192

        suspend fun build(file: File, charset: Charset): CharOffsetIndex = withContext(Dispatchers.IO) {
            val decoder = charset.newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE)

            val byteBuffer = ByteBuffer.allocate(READ_BUFFER_BYTES)
            val charBuffer = CharBuffer.allocate(DECODE_BUFFER_CHARS)
            byteBuffer.flip() // position=0, limit=0：起始为空 buffer（读模式）

            val anchors = mutableListOf<Long>()
            var totalChars = 0L
            var totalBytes = 0L
            var endOfInput = false

            file.inputStream().use { input ->
                while (!endOfInput) {
                    // 把上一轮未解码完的残留字节（多字节字符的前半段）搬到缓冲区头部
                    byteBuffer.compact()
                    val space = byteBuffer.remaining()
                    val read = input.read(byteBuffer.array(), byteBuffer.arrayOffset() + byteBuffer.position(), space)
                    if (read > 0) {
                        byteBuffer.position(byteBuffer.position() + read)
                    } else {
                        endOfInput = true
                    }
                    byteBuffer.flip()

                    // 每个批次开始时记录一次锚点：此时的 (chars, bytes) 是精确对齐的
                    anchors.add(totalChars)
                    anchors.add(totalBytes)

                    while (byteBuffer.hasRemaining()) {
                        charBuffer.clear()
                        val result = decoder.decode(byteBuffer, charBuffer, endOfInput)
                        totalChars += charBuffer.position()
                        if (result === CoderResult.UNDERFLOW) break
                        if (result === CoderResult.OVERFLOW) continue
                        break
                    }
                    totalBytes += byteBuffer.position().toLong()
                    // 未消费的残留字节（多字节字符的前缀）留在 [position, limit)，由下一轮 compact 搬到队首
                }
                charBuffer.clear()
                decoder.flush(charBuffer)
                totalChars += charBuffer.position()
            }

            anchors.add(totalChars)
            anchors.add(totalBytes)

            CharOffsetIndex(
                file = file,
                charset = charset,
                anchors = anchors.toLongArray(),
                totalChars = totalChars,
                totalBytes = totalBytes,
            )
        }
    }

    /** 返回 ≤ targetChar 的最近锚点 (charIndex → byteIndex) */
    fun anchorFor(targetChar: Long): Pair<Long, Long> {
        if (anchors.size < 2) return 0L to 0L
        var bestChar = 0L
        var bestByte = 0L
        var i = 0
        while (i + 1 < anchors.size) {
            val anchorChar = anchors[i]
            if (anchorChar <= targetChar) {
                bestChar = anchorChar
                bestByte = anchors[i + 1]
            } else {
                break
            }
            i += 2
        }
        return bestChar to bestByte
    }

    suspend fun readChars(from: Long, to: Long): String = withContext(Dispatchers.IO) {
        if (to <= from) return@withContext ""
        val target = from.coerceIn(0L, totalChars)
        val end = to.coerceIn(from, totalChars)
        val (anchorChar, anchorByte) = anchorFor(target)
        file.inputStream().use { input ->
            input.skip(anchorByte)
            val reader = input.reader(charset).buffered(READ_BUFFER_BYTES)
            // 锚点可能早于目标若干字符，这里精确跳过偏差，保证任意编码下都不会错位
            var skipped = 0L
            val skipTarget = target - anchorChar
            while (skipped < skipTarget) {
                val s = reader.skip(skipTarget - skipped)
                if (s <= 0) break
                skipped += s
            }
            val wanted = (end - target).toInt()
            val buffer = CharArray(wanted)
            var filled = 0
            while (filled < wanted) {
                val read = reader.read(buffer, filled, wanted - filled)
                if (read <= 0) break
                filled += read
            }
            String(buffer, 0, filled)
        }
    }
}
