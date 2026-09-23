/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-23 10:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-23 10:00
 * @FilePath: data/sources/src/test/kotlin/com/kr/reader/data/sources/json/SourceJsonCodecTest.kt
 * @Description: 书源 JSON 编解码单测：三种包装形态与第三方平铺字段书源的兼容转换
 */
package com.kr.reader.data.sources.json

import com.google.common.truth.Truth.assertThat
import com.kr.reader.data.sources.SourceValidator
import org.junit.Test

class SourceJsonCodecTest {

    private val codec = SourceJsonCodec()
    private val validator = SourceValidator()

    @Test
    fun `数组形态解析出多条书源`() {
        val json = """[{"name":"A","baseUrl":"https://a.com","rules":${rulesJson()}}]"""
        val sources = codec.decodeMany(json)
        assertThat(sources).hasSize(1)
        assertThat(sources[0].name).isEqualTo("A")
    }

    @Test
    fun `sources 包装形态解析`() {
        val json = """{"sources":[{"name":"B","baseUrl":"https://b.com","rules":${rulesJson()}}]}"""
        assertThat(codec.decodeMany(json).map { it.name }).containsExactly("B")
    }

    @Test
    fun `第三方平铺字段书源转换为规则集`() {
        val json = """
            [{
              "bookSourceName": "示例书源",
              "bookSourceGroup": "测试",
              "bookSourceUrl": "https://example.com",
              "bookSourceCharset": "UTF-8#用于注释",
              "enabled": true,
              "searchUrl": "https://example.com/search?q={{key}}|char=gbk|,{"method":"GET"}",
              "ruleSearch": {"bookList": ".list li", "name": "h3@text", "bookUrl": "a@href"},
              "ruleBookInfo": {"name": "h1", "tocUrl": "/toc/1"},
              "ruleToc": {"chapterList": "#chapters li", "chapterName": "a", "chapterUrl": "a@href"},
              "ruleContent": {"content": "#content"}
            }]
        """.trimIndent()

        val source = codec.decodeMany(json).single()
        assertThat(source.name).isEqualTo("示例书源")
        assertThat(source.groupName).isEqualTo("测试")
        // 编码字段常带注释后缀，必须取注释前的真实编码
        assertThat(source.charset).isEqualTo("UTF-8")
        // 编码声明不能残留在 URL 里
        assertThat(source.rules.search.url).isEqualTo("https://example.com/search?q={{key}}")
        assertThat(source.rules.search.list).isEqualTo(".list li")
        assertThat(source.rules.search.name?.rule).isEqualTo("h3")
        assertThat(source.rules.search.detailUrl?.rule).isEqualTo("a@href")
        assertThat(source.rules.catalog.list).isEqualTo("#chapters li")
        assertThat(source.rules.detail.catalog?.pattern).isEqualTo("/toc/1")
        assertThat(source.rules.content.rule).isEqualTo("#content")

        assertThat(validator.validate(source).ok).isTrue()
    }

    @Test
    fun `非法 JSON 抛出可展示错误`() {
        val result = runCatching { codec.decodeMany("{not json") }
        assertThat(result.isFailure).isTrue()
    }

    @Test
    fun `缺少正文规则的书源被校验拦截`() {
        val json = """[{"name":"C","baseUrl":"https://c.com","rules":${rulesJson(content = "")}}]"""
        val source = codec.decodeMany(json).single()
        val validation = validator.validate(source)
        assertThat(validation.ok).isFalse()
        assertThat(validation.reason).contains("正文规则")
    }

    @Test
    fun `非法选择器被校验拦截`() {
        val json = """[{"name":"D","baseUrl":"https://d.com","rules":${rulesJson(content = "[[[")}}]"""
        val source = codec.decodeMany(json).single()
        assertThat(validator.validate(source).ok).isFalse()
    }

    private fun rulesJson(
        searchUrl: String = "https://x.com/s?q={{key}}",
        list: String = ".list li",
        content: String = "#content",
    ): String = """
        {
          "search": {"url": "$searchUrl", "list": "$list", "name": {"rule": "h3"}, "detailUrl": {"rule": "a@href"}},
          "detail": {"title": {"rule": "h1"}},
          "catalog": {"list": "#chapters li", "name": {"rule": "a"}, "url": {"rule": "a@href"}},
          "content": {"rule": "$content"}
        }
    """.trimIndent()
}
