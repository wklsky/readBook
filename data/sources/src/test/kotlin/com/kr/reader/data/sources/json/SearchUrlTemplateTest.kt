/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-23 10:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-23 10:00
 * @FilePath: data/sources/src/test/kotlin/com/kr/reader/data/sources/json/SearchUrlTemplateTest.kt
 * @Description: 搜索 URL 模板解析单测：第三方书源的 `url,{json}` 写法与编码声明必须被正确剥离
 */
package com.kr.reader.data.sources.json

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SearchUrlTemplateTest {

    @Test
    fun `纯 URL 不拆配置`() {
        val parsed = SearchUrlTemplate.parse("https://a.com/search?q={{key}}")
        assertThat(parsed.url).isEqualTo("https://a.com/search?q={{key}}")
        assertThat(parsed.method).isEqualTo("GET")
        assertThat(parsed.charset).isEqualTo("UTF-8")
    }

    @Test
    fun `URL 后挂 JSON 配置时应拆分`() {
        val parsed = SearchUrlTemplate.parse(
            """https://a.com/search,{"method":"POST","body":"kw={{key}}","headers":{"X-Token":"abc"}}""",
        )
        assertThat(parsed.url).isEqualTo("https://a.com/search")
        assertThat(parsed.method).isEqualTo("POST")
        assertThat(parsed.body).isEqualTo("kw={{key}}")
        assertThat(parsed.headers).containsEntry("X-Token", "abc")
    }

    @Test
    fun `内联编码声明既要生效也要从 URL 中剥离`() {
        val parsed = SearchUrlTemplate.parse("https://a.com/search?q={{key}}|char=gbk|")
        assertThat(parsed.charset).isEqualTo("gbk")
        assertThat(parsed.url).doesNotContain("char=")
    }

    @Test
    fun `查询参数里的 charset 不会被当成编码声明剥离`() {
        // 回归用例：早期正则允许竖线可选，会把正常的 ?charset=utf-8 查询参数删掉
        val parsed = SearchUrlTemplate.parse("https://a.com/search?q={{key}}&charset=utf-8")
        assertThat(parsed.url).isEqualTo("https://a.com/search?q={{key}}&charset=utf-8")
        assertThat(parsed.charset).isEqualTo("UTF-8")
    }

    @Test
    fun `关键字按声明编码渲染`() {
        val gbk = SearchUrlTemplate.render("https://a.com/s?q={{key}}", "斗破", 1, "GBK")
        assertThat(gbk).startsWith("https://a.com/s?q=%B6%B7%C6%C6")

        val utf8 = SearchUrlTemplate.render("https://a.com/s?q={{key}}", "斗破", 1, "UTF-8")
        assertThat(utf8).startsWith("https://a.com/s?q=%E6%96%97%E7%A0%B4")
    }

    @Test
    fun `页码占位符被替换`() {
        val url = SearchUrlTemplate.render("https://a.com/s?kw={{key}}&p={{page}}", "书", 3, "UTF-8")
        assertThat(url).contains("&p=3")
    }

    @Test
    fun `空模板返回空串而不是抛异常`() {
        assertThat(SearchUrlTemplate.parse("").url).isEmpty()
    }
}
