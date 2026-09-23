/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-23 10:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-23 10:00
 * @FilePath: data/sources/src/test/kotlin/com/kr/reader/data/sources/RuleEngineTest.kt
 * @Description: 规则引擎单测：三类提取语法与后处理管道是书源可用性的根基，必须逐条断言
 */
package com.kr.reader.data.sources

import com.google.common.truth.Truth.assertThat
import com.kr.reader.core.model.source.ContentRule
import com.kr.reader.domain.usecase.PurifyContentUseCase
import org.jsoup.Jsoup
import org.junit.Test

class RuleEngineTest {

    private val engine = RuleEngine(PurifyContentUseCase())

    private fun doc(html: String, baseUri: String = "https://a.com"): org.jsoup.nodes.Document =
        Jsoup.parse(html, baseUri)

    @Test
    fun `选择器提取文本`() {
        val document = doc("""<div class="book"><h3>斗破苍穹</h3><span>天蚕土豆</span></div>""")
        val root = document.selectFirst(".book")!!
        assertThat(engine.extract(ContentRule(rule = "h3"), root)).isEqualTo("斗破苍穹")
        assertThat(engine.extract(ContentRule(rule = "span"), root)).isEqualTo("天蚕土豆")
    }

    @Test
    fun `文本字段不会误取链接`() {
        // 回归用例：搜索条目普遍是 <a>，若不加区分地优先读 href，书名会整片变成 URL
        val document = doc("""<a class="book" href="/book/1.html">斗破苍穹</a>""")
        val root = document.selectFirst("a")!!
        assertThat(engine.extract(ContentRule(rule = "a"), root, "https://a.com")).isEqualTo("斗破苍穹")
        assertThat(engine.extract(ContentRule(rule = "a"), root, "https://a.com", wantUrl = true))
            .isEqualTo("https://a.com/book/1.html")
    }

    @Test
    fun `行尾锚定不会被误判成捕获组`() {
        val document = doc("""<p>共 1234 字</p>""")
        val root = document.body()
        assertThat(engine.extract(ContentRule(rule = """regex:共\s*\d+\s*字$"""), root)).isEqualTo("共 1234 字")
    }

    @Test
    fun `安全正则不被 ReDoS 检查误杀`() {
        // (第.章)(.*) 是并列结构而非嵌套量词，必须放行，否则用户的替换规则会静默失效
        assertThat(engine.applyProcessing("第一章 开局", "regex:(第.章)(.*)=>$1：$2")).isEqualTo("第一章： 开局")
        assertThat(engine.applyProcessing("aaa-bbb", "regex:(a+)-(b+)=>$2$1")).isEqualTo("bbbaaa")
    }

    @Test
    fun `属性语法取到绝对链接`() {
        val document = doc("""<a class="go" href="/book/1.html">详情</a>""")
        val root = document.selectFirst("a")!!
        assertThat(engine.extract(ContentRule(rule = "@href"), root, "https://a.com", wantUrl = true))
            .isEqualTo("https://a.com/book/1.html")
    }

    @Test
    fun `正则语法取捕获组`() {
        val document = doc("""<p>共 1234 字</p>""")
        val root = document.body()
        assertThat(engine.extract(ContentRule(rule = """regex:共\s*(\d+)\s*字$1"""), root)).isEqualTo("1234")
    }

    @Test
    fun `JsonPath 语法取值`() {
        val document = doc("""<script>{"data":{"title":"书名"}}</script>""")
        val root = document.selectFirst("script")!!
        assertThat(engine.extract(ContentRule(rule = "$.data.title"), root)).isEqualTo("书名")
    }

    @Test
    fun `后处理管道按顺序执行`() {
        val result = engine.applyProcessing("  你好&nbsp;&amp;我  ", "trim|unescape")
        assertThat(result).isEqualTo("你好 & 我")
    }

    @Test
    fun `后处理支持正则替换与默认值`() {
        assertThat(engine.applyProcessing("第一章 开局", """regex:第(\d+)章=>第$1节""")).isEqualTo("第一节 开局")
        assertThat(engine.applyProcessing("", "default:未知")).isEqualTo("未知")
    }

    @Test
    fun `危险正则被拒绝而不是拖死线程`() {
        // 嵌套量词是典型 ReDoS 写法，必须被 isSafePattern 拦下（返回原值即视为未执行）
        val dangerous = engine.applyProcessing("aaaaaaaaaaaaaaaaaaaa", "regex:(a+)+=>x")
        assertThat(dangerous).isEqualTo("aaaaaaaaaaaaaaaaaaaa")
    }

    @Test
    fun `正文按段落抽取并剔除脚本`() {
        val document = doc(
            """
            <div id="content">
              <script>var ad = 1;</script>
              <p>第一段内容</p>
              <p>第二段内容</p>
              <div class="ads">广告</div>
            </div>
            """.trimIndent(),
        )
        val text = engine.parseContent(ContentRule(rule = "#content"), document)
        assertThat(text).contains("第一段内容")
        assertThat(text).contains("第二段内容")
        assertThat(text).doesNotContain("广告")
        assertThat(text).doesNotContain("var ad")
    }

    @Test
    fun `正文规则匹配不到时明确报错`() {
        val document = doc("<html><body><p>hi</p></body></html>")
        runCatching { engine.parseContent(ContentRule(rule = "#not-exist"), document) }
            .onSuccess { throw AssertionError("应抛出异常以便调试器定位") }
            .onFailure { assertThat(it).isInstanceOf(IllegalStateException::class.java) }
    }

    @Test
    fun `相对链接解析为绝对链接`() {
        assertThat(engine.resolveUrl("https://a.com/x/y.html", "../z.html")).isEqualTo("https://a.com/z.html")
        assertThat(engine.resolveUrl("", "/a.html")).isEqualTo("/a.html")
    }
}
