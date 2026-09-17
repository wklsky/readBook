/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: domain/src/main/kotlin/com/kr/reader/domain/usecase/PurifyContentUseCase.kt
 * @Description: 正文净化（第 4 卷 4.2.6）：删除盗版站点常见的 QQ 引流/book 红包/下标乱码
 */
package com.kr.reader.domain.usecase

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurifyContentUseCase @Inject constructor() {

    companion object {
        /** 四个以上成对【】包裹的运营文案，典型形态是 QQ 群引流广告 */
        private val QQ_WRAPPED_AD = Regex("【{4,}[^】]*】{4,}")
        /** 「错别字感谢xxx大红包」是盗版站的点赞引流话术 */
        private val THANKS_RED_PACKET = Regex("错别字感谢[^\\n]{0,20}大红包")
        /** 连续下划线 + 数字，通常是抓取模板残留的分隔线 */
        private val UNDERSCORE_COUNT = Regex("_{5,}\\d+")
        /** ⑤⑥⑦ 连续五个以上圈字，常见于盗版章节计数乱码 */
        private val CIRCLED_NUMBER = Regex("[\\u2460-\\u2468]{5,}")
        /** 行尾多余空白；中文正文里全角空格 U+3000 同样需要清理 */
        private val TRAILING_BLANK = Regex("[ \\t\\u3000]+$")
    }

    fun purify(content: String): String = content
        .let { THANKS_RED_PACKET.replace(it, "") }
        .let { UNDERSCORE_COUNT.replace(it, "") }
        .let { CIRCLED_NUMBER.replace(it, "") }
        .lineSequence()
        .map { line -> TRAILING_BLANK.replace(line, "") }
        .joinToString(separator = "\n")
        .let { QQ_WRAPPED_AD.replace(it, "") }
        .trim { it.isWhitespace() || it == '\u3000' }

    operator fun invoke(content: String): String = purify(content)
}
