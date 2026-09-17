#!/usr/bin/env node
/**
 * build-archive.js
 * 把 docs/ 下的分卷 Markdown 合并为单一档案，并生成带侧边导航的 HTML 阅读版。
 *
 * 用法：node scripts/build-archive.js
 * 输出：KotlinReader-完整开发档案.md
 *       KotlinReader-开发档案.html
 */

'use strict';

const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const DOCS = path.join(ROOT, 'docs');
const OUT_MD = path.join(ROOT, 'KotlinReader-完整开发档案.md');
const OUT_HTML = path.join(ROOT, 'KotlinReader-开发档案.html');

// ─────────────────────────────────────────────────────────────
// 0. 收集源文件
// ─────────────────────────────────────────────────────────────
const FILES = [
  'README.md',
  '00-第0卷-项目总纲.md',
  '01-第1卷-技术选型与工程规范.md',
  '02-第2卷-系统架构设计.md',
  '03-第3卷-数据模型与存储设计.md',
  '04-第4卷-功能模块详细设计.md',
  '05-第5卷-关键算法实现方案.md',
  '06-第6卷-性能与NFR实现.md',
  '07-第7卷-测试与质量保障.md',
  '08-第8卷-开发计划与协作规范.md',
  '09-第9卷-使用手册.md',
  '10-第10卷-附录.md',
];

for (const f of FILES) {
  if (!fs.existsSync(path.join(DOCS, f))) {
    console.error(`缺少源文件: docs/${f}`);
    process.exit(1);
  }
}

// 文件名 → 锚点 id
function fileToAnchor(name) {
  if (name === 'README.md') return 'top';
  const m = /^(\d\d)-/.exec(name);
  return m ? `vol-${m[1]}` : null;
}

// ─────────────────────────────────────────────────────────────
// 1. 合并 Markdown
// ─────────────────────────────────────────────────────────────
const HEADER = `> **本文件说明**
>
> - 这是 \`docs/\` 下分卷文档的机械合并版，便于全文检索与打印。
> - 推荐阅读方式：在浏览器中打开 **\`KotlinReader-开发档案.html\`**（带侧边导航、目录过滤、代码高亮与一键复制）。
> - 本文件由 \`scripts/build-archive.js\` 自动生成，**请勿直接编辑**。修改内容请编辑 \`docs/\` 下的对应分卷文件，然后重新运行构建脚本。

---

`;

// 合并时把分卷之间的相对链接改写为档案内的锚点，避免死链
function rewriteInternalLinks(md) {
  return md.replace(/\]\((\.\/)?([^)]+\.md)(#[^)]*)?\)/g, (full, _dot, file, hash) => {
    const base = path.basename(file);
    const anchor = fileToAnchor(base);
    if (!anchor) return full;
    return `](#${anchor})`;
  });
}

const parts = [];
parts.push(HEADER);
for (const f of FILES) {
  let raw = fs.readFileSync(path.join(DOCS, f), 'utf8');
  raw = rewriteInternalLinks(raw);

  // 为每个分卷的首个 h1 注入显式锚点 id，使跨卷链接可跳转
  const anchor = fileToAnchor(f);
  if (anchor) {
    raw = raw.replace(/^#\s+(.+?)\s*$/m, (_m, title) => `# ${title} {#${anchor}}`);
  }

  parts.push(raw.trimEnd());
  parts.push('\n\n---\n\n');
}
const MERGED_MD = parts.join('\n').replace(/\n{4,}/g, '\n\n\n');
fs.writeFileSync(OUT_MD, MERGED_MD, 'utf8');
console.log(`✓ 已生成合并 Markdown：${path.relative(ROOT, OUT_MD)} (${(MERGED_MD.length / 1024).toFixed(0)} KB)`);

// ─────────────────────────────────────────────────────────────
// 2. Markdown → HTML
// ─────────────────────────────────────────────────────────────
const esc = (s) =>
  s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');

// GitHub 风格 slug（保留中日韩字符与数字字母）
function slugify(text) {
  return text
    .toLowerCase()
    .replace(/<[^>]+>/g, '')
    .replace(/[^\u4e00-\u9fa5\u3040-\u30ffa-z0-9\s-]/g, '')
    .trim()
    .replace(/\s+/g, '-');
}

// 标题 id 分配（基于块对象的稳定映射，保证预扫描与渲染结果一致）
const headingIds = new Map();   // block 对象 -> id
const allSlugs = new Set();

function assignHeadingIds(blocks) {
  const counter = new Map();
  const walk = (list) => {
    for (const b of list) {
      if (b.type === 'heading') {
        if (b.explicitId) {
          headingIds.set(b, b.explicitId);
          allSlugs.add(b.explicitId);
        } else if (b.level <= 3) {
          let s = slugify(b.text) || 'section';
          if (counter.has(s)) {
            const n = counter.get(s) + 1;
            counter.set(s, n);
            s = `${s}-${n}`;
          } else {
            counter.set(s, 1);
          }
          headingIds.set(b, s);
          allSlugs.add(s);
        }
      } else if (b.type === 'quote' && b.inner) {
        walk(b.inner);
      }
    }
  };
  walk(blocks);
}

function resolveHeadingId(b) {
  return headingIds.get(b) || '';
}

// ─────────────────────────────────────────────────────────────
// 2.1 行内元素解析（输入为已转义的文本）
// ─────────────────────────────────────────────────────────────
function inline(text, linkResolver) {
  const codes = [];
  // 先摘出行内代码，避免其内部被进一步解析
  let t = text.replace(/`([^`]+)`/g, (_m, c) => {
    codes.push(c);
    return `\u0000C${codes.length - 1}\u0000`;
  });

  // 复选框
  t = t.replace(/^\[ \]\s?/, '<span class="task">☐</span> ')
       .replace(/^\[x\]\s?/i, '<span class="task done">☑</span> ');

  // 链接
  t = t.replace(/\[([^\]]*)\]\(([^)\s]+)\)/g, (_m, label, href) => {
    const resolved = linkResolver(href);
    const external = /^https?:/i.test(href);
    if (!resolved) return `<span class="deadlink">${label}</span>`;
    return external
      ? `<a href="${resolved}" target="_blank" rel="noopener">${label}<span class="ext">↗</span></a>`
      : `<a href="${resolved}">${label}</a>`;
  });

  // 粗体 / 斜体 / 删除线
  t = t.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
  t = t.replace(/(^|[\s（(])\*([^*\n]+)\*(?=[\s）)。，,.]|$)/g, '$1<em>$2</em>');
  t = t.replace(/~~([^~]+)~~/g, '<del>$1</del>');

  // 放回行内代码
  t = t.replace(/\u0000C(\d+)\u0000/g, (_m, i) => `<code>${codes[+i]}</code>`);
  return t;
}

// ─────────────────────────────────────────────────────────────
// 2.2 代码高亮（轻量实现：注释 / 字符串 / 数字 / 关键字）
// ─────────────────────────────────────────────────────────────
const KEYWORDS = {
  kotlin: 'abstract|actual|annotation|as|break|by|catch|class|companion|const|constructor|continue|crossinline|data|delegate|do|dynamic|else|enum|expect|external|field|file|finally|for|fun|get|if|import|in|infix|init|inline|inner|interface|internal|is|lateinit|noinline|object|open|operator|out|override|package|param|private|protected|public|reified|return|sealed|set|super|suspend|tailrec|this|throw|try|typealias|val|var|vararg|when|where|while|true|false|null|it',
  java: 'abstract|assert|boolean|break|byte|case|catch|char|class|const|continue|default|do|double|else|enum|extends|final|finally|float|for|if|implements|import|instanceof|int|interface|long|native|new|package|private|protected|public|return|short|static|strictfp|super|switch|synchronized|this|throw|throws|transient|try|void|volatile|while|true|false|null',
  python: 'and|as|assert|async|await|break|class|continue|def|del|elif|else|except|finally|for|from|global|if|import|in|is|lambda|None|nonlocal|not|or|pass|raise|return|True|False|try|while|with|yield',
  bash: 'if|then|else|elif|fi|for|while|do|done|case|esac|function|return|local|export|echo|cd|source|set',
  protobuf: 'syntax|message|option|repeated|optional|required|enum|service|rpc|returns|string|int32|int64|bool|float|double|bytes|map|oneof|reserved|package|import|java_package|java_multiple_files|true|false',
  json: 'true|false|null',
  jsonc: 'true|false|null',
  yaml: 'true|false|null|yes|no',
  proguard: 'keep|keepclassmembers|keepclasseswithmembers|dontwarn|dontnote|dontoptimize|renamesourcefileattribute|keepattributes',
  xml: '',
  gradle: '',
  properties: '',
  text: '',
};

function highlight(code, lang) {
  const L = (lang || '').toLowerCase();
  const e = esc(code);

  // 语言特化的注释前缀
  const hashComment = ['yaml', 'toml', 'properties', 'bash', 'sh', 'proguard', 'python', 'conf'].includes(L);
  const slashComment = ['kotlin', 'java', 'json', 'jsonc', 'gradle', 'groovy', 'kt', 'js', 'ts'].includes(L);
  const xmlComment = L === 'xml' || L === 'html';
  const jsonLike = ['json', 'jsonc', 'protobuf', 'properties', 'toml', 'yaml'].includes(L);
  const kw = KEYWORDS[L] || '';

  const patterns = [];
  if (slashComment) patterns.push('\\/\\/[^\\n]*');
  if (hashComment) patterns.push('#[^\\n]*');
  if (xmlComment) patterns.push('&lt;!--[\\s\\S]*?--&gt;');
  patterns.push('\\/\\*[\\s\\S]*?\\*\\/');
  // 字符串（ASCII 直引号；esc() 不转义引号，故直接匹配）
  patterns.push('"(?:[^"\\\\\\n]|\\\\.)*"');
  patterns.push("'(?:[^'\\\\\\n]|\\\\.)*'");
  if (kw) patterns.push(`\\b(?:${kw})\\b`);
  patterns.push('\\b\\d+\\.?\\d*\\b');

  const re = new RegExp('(' + patterns.join('|') + ')', 'g');

  let out = '';
  let last = 0;
  let m;
  while ((m = re.exec(e)) !== null) {
    out += e.slice(last, m.index);
    const tok = m[0];
    let cls = '';
    if (/^(\/\/|#|&lt;!--|\/\*)/.test(tok)) cls = 'cm';
    else if (/^["']/.test(tok)) cls = 'st';
    else if (kw && new RegExp(`^(?:${kw})$`).test(tok)) cls = 'kw';
    else if (/^\d/.test(tok)) cls = 'nu';
    out += cls ? `<span class="${cls}">${tok}</span>` : tok;
    last = m.index + tok.length;
  }
  out += e.slice(last);
  return out;
}

// ─────────────────────────────────────────────────────────────
// 2.3 块级解析
// ─────────────────────────────────────────────────────────────
function parseBlocks(lines) {
  const blocks = [];
  let i = 0;

  const isTableRow = (l) => /^\s*\|.*\|\s*$/.test(l);
  const isTableSep = (l) => /^\s*\|[\s:|-]+\|\s*$/.test(l);
  const isListStart = (l) => /^\s*(?:[-*+]|\d+\.)\s+/.test(l);
  const isHeading = (l) => /^#{1,6}\s+/.test(l);
  const isHr = (l) => /^\s*(?:-{3,}|_{3,}|\*{3,})\s*$/.test(l);
  const isQuote = (l) => /^\s*>\s?/.test(l);

  while (i < lines.length) {
    const line = lines[i];

    // 空行
    if (!line.trim()) { i++; continue; }

    // 代码块
    const fence = /^\s*(`{3,}|~{3,})\s*([\w+-]*)\s*$/.exec(line);
    if (fence) {
      const marker = fence[1][0];
      const lang = fence[2] || '';
      const body = [];
      i++;
      while (i < lines.length && !new RegExp(`^\\s*${marker}{3,}\\s*$`).test(lines[i])) {
        body.push(lines[i]);
        i++;
      }
      i++; // 跳过结束围栏
      blocks.push({ type: 'code', lang, code: body.join('\n') });
      continue;
    }

    // 标题（支持行尾 {#explicit-id} 显式锚点）
    if (isHeading(line)) {
      const m = /^(#{1,6})\s+(.*)$/.exec(line);
      const raw = m[2].trim();
      const idm = /\s*\{#([\w-]+)\}\s*$/.exec(raw);
      blocks.push({
        type: 'heading',
        level: m[1].length,
        text: idm ? raw.slice(0, idm.index).trim() : raw,
        explicitId: idm ? idm[1] : null,
      });
      i++;
      continue;
    }

    // 分割线
    if (isHr(line)) { blocks.push({ type: 'hr' }); i++; continue; }

    // 表格
    if (isTableRow(line) && i + 1 < lines.length && isTableSep(lines[i + 1])) {
      const splitRow = (r) =>
        r.trim().replace(/^\||\|$/g, '')
          .replace(/\\\|/g, '\u0000PIPE\u0000')
          .split('|')
          .map((c) => c.replace(/\u0000PIPE\u0000/g, '|').trim());
      const header = splitRow(line);
      const aligns = splitRow(lines[i + 1]).map((c) => {
        const l = c.startsWith(':'), r = c.endsWith(':');
        return l && r ? 'center' : r ? 'right' : l ? 'left' : 'left';
      });
      i += 2;
      const rows = [];
      while (i < lines.length && isTableRow(lines[i])) {
        rows.push(splitRow(lines[i]));
        i++;
      }
      blocks.push({ type: 'table', header, aligns, rows });
      continue;
    }

    // 引用
    if (isQuote(line)) {
      const buf = [];
      while (i < lines.length && (isQuote(lines[i]) || (lines[i].trim() && buf.length && !isHeading(lines[i]) && !isListStart(lines[i]) && !isTableRow(lines[i])))) {
        buf.push(lines[i].replace(/^\s*>\s?/, ''));
        i++;
        if (i < lines.length && !lines[i].trim()) break;
      }
      blocks.push({ type: 'quote', lines: buf, inner: parseBlocks(buf) });
      continue;
    }

    // 列表
    if (isListStart(line)) {
      const items = [];
      while (i < lines.length) {
        const l = lines[i];
        if (!l.trim()) {
          // 空行后若仍是列表项则继续（宽松合并）
          if (i + 1 < lines.length && isListStart(lines[i + 1])) { i++; continue; }
          break;
        }
        const m = /^(\s*)(?:([-*+])|(\d+)\.)\s+(.*)$/.exec(l);
        if (m) {
          items.push({
            indent: m[1].replace(/\t/g, '  ').length,
            ordered: !!m[3],
            num: m[3] ? parseInt(m[3], 10) : null,
            text: m[4],
          });
          i++;
        } else if (/^\s{2,}\S/.test(l) && items.length) {
          // 续行
          items[items.length - 1].text += '\n' + l.trim();
          i++;
        } else break;
      }
      blocks.push({ type: 'list', items });
      continue;
    }

    // 段落
    const para = [];
    while (
      i < lines.length &&
      lines[i].trim() &&
      !isHeading(lines[i]) &&
      !isListStart(lines[i]) &&
      !isTableRow(lines[i]) &&
      !isQuote(lines[i]) &&
      !isHr(lines[i]) &&
      !/^\s*(`{3,}|~{3,})/.test(lines[i])
    ) {
      para.push(lines[i].trim());
      i++;
    }
    if (para.length) blocks.push({ type: 'para', text: para.join(' ') });
  }
  return blocks;
}

// ─────────────────────────────────────────────────────────────
// 2.4 渲染
// ─────────────────────────────────────────────────────────────
const toc = [];

function renderList(block, linkResolver) {
  const out = [];
  const stack = []; // [{ordered, indent, open: <ul|ol>}]

  const closeTo = (indent) => {
    while (stack.length && stack[stack.length - 1].indent >= indent && stack.length > 1) {
      out.push(`</li></${stack.pop().tag}>`);
    }
  };

  for (let k = 0; k < block.items.length; k++) {
    const it = block.items[k];
    const tag = it.ordered ? 'ol' : 'ul';

    if (!stack.length) {
      stack.push({ tag, indent: it.indent });
      out.push(`<${tag}${it.ordered && it.num && it.num !== 1 ? ` start="${it.num}"` : ''}>`);
    } else if (it.indent > stack[stack.length - 1].indent) {
      stack.push({ tag, indent: it.indent });
      out.push(`<${tag}${it.ordered && it.num && it.num !== 1 ? ` start="${it.num}"` : ''}>`);
    } else if (it.indent < stack[stack.length - 1].indent) {
      while (stack.length > 1 && it.indent < stack[stack.length - 1].indent) {
        out.push(`</li></${stack.pop().tag}>`);
      }
      if (stack.length && stack[stack.length - 1].tag !== tag) {
        out.push(`</li></${stack.pop().tag}>`);
        stack.push({ tag, indent: it.indent });
        out.push(`<${tag}>`);
      } else {
        out.push('</li>');
      }
    } else {
      out.push('</li>');
    }

    const body = it.text.split('\n').map((x) => inline(x, linkResolver)).join('<br>');
    out.push(`<li>${body}`);
  }
  while (stack.length) out.push(`</li></${stack.pop().tag}>`);
  return out.join('');
}

function renderBlocks(blocks, linkResolver) {
  const html = [];
  for (const b of blocks) {
    switch (b.type) {
      case 'heading': {
        const id = resolveHeadingId(b);
        const txt = inline(b.text, linkResolver);
        const attr = id ? ` id="${id}"` : '';
        html.push(`<h${b.level}${attr}>${txt}</h${b.level}>`);
        if (b.level <= 3) toc.push({ level: b.level, text: b.text, id: id || '' });
        break;
      }
      case 'code':
        html.push(
          `<div class="codeblock"${b.lang ? ` data-lang="${b.lang}"` : ''}>` +
          `<div class="codebar"><span class="lang">${b.lang || 'text'}</span>` +
          `<button class="copy" type="button" aria-label="复制代码">复制</button></div>` +
          `<pre><code>${highlight(b.code, b.lang)}</code></pre></div>`
        );
        break;
      case 'table': {
        const th = b.header
          .map((c, idx) => `<th style="text-align:${b.aligns[idx] || 'left'}">${inline(c, linkResolver)}</th>`)
          .join('');
        const trs = b.rows
          .map((r) => {
            const tds = b.header
              .map((_h, idx) => `<td style="text-align:${b.aligns[idx] || 'left'}">${inline(r[idx] || '', linkResolver)}</td>`)
              .join('');
            return `<tr>${tds}</tr>`;
          })
          .join('');
        html.push(`<div class="tblwrap"><table><thead><tr>${th}</tr></thead><tbody>${trs}</tbody></table></div>`);
        break;
      }
      case 'quote': {
        const inner = b.inner || renderBlocks(parseBlocks(b.lines), linkResolver);
        html.push(`<blockquote>${typeof inner === 'string' ? inner : renderBlocks(inner, linkResolver)}</blockquote>`);
        break;
      }
      case 'list':
        html.push(renderList(b, linkResolver));
        break;
      case 'hr':
        html.push('<hr>');
        break;
      case 'para':
        // 段落末的双空格换行已被合并，这里不处理
        html.push(`<p>${inline(b.text, linkResolver)}</p>`);
        break;
    }
  }
  return html.join('\n');
}

// ─────────────────────────────────────────────────────────────
// 3. 解析 → 分配标题锚点 → 渲染
// ─────────────────────────────────────────────────────────────
const mdLines = MERGED_MD.split(/\r?\n/);

// 只解析一次，保证块对象唯一（quote 的 inner 已在解析时缓存）
const blocks = parseBlocks(mdLines);
assignHeadingIds(blocks);

// 链接解析器
function linkResolver(href) {
  if (/^https?:/i.test(href)) return href;
  if (href.startsWith('#')) {
    const s = decodeURIComponent(href.slice(1));
    return allSlugs.has(s) ? `#${s}` : null;
  }
  return null;
}

// 正式渲染
const bodyHtml = renderBlocks(blocks, linkResolver);

// 构建 TOC 树
const tocHtml = (() => {
  const out = [];
  let open2 = false, open3 = false;
  for (const t of toc) {
    if (t.level === 1) {
      if (open3) { out.push('</div>'); open3 = false; }
      if (open2) { out.push('</li>'); open2 = false; }
      const href = t.id ? `#${t.id}` : '#top';
      out.push(`<li class="toc-vol"><a href="${href}">${inline(t.text, linkResolver)}</a></li>`);
    } else if (t.level === 2) {
      if (open3) { out.push('</div>'); open3 = false; }
      if (open2) out.push('</li>');
      out.push(`<li class="toc-sec"><a href="#${t.id}">${inline(t.text, linkResolver)}</a>`);
      open2 = true;
    } else if (t.level === 3) {
      if (!open3) { out.push('<div class="toc-sub">'); open3 = true; }
      out.push(`<a href="#${t.id}">${inline(t.text, linkResolver)}</a>`);
    }
  }
  if (open3) out.push('</div>');
  if (open2) out.push('</li>');
  return out.join('\n');
})();

const genDate = new Date().toISOString().slice(0, 10);
const totalChars = MERGED_MD.length;
const headingCount = toc.length;

const HTML = `<!DOCTYPE html>
<html lang="zh-CN" data-theme="dark">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Kotlin Reader · 完整开发档案</title>
<style>
:root{
  --bg:#0d1117; --bg-soft:#161b22; --bg-elev:#1c2128; --sidebar:#0a0e14;
  --text:#c9d1d9; --text-dim:#8b949e; --text-faint:#6e7681;
  --border:#21262d; --border-soft:#30363d;
  --accent:#d0a86a; --accent-soft:rgba(208,168,106,.14);
  --link:#7fb3d5; --code-bg:#11161d;
  --kw:#c78d5b; --st:#8fbc8f; --cm:#5f6b75; --nu:#b294bb;
  --shadow:0 1px 3px rgba(0,0,0,.4);
  --radius:8px;
}
html[data-theme="light"]{
  --bg:#fbf9f6; --bg-soft:#f4f0ea; --bg-elev:#ffffff; --sidebar:#f1ece4;
  --text:#2b2723; --text-dim:#6b6259; --text-faint:#948b81;
  --border:#e2dbd1; --border-soft:#d5ccc0;
  --accent:#8a6a3b; --accent-soft:rgba(138,106,59,.10);
  --link:#2f6f9f; --code-bg:#f6f2ec;
  --kw:#9c4c1c; --st:#3f7a3f; --cm:#928a80; --nu:#7a4f9c;
  --shadow:0 1px 2px rgba(0,0,0,.08);
}
*{box-sizing:border-box}
html{scroll-behavior:smooth;scroll-padding-top:24px}
body{
  margin:0;background:var(--bg);color:var(--text);
  font-family:-apple-system,BlinkMacSystemFont,"Segoe UI","PingFang SC","Hiragino Sans GB","Microsoft YaHei","Source Han Sans SC",sans-serif;
  font-size:15.5px;line-height:1.78;-webkit-font-smoothing:antialiased;
}
.layout{display:flex;min-height:100vh}

/* ── 侧边栏 ───────────────────────────── */
aside{
  width:326px;flex:0 0 326px;background:var(--sidebar);
  border-right:1px solid var(--border);
  position:sticky;top:0;height:100vh;overflow-y:auto;
  display:flex;flex-direction:column;
}
.brand{padding:20px 20px 14px;border-bottom:1px solid var(--border)}
.brand h1{margin:0;font-size:16px;letter-spacing:.4px;color:var(--text)}
.brand .sub{margin-top:5px;font-size:11.5px;color:var(--text-faint);letter-spacing:.5px}
.brand .meta{margin-top:9px;font-size:11px;color:var(--text-faint);display:flex;gap:10px;flex-wrap:wrap}
.searchbox{padding:12px 16px;border-bottom:1px solid var(--border);position:relative}
.searchbox input{
  width:100%;padding:8px 11px;border-radius:6px;border:1px solid var(--border-soft);
  background:var(--bg-soft);color:var(--text);font-size:13px;outline:none;font-family:inherit;
}
.searchbox input:focus{border-color:var(--accent)}
.searchbox .hint{margin-top:7px;font-size:11px;color:var(--text-faint)}
nav{padding:12px 10px 40px;flex:1}
nav ol,nav ul{list-style:none;margin:0;padding:0}
nav .toc-vol{margin-bottom:4px}
nav .toc-vol>a{
  display:block;padding:8px 11px;font-size:13.5px;font-weight:600;
  color:var(--accent);text-decoration:none;border-radius:6px;
  border-left:2px solid transparent;
}
nav .toc-vol>a:hover{background:var(--accent-soft);border-left-color:var(--accent)}
nav .toc-sec>a{
  display:block;padding:5px 11px 5px 20px;font-size:12.8px;color:var(--text-dim);
  text-decoration:none;border-radius:5px;
}
nav .toc-sec>a:hover{background:var(--bg-soft);color:var(--text)}
nav .toc-sub{display:flex;flex-direction:column;padding:2px 0 6px}
nav .toc-sub a{
  padding:3px 11px 3px 32px;font-size:12px;color:var(--text-faint);text-decoration:none;
  border-left:1px solid var(--border);margin-left:20px;
}
nav .toc-sub a:hover{color:var(--accent);border-left-color:var(--accent)}
nav a.active{background:var(--accent-soft);color:var(--accent);font-weight:600}
nav .noresult{padding:14px 16px;font-size:12.5px;color:var(--text-faint)}
nav .toc-sec.hidden,nav .toc-vol.hidden{display:none}

/* ── 主内容 ───────────────────────────── */
main{flex:1;min-width:0}
.toolbar{
  position:sticky;top:0;z-index:20;background:color-mix(in srgb,var(--bg) 88%,transparent);
  backdrop-filter:blur(10px);border-bottom:1px solid var(--border);
  display:flex;align-items:center;gap:10px;padding:9px 26px;
}
.toolbar .crumb{font-size:12.5px;color:var(--text-faint);flex:1;
  white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.toolbar button{
  background:var(--bg-soft);border:1px solid var(--border-soft);color:var(--text-dim);
  border-radius:6px;padding:5px 11px;font-size:12px;cursor:pointer;font-family:inherit;
  transition:.15s;white-space:nowrap;
}
.toolbar button:hover{border-color:var(--accent);color:var(--accent)}
.toolbar .progress{position:absolute;left:0;bottom:-1px;height:2px;background:var(--accent);width:0;transition:width .1s}
article{max-width:862px;margin:0 auto;padding:38px 26px 120px}

h1,h2,h3,h4,h5,h6{line-height:1.35;font-weight:650;scroll-margin-top:70px}
h1{font-size:27px;margin:0 0 22px;padding-bottom:16px;border-bottom:2px solid var(--border-soft);letter-spacing:.3px}
h2{font-size:21px;margin:46px 0 16px;padding-bottom:9px;border-bottom:1px solid var(--border)}
h3{font-size:17.5px;margin:32px 0 12px;color:var(--accent)}
h4{font-size:15.5px;margin:24px 0 9px}
h5{font-size:14.5px;margin:20px 0 8px;color:var(--text-dim)}
h6{font-size:14px;margin:18px 0 8px;color:var(--text-dim)}
p{margin:0 0 14px}
a{color:var(--link);text-decoration:none;border-bottom:1px solid transparent}
a:hover{border-bottom-color:var(--link)}
a .ext{font-size:.8em;opacity:.6;margin-left:2px}
.deadlink{color:var(--text-dim);border-bottom:1px dashed var(--border-soft);cursor:default}
strong{color:var(--text);font-weight:650}
em{font-style:italic;color:var(--text-dim)}
del{color:var(--text-faint)}
hr{border:0;border-top:1px solid var(--border);margin:46px 0}

ul,ol{margin:0 0 15px;padding-left:24px}
li{margin:5px 0}
li>ul,li>ol{margin:5px 0}
.task{color:var(--accent);font-weight:700}
.task.done{color:var(--st)}

code{
  font-family:"JetBrains Mono","Cascadia Code",Consolas,"SF Mono",Menlo,monospace;
  font-size:.875em;background:var(--code-bg);padding:2px 5.5px;border-radius:4px;
  border:1px solid var(--border);color:var(--accent);
}
.codeblock{
  position:relative;margin:0 0 20px;border-radius:var(--radius);overflow:hidden;
  border:1px solid var(--border);background:var(--code-bg);box-shadow:var(--shadow);
}
.codebar{
  display:flex;align-items:center;justify-content:space-between;
  padding:6px 12px;background:var(--bg-elev);border-bottom:1px solid var(--border);
}
.codebar .lang{
  font-size:10.5px;letter-spacing:1px;text-transform:uppercase;
  color:var(--text-faint);font-family:ui-monospace,monospace;
}
.codebar .copy{
  background:transparent;border:1px solid var(--border-soft);color:var(--text-faint);
  font-size:11px;padding:2px 8px;border-radius:4px;cursor:pointer;font-family:inherit;
}
.codebar .copy:hover{color:var(--accent);border-color:var(--accent)}
.codeblock pre{margin:0;padding:14px 16px;overflow-x:auto;line-height:1.62}
.codeblock code{
  background:none;border:0;padding:0;color:var(--text);
  font-size:12.9px;white-space:pre;
}
.kw{color:var(--kw);font-weight:600}
.st{color:var(--st)}
.cm{color:var(--cm);font-style:italic}
.nu{color:var(--nu)}

.tblwrap{overflow-x:auto;margin:0 0 20px;border:1px solid var(--border);border-radius:var(--radius)}
table{border-collapse:collapse;width:100%;font-size:13.6px}
th,td{padding:9px 13px;border-bottom:1px solid var(--border);vertical-align:top}
th{background:var(--bg-elev);font-weight:650;color:var(--text);white-space:nowrap;font-size:13px}
tbody tr:last-child td{border-bottom:0}
tbody tr:hover{background:var(--bg-soft)}
td code{font-size:12.3px}

blockquote{
  margin:0 0 18px;padding:13px 18px;border-left:3px solid var(--accent);
  background:var(--accent-soft);border-radius:0 var(--radius) var(--radius) 0;
  color:var(--text);
}
blockquote p:last-child{margin-bottom:0}
blockquote h1,blockquote h2,blockquote h3{font-size:15px;margin:0 0 8px;border:0;color:var(--accent)}

mark{background:var(--accent);color:#12161c;padding:0 3px;border-radius:2px}
.totop{
  position:fixed;right:26px;bottom:26px;z-index:30;
  width:40px;height:40px;border-radius:50%;border:1px solid var(--border-soft);
  background:var(--bg-elev);color:var(--text-dim);cursor:pointer;font-size:16px;
  box-shadow:var(--shadow);display:none;align-items:center;justify-content:center;
}
.totop.show{display:flex}
.totop:hover{color:var(--accent);border-color:var(--accent)}

.menubtn{display:none}
@media (max-width:1080px){
  aside{position:fixed;left:0;top:0;z-index:50;transform:translateX(-100%);transition:transform .22s}
  aside.open{transform:none;box-shadow:0 0 40px rgba(0,0,0,.5)}
  .menubtn{display:inline-block}
  article{padding:26px 18px 100px}
  .toolbar{padding:9px 16px}
  .totop{right:16px;bottom:16px}
}
@media print{
  aside,.toolbar,.totop{display:none}
  body{background:#fff;color:#000;font-size:11pt}
  .codeblock,table{break-inside:avoid}
  article{max-width:none;padding:0}
}
</style>
</head>
<body>
<div class="layout">
  <aside id="sidebar">
    <div class="brand">
      <h1>Kotlin Reader</h1>
      <div class="sub">完整开发档案 · v1.0</div>
      <div class="meta">
        <span>${headingCount} 个章节</span>
        <span>${(totalChars / 10000).toFixed(1)} 万字</span>
        <span>${genDate}</span>
      </div>
    </div>
    <div class="searchbox">
      <input id="q" type="search" placeholder="搜索章节标题…" autocomplete="off" spellcheck="false">
      <div class="hint" id="qhint">输入关键词过滤目录</div>
    </div>
    <nav><ol id="toc">
${tocHtml}
    </ol><div class="noresult" id="noresult" style="display:none">没有匹配的章节</div></nav>
  </aside>

  <main>
    <div class="toolbar">
      <button class="menubtn" id="menubtn" type="button">☰ 目录</button>
      <span class="crumb" id="crumb">Kotlin Reader · 完整开发档案</span>
      <button id="themebtn" type="button">☀ 浅色</button>
      <button id="printbtn" type="button">打印 / PDF</button>
      <span class="progress" id="progress"></span>
    </div>
    <article id="article">
${bodyHtml}
    </article>
  </main>
</div>
<button class="totop" id="totop" type="button" aria-label="回到顶部">↑</button>

<script>
(function(){
  var THEME_KEY='kr-archive-theme';
  var html=document.documentElement;
  var saved=localStorage.getItem(THEME_KEY)||'dark';
  html.setAttribute('data-theme',saved);
  var tbtn=document.getElementById('themebtn');
  function syncBtn(){tbtn.textContent=html.getAttribute('data-theme')==='dark'?'☀ 浅色':'☾ 深色';}
  syncBtn();
  tbtn.addEventListener('click',function(){
    var next=html.getAttribute('data-theme')==='dark'?'light':'dark';
    html.setAttribute('data-theme',next);localStorage.setItem(THEME_KEY,next);syncBtn();
  });
  document.getElementById('printbtn').addEventListener('click',function(){window.print();});

  // 侧边栏（窄屏）
  var sidebar=document.getElementById('sidebar');
  document.getElementById('menubtn').addEventListener('click',function(e){
    e.stopPropagation();sidebar.classList.toggle('open');
  });
  document.addEventListener('click',function(e){
    if(window.innerWidth<=1080&&sidebar.classList.contains('open')&&!sidebar.contains(e.target)){
      sidebar.classList.remove('open');
    }
  });
  sidebar.addEventListener('click',function(e){
    if(e.target.tagName==='A'&&window.innerWidth<=1080)sidebar.classList.remove('open');
  });

  // 复制代码
  document.querySelectorAll('.codeblock').forEach(function(block){
    var btn=block.querySelector('.copy');
    if(!btn)return;
    btn.addEventListener('click',function(){
      var code=block.querySelector('code');
      var text=code?code.innerText:'';
      var done=function(){btn.textContent='已复制';setTimeout(function(){btn.textContent='复制';},1400);};
      if(navigator.clipboard&&navigator.clipboard.writeText){
        navigator.clipboard.writeText(text).then(done).catch(function(){fallback(text,done);});
      }else{fallback(text,done);}
    });
  });
  function fallback(text,cb){
    var ta=document.createElement('textarea');ta.value=text;ta.style.position='fixed';
    ta.style.opacity='0';document.body.appendChild(ta);ta.select();
    try{document.execCommand('copy');cb();}catch(e){}
    document.body.removeChild(ta);
  }

  // 目录过滤
  var q=document.getElementById('q');
  var noresult=document.getElementById('noresult');
  var tocItems=Array.prototype.slice.call(document.querySelectorAll('#toc > li'));
  q.addEventListener('input',function(){
    var kw=q.value.trim().toLowerCase();
    if(!kw){
      tocItems.forEach(function(li){li.classList.remove('hidden');});
      document.getElementById('qhint').textContent='输入关键词过滤目录';
      noresult.style.display='none';return;
    }
    var hit=0;
    tocItems.forEach(function(li){
      var t=li.textContent.toLowerCase();
      var m=t.indexOf(kw)>-1;
      li.classList.toggle('hidden',!m);
      if(m)hit++;
    });
    document.getElementById('qhint').textContent=hit+' 个匹配';
    noresult.style.display=hit?'none':'block';
  });
  q.addEventListener('keydown',function(e){
    if(e.key==='Escape'){q.value='';q.dispatchEvent(new Event('input'));q.blur();}
    if(e.key==='Enter'){
      var first=document.querySelector('#toc > li:not(.hidden) a');
      if(first){first.click();}
    }
  });

  // 卷分隔优化：h1 自身已有下边框，去掉紧随其前的 hr，避免双线
  document.querySelectorAll('#article > h1').forEach(function(h1){
    var prev=h1.previousElementSibling;
    if(prev&&prev.tagName==='HR')prev.style.display='none';
  });

  // 阅读进度条 + 回到顶部 + 当前章节高亮
  var progress=document.getElementById('progress');
  var totop=document.getElementById('totop');
  var crumb=document.getElementById('crumb');
  var headings=Array.prototype.slice.call(document.querySelectorAll('#article > h1, #article > h2'));
  var links=Array.prototype.slice.call(document.querySelectorAll('#toc a'));
  var linkMap={};links.forEach(function(a){linkMap[a.getAttribute('href')]=a;});
  var ticking=false;
  function onScroll(){
    if(ticking)return;ticking=true;
    requestAnimationFrame(function(){
      var h=document.documentElement;
      var max=h.scrollHeight-h.clientHeight;
      var pct=max>0?(h.scrollTop/max)*100:0;
      progress.style.width=pct.toFixed(2)+'%';
      totop.classList.toggle('show',h.scrollTop>500);

      var cur=null;
      for(var i=0;i<headings.length;i++){
        if(headings[i].getBoundingClientRect().top<=110)cur=headings[i];else break;
      }
      links.forEach(function(a){a.classList.remove('active');});
      if(cur){
        var a=linkMap['#'+cur.id];
        if(a)a.classList.add('active');
        crumb.textContent=cur.textContent.trim();
      }
      ticking=false;
    });
  }
  window.addEventListener('scroll',onScroll,{passive:true});
  window.addEventListener('resize',onScroll);
  onScroll();

  totop.addEventListener('click',function(){window.scrollTo({top:0,behavior:'smooth'});});

  // 键盘快捷键
  document.addEventListener('keydown',function(e){
    if(e.target.tagName==='INPUT')return;
    if(e.key==='/'){e.preventDefault();q.focus();}
  });
})();
</script>
</body>
</html>`;

fs.writeFileSync(OUT_HTML, HTML, 'utf8');
console.log(`✓ 已生成 HTML 阅读版：${path.relative(ROOT, OUT_HTML)} (${(HTML.length / 1024).toFixed(0)} KB)`);
console.log(`  目录章节数：${toc.length}`);
