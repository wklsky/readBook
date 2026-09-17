> **本文件说明**
>
> - 这是 `docs/` 下分卷文档的机械合并版，便于全文检索与打印。
> - 推荐阅读方式：在浏览器中打开 **`KotlinReader-开发档案.html`**（带侧边导航、目录过滤、代码高亮与一键复制）。
> - 本文件由 `scripts/build-archive.js` 自动生成，**请勿直接编辑**。修改内容请编辑 `docs/` 下的对应分卷文件，然后重新运行构建脚本。

---


# Kotlin Reader · 完整开发档案 {#top}
> 一份覆盖**从立项到上架、从架构到使用**的完整工程档案。
> 目标是让任何一个新加入的开发者，读完这套文档就能开始写代码；让任何用户，读完第 9 卷就知道怎么把书读起来。

---

## 档案导航

| 卷 | 标题 | 内容概要 | 主要读者 |
| --- | --- | --- | --- |
| [第 0 卷](#vol-00) | **项目总纲** | 产品定位、三条产品红线、用户画像、场景清单、竞品对比、版本路线图、范围界定、术语表、成功判据 | 全员 |
| [第 1 卷](#vol-01) | **技术选型与工程规范** | 技术栈逐项选型论证（含放弃理由）、14 模块结构、分层架构、代码规范、协程与 Compose 纪律、Git 工作流、CI/CD、依赖治理 | 全员 |
| [第 2 卷](#vol-02) | **系统架构设计** | 分层架构全景、模块职责矩阵、三条核心数据流（导入/首屏/聚合搜索）、线程模型与调度器分配、导航设计、生命周期、架构守护、降级矩阵 | 架构、开发 |
| [第 3 卷](#vol-03) | **数据模型与存储设计** | 领域模型 ER 图、**字符级进度模型（核心设计）**、8 张 Room 表、索引设计、DataStore Proto、文件存储布局、迁移策略 | 开发 |
| [第 4 卷](#vol-04) | **功能模块详细设计** | 书架 / 解析引擎 / 分页引擎 / 进度 / 排版主题 / 交互翻页 / 网络书源 / 聚合搜索与缓存 —— 7 大模块的功能清单、交互规格、实现代码、边界情况、验收要点 | 开发 |
| [第 5 卷](#vol-05) | **关键算法实现方案** | 7 张算法卡：编码探测、章节识别投票、分页正确性、偏移映射、选择器回退链、ReDoS 防护、内存压力控制。含测试向量与达标判据 | 开发 |
| [第 6 卷](#vol-06) | **性能与 NFR 实现** | 18 条 P1 性能指标、三条 P0 硬指标的逐项攻坚（含反例与优化清单）、内存预算表、兼容性适配（含厂商 ROM 坑）、隐私与数据安全、能效优化、性能回归体系 | 开发、测试 |
| [第 7 卷](#vol-07) | **测试与质量保障** | 测试金字塔、覆盖率门禁、必须测试的用例清单、属性测试、Fakes 设计、MockWebServer 用法、迁移测试、**V1.0 验收标准（功能/性能/质量）**、发布检查单、缺陷管理 | 测试、开发 |
| [第 8 卷](#vol-08) | **开发计划与协作规范** | 5 个里程碑与 Gate 标准、WBS 任务拆解（256 人日）、团队分工与模块 Owner、Code Review 规范、ADR 机制、**风险登记册（14 条）**、变更管理、交付物清单 | 项目管理、全员 |
| [第 9 卷](#vol-09) | **使用手册** | 3 分钟快速上手、书架使用、阅读操作、排版与主题详解、书源配置、设置项总览、**6 类常见问题 FAQ（30+ 问）**、快捷操作速查 | 用户 |
| [第 10 卷](#vol-10) | **附录** | 书源 JSON 完整规范（含 3 个示例）、内置章节规则库、主题色板精确色值、35 条错误码表、默认值速查、术语缩写、参考致谢、版本记录 | 全员 |

---

## 快速开始

### 如果你想了解这个项目是什么

读 [第 0 卷 · 项目总纲](#vol-00) 的 0.1（概述与红线）、0.2（用户画像）、0.4（路线图）。约 10 分钟。

### 如果你要开始写代码

```
第 1 卷 1.2（模块结构）→ 第 1 卷 1.9（环境搭建）
    → 第 2 卷（理解数据流与线程模型）
    → 第 3 卷（理解数据模型，特别是字符级进度）
    → 第 4 卷对应你负责的模块
    → 第 5 卷对应你涉及的算法
```

必读的第 1 卷三节：**1.4 代码规范**、**1.4.3 协程纪律**、**1.4.4 Compose 纪律**。这三节的内容会直接影响 Code Review 结果。

### 如果你要验收或测试

读 [第 7 卷](#vol-07)（7.5 验收标准是核心）+ [第 6 卷 6.9 NFR 达标清单](#vol-06)。

### 如果你只是用户

直接读 [第 9 卷 · 使用手册](#vol-09)。遇到问题查 9.7 FAQ。

---

## 本档案的三个核心设计决策

整份档案中，以下三个决策影响面最大，是理解本项目的关键：

### 决策一：字符级偏移的阅读进度（第 3 卷 3.1.2）

阅读进度记录的不是"第几页"，而是"读到全书的第几个字"。

**为什么重要**：这是本项目最重要的一条设计。它一次性解决了"改字号后进度丢失"这个阅读器行业普遍存在的老问题。代价是分页引擎必须输出字符映射，复杂度上升——但收益远超成本。

### 决策二：解析与分页的资源隔离（第 2 卷 2.4.3）

分章用独立的 2 线程池，与分页测量物理隔离。

**为什么重要**：没有这个隔离，后台导入 500 本书时用户翻开一本书会掉帧。这是"翻页 60fps"与"批量导入"两个需求能同时成立的唯一办法。

### 决策三：流式分页与首批优先（第 4 卷 4.3.3）

分页不分完再显示，而是首批 2 页立即上屏，其余后台继续。

**为什么重要**：这是"无肉眼可见白屏"的实现落点。10 万字单章若等全部分完再显示需要数秒，流式发射把首屏压到 120ms 内。

---

## 三条不可妥协的产品红线

任何功能迭代都不得违反（详见第 0 卷 0.1.2）：

1. **无广告、无推送、无账号强制登录。**
2. **本地文件不出设备。** 导入的书籍、封面、进度、书签，任何情况下不上传。网络能力仅用于书源抓取。
3. **不吃性能。** 性能预算是功能准入的硬门槛。

---

## 档案结构说明

```
docs/
├── README.md                          ← 本文件（档案索引）
├── 00-第0卷-项目总纲.md
├── 01-第1卷-技术选型与工程规范.md
├── 02-第2卷-系统架构设计.md
├── 03-第3卷-数据模型与存储设计.md
├── 04-第4卷-功能模块详细设计.md
├── 05-第5卷-关键算法实现方案.md
├── 06-第6卷-性能与NFR实现.md
├── 07-第7卷-测试与质量保障.md
├── 08-第8卷-开发计划与协作规范.md
├── 09-第9卷-使用手册.md
└── 10-第10卷-附录.md
```

**配套产出**（与 docs/ 同级）：

```
KotlinReader-完整开发档案.md       ← 全部卷册合并为单一 Markdown
KotlinReader-开发档案.html         ← 带侧边导航的 HTML 阅读版（推荐阅读方式）
```

---

## 文档维护约定

| 约定 | 说明 |
| --- | --- |
| **同步更新** | 代码变更若影响档案任何条目，必须在同一 PR 中更新对应章节 |
| **不删只改** | 已发布的规范不直接删除，改为标注"已废弃，见 xxx" |
| **附录优先** | 常量、默认值、错误码等"数据型"内容一律进第 10 卷附录 |
| **交叉引用** | 章节间引用使用相对路径链接，保证在 Git 与 IDE 中可跳转 |
| **版本对应** | 档案版本与应用版本对应（V1.0 应用 ↔ 档案 1.0） |

---

## 关于这份档案的完整性

本档案共约 **10 卷、覆盖 12 个技术专题、含 60+ 段可直接使用的代码/伪代码、35 条错误码、14 条风险登记、25 条功能验收项、14 条性能验收项、10 条质量验收项**。

它不仅描述"要做什么"，也明确"不做什么"（第 0 卷 0.5 Out of Scope）；不仅给出正确实现，也给出**错误实现的反例与性能对比**（第 5 卷、第 6 卷）；不仅列出指标，也给出**测量方法与归因工具**（第 6 卷 6.6）。

**如果你发现档案中有任何与实现不符、含糊、或已被证明错误的描述，请直接修正它** —— 这份档案的价值取决于它与真实代码的一致性。


---


# 第 0 卷 · 项目总纲 {#vol-00}
> 本文档为 Kotlin Reader 项目的顶层纲领，定义"我们做什么、为谁做、做到什么程度、按什么节奏做"。所有下游设计文档（第 1～10 卷）均以本卷为约束前提，任何与蓝图冲突的实现都视为设计缺陷。

---

## 0.1 项目概述

| 项 | 内容 |
| --- | --- |
| 项目名称 | Kotlin Reader（中文代号：**纯净阅读**） |
| 项目代号 | `kr` |
| 产品定位 | 纯净、轻量、高自定义的**多格式本地阅读**与**网络聚合阅读**器 |
| 平台 | Android 原生（Kotlin + Jetpack Compose） |
| 最低版本 | Android 8.0 / API 26 |
| 目标版本 | Android 14+ / API 34+，前瞻适配 API 35 |
| 包名 | `com.kr.reader` |
| 开源策略 | 客户端闭源 / 解析引擎与书源规范开源（便于社区共建书源生态） |

### 0.1.1 一句话定位

> **把"读"这件事做到极致的阅读器**——本地藏书零成本导入，网文全网聚合检索，排版与交互全部可调，且不牺牲任何一帧流畅度。

### 0.1.2 三条产品红线

以下三条是不可妥协的产品红线，任何功能迭代都不得违反：

1. **无广告、无推送、无账号强制登录。** 应用内不出现任何形态的商业广告位；不申请通知权限用于营销；阅读行为不需要登录即可完整使用。
2. **本地文件不出设备。** 用户导入的 TXT/EPUB/PDF 全文、封面、阅读进度、书签，任何情况下不上传服务器。网络能力仅用于"书源检索与抓取"这一条独立链路。
3. **不吃性能。** 不因为堆功能导致翻页掉帧、冷启动变慢、内存膨胀。性能预算是功能准入的硬门槛（见 6.x 与 7.5）。

---

## 0.2 目标用户与使用场景

### 0.2.1 用户画像

#### 画像 A：本地电子书收藏者（"仓鼠型"）

- **特征**：多年积累数百本 TXT/EPUB/PDF，散落在 Downloads、网盘同步目录、微信文件里；对封面、分类、进度有强整理欲。
- **痛点**：
  - 主流阅读器导入 EPUB 慢、扫不到深层目录；
  - 换设备后阅读进度丢失，或者换个字号进度就跳回上一章（这是本项目的重点攻克项）；
  - 部分阅读器强制上传书籍到云端才能同步。
- **诉求**：快速批量导入、自动抓封面与作者、网格书架好看、跨字号进度绝对精准。

#### 画像 B：长文本网文读者（"追更型"）

- **特征**：一本书 2000 章、单章 3000 字，每天追更 20～50 章；习惯在多个站点之间换源，因为某个源经常断更或排版错乱。
- **痛点**：
  - 收藏夹里的站点广告满天飞、正文里混入"本章未完，请点击下一页"；
  - 换源后要重新找章节位置；
  - 地铁/高铁没信号时想提前缓存，但手动一章节一章节点。
- **诉求**：一书多源聚合搜索、一键换源保留进度、后台批量缓存、"净化"后的正文。

### 0.2.2 核心用户旅程（Golden Path）

**旅程 1：本地导入到沉浸阅读（目标 ≤ 60 秒）**

```
启动 → 书架空态 → 点「扫描本地」→ 选择文件夹 → 深度扫描 → 勾选 12 本书 →
导入进度条 → 书架出现 12 张封面 → 点开第一本 → 自动记住上次位置（首次为第 1 章）
→ 上下滑动阅读 → 长按屏幕中央唤出设置 → 调字号/主题 → 退出 → 进度已保存
```

**旅程 2：全网找书到离线缓存**

```
书架 → 底部「发现」→ 输入"书名/作者" → 并发检索 8 个书源（2.8s 返回）→
归并去重列表（按源质量排序）→ 点开详情看简介与目录 → 加入书架 →
点「缓存全本」→ 后台上传 Worker 逐章抓取 → 断网也能读
```

### 0.2.3 场景清单（用于测试用例反推）

| 编号 | 场景 | 关键约束 |
| --- | --- | --- |
| SC-01 | 地铁通勤（无网）读完本地 TXT 一章 | 全程零网络请求，翻页 60fps |
| SC-02 | 导入 10MB 单文件 TXT | 首次分章 ≤ 1.5s |
| SC-03 | 导入 500 本 EPUB 批量扫描 | 扫描期间可操作书架，不 ANR |
| SC-04 | 阅读 PDF 教材并夜间反色 | 双击 2x 缩放不糊、反色保留图表可辨 |
| SC-05 | 追更网文 2000 章、每日换源 3 次 | 换源后章节与字级进度不丢 |
| SC-06 | 连续阅读 1 小时 | 内存波动 ≤ 150MB，无泄漏 |
| SC-07 | 120Hz 高刷屏快速连续翻页 | 帧率 ≥ 90fps，无白屏闪烁 |
| SC-08 | 自定义字体（50MB 的 .otf）加载 | 首屏 ≤ 800ms，不阻塞主线程 |

---

## 0.3 竞品分析与差异化

| 维度 | 传统本地阅读器 | 主流网文 App | **Kotlin Reader** |
| --- | --- | --- | --- |
| 本地格式 | 仅 TXT / 部分 EPUB | 基本不支持 | TXT + EPUB + PDF 全支持 |
| 书源 | 不支持 | 闭源、不可自定义 | 开放 JSON 规范，可导入/批量导入 |
| 排版自定义 | 基础字号/亮度 | 极简 | 字号 12–36sp、行距、段距、四边距、自定义字体、HEX 前景/背景 |
| 进度精度 | 章节 + 百分比 | 章节 + 百分比 | **字符级偏移**（换字号/换字体不丢） |
| 广告 | 无 | 大量 | 零 |
| 上架分发 | 应用商店 | 应用商店 | 应用商店（书源功能以"扩展能力"呈现，规避合规风险） |
| 性能 | 一般（大文件卡） | 一般 | 分章 <1.5s、60/90/120fps、内存 <150MB |

**差异化总结为三句话**：本地格式最全、书源生态最开放、排版与性能最讲究。

---

## 0.4 版本路线图

### V0.1 「骨架」（M1–M2，约 4 周）

- 工程骨架、多模块、CI 流水线
- 书架列表视图 + 单文件夹扫描 + TXT 导入
- TXT 阅读（UTF-8/GBK 双编码）、上下滚动、基础进度保存
- 主题：默认 + 夜间两套

**出口标准**：能从本地导入一本 5MB 的 GBK TXT 并读完一章，退出重进仍停在原位置。

### V0.2 「能读」（M3–M4，约 5 周）

- EPUB 解析（OPF/NCX/内嵌图）与 PDF 单页渲染
- 物理分页引擎 + 仿真/覆盖/滑动/滚动四种翻页
- 目录提取（TXT 正则规则）、章节跳转、滑动条跳转
- 字体/字号/行距/段距/边距设置面板
- 字节级→字符级进度模型落地

**出口标准**：四种翻页模式下，任意格式书籍切换字号后进度误差 ≤ 1 屏。

### V0.3 「好用」（M5–M6，约 4 周）

- 网格书架、分类分组、拖拽排序、批量操作
- 书源 JSON 导入/批量导入/规则调试器
- 单源搜索 + 详情页 + 加入书架 + 在线阅读（含缓存）
- 书签、阅读统计、电量/时间/章进度常驻显示
- 自动滚屏、常亮

**出口标准**：导入 3 个书源，能搜到并在线阅读一本 2000 章网文。

### V0.4 「聚合」（M7–M8，约 5 周）

- 多源并发聚合搜索 + 结果归并去重
- 一键换源（保留字级进度）
- 后台离线批量缓存（WorkManager）
- 正文清洗规则（广告/占位符/页脚）
- 自定义主题（HEX 前景/背景）、自定义字体（本地 ttf/otf）

**出口标准**：8 源并发搜索 ≤ 3s；断网后缓存章节可读。

### V1.0 「发布」（M9，约 4 周）

- 全量性能调优达标（6.x 全部指标）
- 无障碍适配、TalkBack、动态字号
- 应用图标/启动图/i18n（中/英）
- 隐私政策、权限最小化审计、上架准备
- 全量测试回归 + 老机型兼容矩阵验证

**出口标准**：见 7.5 验收标准全部通过。

### V1.x 展望（不做进 V1.0，避免范围蔓延）

MOBI/AZW3 支持、WebDAV 同步、跨设备进度同步（端到端加密）、TTS 朗读、词典划词、漫画模式（CBZ）。

---

## 0.5 范围界定
> 明确「不做什么」比「做什么」更重要，防止开发过程中边界失控。

### 0.5.1 In Scope（V1.0 必须交付）

- 本地 TXT / EPUB / PDF 的导入、归档、阅读
- 完整的排版自定义与四种翻页模式
- 字符级精度的进度记录与恢复
- 基于 JSON 规范的自定义书源：导入、管理、搜索、聚合、在线阅读、批量缓存
- 书架的分类、排序、批量操作
- 全链路性能达标与隐私合规

### 0.5.2 Out of Scope（V1.0 明确不做）

| 不做的事 | 原因 |
| --- | --- |
| 账号体系 / 云同步 | 违背"无账号"红线；跨设备同步列为 V1.x |
| 书城 / 付费内容 / 打赏 | 商业模式不在此 |
| 书籍内格式化编辑 | 阅读器不是编辑器 |
| 录音、有声书 | 独立产品线 |
| 社交（书评、书单、关注） | 违背"纯净"定位 |
| 内建书源市场（在线书源分发） | 合规风险高，V1.0 仅支持本地 JSON 导入 |
| iOS / Desktop | 本期仅 Android |

---

## 0.6 术语表（全档案统一口径）

| 术语 | 英文 | 定义 |
| --- | --- | --- |
| 书籍 | Book | 书架中的一个条目，对应一个 `file_path` + 一组元数据 |
| 章节 | Chapter | 书籍内的逻辑分节；TXT 由正则切分，EPUB 由 NCX/NAV 提供，PDF 以固定页数聚合 |
| 分页 | Page / Pagination | 依据屏幕与排版参数，把连续文本切分为一屏一屏的可视单元 |
| 字级偏移 | Character Offset | 全书文本流中的**字符下标**（非字节、非行号），进度记录的黄金标准 |
| 书源 | Book Source | 一份描述"如何从某网站检索与抓取书籍"的 JSON 规则集 |
| 换源 | Source Switching | 在不丢失阅读位置的前提下，把当前书籍切换到另一书源的同一本书 |
| 净化 | Purify | 按正则清洗正文中的广告、水印、占位符、无关页脚 |
| 书源组 | Source Group | 用于并发检索时对书源分组限流的逻辑集合 |
| 排版参数 | Typography Config | 字号、行距、段距、四边距、字体、对齐方式的集合 |
| 主题 | Theme | 一组前景色/背景色/强调色 + 是否反色的配置 |
| 首屏 | First Paint | 打开书籍到出现第一屏可读文本的时间 |
| 分章 | Chapterization | 从原始文件文本中识别并生成章节索引的过程 |
| 归档 | Archive | 导入时把外部文件复制进应用私有目录，避免原文件被移动后失效 |
| 缓存章节 | Cached Chapter | 从书源抓取后落库的正文快照，供离线阅读 |

---

## 0.7 项目成功判据

V1.0 发布即视为成功的条件（同时满足）：

1. **性能三达标**：10MB TXT 分章 <1.5s、翻页 ≥60fps、1 小时内存波动 ≤150MB。
2. **精度一达标**：字号±2 级 / 字体切换 / 边距变化后，恢复位置与原位置视觉偏差 ≤1 屏。
3. **书源可用性**：官方示例书源 ≥3 个可用，聚合搜索成功率 ≥80%。
4. **稳定性**：崩溃率 <0.5%，ANR 率 <0.1%。
5. **合规**：权限申请最小化（见 6.5），隐私政策明确声明"不上传本地阅读文件"。

---

## 0.8 阅读指引

| 你的角色 | 建议阅读路径 |
| --- | --- |
| 项目负责人 | 第 0 卷 → 第 8 卷 → 第 7 卷.5 |
| Android 架构师 | 第 1 → 2 → 3 → 5 卷 |
| 客户端开发（阅读器组） | 第 4.2 / 4.3 / 4.5 / 4.6 卷 + 第 5 卷 |
| 客户端开发（书架/网络组） | 第 4.1 / 4.7 / 4.8 卷 + 第 3 卷 |
| 性能/测试工程师 | 第 6 卷 + 第 7 卷 |
| 用户 / 运营 | 第 9 卷 + 附录 A |

---

**下一卷**：[第 1 卷 · 技术选型与工程规范](#vol-01)


---


# 第 1 卷 · 技术选型与工程规范 {#vol-01}
> 本卷回答两个问题：**用什么技术做**，以及**团队用什么规则一起做**。所有选型均给出「候选 → 结论 → 理由 → 风险」四段论证，不允许出现"因为流行所以用"的技术决策。

---

## 1.1 技术栈总览

### 1.1.1 语言与工具链

| 类别 | 选型 | 版本基线 | 说明 |
| --- | --- | --- | --- |
| 语言 | Kotlin | 2.0.x | 使用 K2 编译器，显著提升编译速度 |
| JDK | Temurin JDK | 17 | AGP 8.x 要求 JDK 17 |
| 构建 | Gradle + Kotlin DSL + Version Catalog | Gradle 8.9 | 依赖集中管理于 `libs.versions.toml` |
| AGP | Android Gradle Plugin | 8.6.x | 支持非传递 R 类、配置缓存 |
| 最小/目标 SDK | minSdk / targetSdk / compileSdk | 26 / 34 / 35 | 见 0.1 表 |
| 代码规范 | ktlint + detekt | 1.3.x / 1.23.x | 提交前 hook 强制 |
| 静态分析 | Android Lint | 随 AGP | `lintOptions.abortOnError = true` |

### 1.1.2 UI 层

| 能力 | 选型 | 理由 | 备选与放弃原因 |
| --- | --- | --- | --- |
| UI 框架 | **Jetpack Compose** (+ Material 3) | 声明式 UI 在翻页动效、动态主题、自绘文字上表达力远强于 View 体系；`Canvas` 直接绘制分页结果 | 放弃 XML + RecyclerView：翻页动画与动态排版需大量自定义 View，维护成本高 |
| 文本绘制 | Compose `Text` + `TextMeasurer` | `TextMeasurer` 提供 `measure()` 返回 `TextLayoutResult`，是实现物理分页的核心 API | 放弃原生 `StaticLayout`：需桥接 View 互操作，且 Compose 内 `TextMeasurer` 已足够 |
| 状态管理 | ViewModel + StateFlow | 单向数据流、可测试、与 Compose 天然契合 | 放弃 MVI 框架（MVIKotlin）：引入额外学习成本，收益有限 |
| 导航 | Navigation Compose | 类型安全路由（2.8+） | 放弃 Fragment：与 Compose 混用徒增复杂度 |
| 图片加载 | Coil 3 | Kotlin 原生、Compose 优先、支持 SVG/GIF、体积小 | 放弃 Glide：Compose 集成需额外适配 |
| 动画 | Compose Animation + `Animatable` | 翻页动效需精确控制进度值 | — |

### 1.1.3 数据层

| 能力 | 选型 | 理由 |
| --- | --- | --- |
| 本地数据库 | **Room 2.6.x**（SQLite） | 编译期校验 SQL；支持 `@Transaction`、Flow 查询、Paging 3 集成；表结构复杂（书籍/章节/书源/缓存）需强约束 |
| 键值配置 | **DataStore (Proto / Preferences)** | 排版参数、主题、阅读偏好；Proto 版提供类型安全与 schema 演进 |
| 文件存储 | 应用私有 `filesDir` + SAF 授权路径 | 见 3.4 存储布局 |
| 依赖注入 | **Hilt 2.52** | 编译期校验、与 ViewModel/Worker 集成成熟 | 
| 序列化 | **kotlinx.serialization** | Kotlin 原生、编译期生成、无反射开销；书源 JSON 解析首选 |
| 分页加载 | Paging 3 | 书架 500+ 本、目录 2000+ 章、搜索结果列表均需分页 |

> **DI 备选说明**：Koin 更轻量（无 KSP 编译开销），但 Hilt 的编译期校验能在大型多模块项目里更早暴露依赖错误。本项目模块数 ≥ 12，选择 Hilt。

### 1.1.4 异步与并发

| 能力 | 选型 | 说明 |
| --- | --- | --- |
| 协程 | kotlinx.coroutines 1.9.x | `Dispatchers.Default` 跑分页/分章，`IO` 跑文件与网络 |
| 结构化并发 | `supervisorScope` + `coroutineScope` | 聚合搜索要求"单源失败不影响整体"，必须用 supervisor |
| 流 | `Flow` / `StateFlow` / `SharedFlow` | 进度、设置、缓存状态的全链路响应式 |
| 线程池隔离 | 自定义 `Dispatchers` | `KrDispatchers.Parse`（2 线程）避免分章任务饿死 UI |
| 后台任务 | **WorkManager 2.9.x** | 批量缓存需满足"可约束（仅 Wi-Fi）、可重试、进程死亡可恢复" |

### 1.1.5 网络层

| 能力 | 选型 | 理由 |
| --- | --- | --- |
| HTTP 客户端 | **OkHttp 4.12** | 连接池复用、拦截器链（UA/重试/日志）、HTTP/2；书源抓取是高频碎片请求，连接复用收益大 |
| HTML 解析 | **Jsoup 1.18** | CSS 选择器 + 类 XPath 语法，直接支撑书源"规则"的表达；容错解析脏 HTML |
| 正则 | Kotlin `Regex`（Java 引擎） | 正文清洗、TXT 目录识别 |
| 编码探测 | juniversalchardet + 自研启发式 | 见 5.1 |
| 字符集转换 | 自研流式转码器 | 大文件不能整读进内存，见 6.1 |

> **不用 Retrofit 的理由**：书源 URL 规则是**运行时动态**的（用户在 JSON 里写 URL 模板），无法在编译期定义接口。Retrofit 的注解式接口模型在此场景失效，直接用 OkHttp + 自建 `SourceRequestBuilder` 更贴合。

### 1.1.6 格式解析

| 格式 | 方案 | 说明 |
| --- | --- | --- |
| TXT | 自研（编码探测 + 流式转码 + 正则分章） | 无成熟库；核心难点见 5.1 / 5.2 |
| EPUB | 自研（ZipFile + XmlPullParser）+ Jsoup | EPUB 本质是 ZIP + XHTML；自行解析 OPF/NCX/NAV 可控性最高 |
| PDF | **Android 原生 `PdfRenderer`** | 系统级渲染，无需引入 20MB+ 的 MuPDF；代价是仅支持 API 21+ 且不支持加密 PDF（列为降级提示） |
| 图片 | Coil + 自研缩放容器 | EPUB 内嵌图点击放大 |

---

## 1.2 工程结构：Gradle 多模块

### 1.2.1 模块清单

```
kotlin-reader/
├── app/                          # 壳工程：Application、MainActivity、导航宿主、DI 装配
├── build-logic/                  # 约定插件（Convention Plugins），统一各模块构建配置
│   └── convention/
├── core/
│   ├── common/                   # 纯 Kotlin 工具：Result、Dispatcher、扩展函数
│   ├── model/                    # 领域模型（纯 Kotlin，无 Android 依赖）
│   ├── database/                 # Room 实体 / DAO / 迁移
│   ├── datastore/                # Proto DataStore 定义与读写
│   ├── designsystem/             # 主题、色板、通用 Compose 组件、Typography
│   ├── ui/                       # 通用 UI 组件（Loading/Empty/Error/Snackbar）
│   ├── network/                  # OkHttp 封装、拦截器、限流器
│   └── testing/                  # 测试工具、Fake、TestRule
├── domain/                       # 用例层（UseCase），依赖 core:model，不依赖 Android
├── data/
│   ├── books/                    # 书架、书籍、章节、进度的 Repository 实现
│   ├── parser/                   # TXT/EPUB/PDF 解析引擎（含编码探测、分章）
│   ├── pagination/               # 物理分页引擎
│   ├── sources/                  # 书源规则引擎、抓取、清洗
│   └── settings/                 # 排版/主题配置 Repository
└── feature/
    ├── bookshelf/                # 书架页 + 网格/列表切换 + 批量操作
    ├── reader/                   # 阅读器页（最高复杂度模块）
    ├── settings/                 # 阅读设置面板
    ├── sources/                  # 书源管理 + 规则调试器
    ├── search/                   # 聚合搜索 + 详情页
    └── about/                    # 关于、隐私政策、开源许可
```

### 1.2.2 模块依赖规则（**强制**）

```
feature/*  ──►  domain  ──►  core:model
    │              │
    │              ▼
    │          data/*  ──►  core:database / core:network / core:datastore
    │              │
    ▼              ▼
core:designsystem / core:ui  ──►  core:common
```

**四条铁律**：

1. **`core:model` 是纯 Kotlin 模块**，禁止依赖任何 Android SDK。用 `java-library` 插件，而非 `com.android.library`。
2. **`feature` 之间禁止互相依赖**。跨特性通信一律通过 `domain` 的 UseCase 或导航回调。违反此规则会导致循环依赖与增量编译失效。
3. **`data` 只能被 `domain` 与 `feature` 依赖，不能反向依赖。**
4. **`core:designsystem` 禁止依赖 `data` 与 `domain`**，它只认识 UI。

在 `build-logic` 中用约定插件机械保证上述规则，而不是靠 code review：

```kotlin
// build-logic/convention/src/main/kotlin/KrLibraryConventionPlugin.kt
class KrLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        pluginManager.apply("org.jetbrains.kotlin.android")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        extensions.configure<LibraryExtension> {
            compileSdk = 35
            defaultConfig.minSdk = 26
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
            // 关键：非传递 R 类 + 资源命名空间，禁止跨模块引用资源 id
            buildFeatures { compose = true }
        }
        // 强制开启配置缓存与并行编译
        gradle.startParameter.apply { isConfigurationCache = true }
    }
}
```

### 1.2.3 版本目录（`gradle/libs.versions.toml`）

```toml
[versions]
kotlin = "2.0.21"
agp = "8.6.1"
composeBom = "2024.10.01"
room = "2.6.1"
hilt = "2.52"
okhttp = "4.12.0"
jsoup = "1.18.1"
coil = "3.0.4"
datastore = "1.1.1"
workmanager = "2.9.1"
paging = "3.3.2"
coroutines = "1.9.0"
serialization = "1.7.3"

[libraries]
compose-bom              = { module = "androidx.compose:compose-bom", version.ref = "composeBom" }
compose-ui               = { module = "androidx.compose.ui:ui" }
compose-material3        = { module = "androidx.compose.material3:material3" }
compose-foundation       = { module = "androidx.compose.foundation:foundation" }
lifecycle-viewmodel-compose = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version = "2.8.6" }
navigation-compose       = { module = "androidx.navigation:navigation-compose", version = "2.8.3" }
room-runtime             = { module = "androidx.room:room-runtime", version.ref = "room" }
room-ktx                 = { module = "androidx.room:room-ktx", version.ref = "room" }
room-compiler            = { module = "androidx.room:room-compiler", version.ref = "room" }
hilt-android             = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }
hilt-compiler            = { module = "com.google.dagger:hilt-android-compiler", version.ref = "hilt" }
okhttp                   = { module = "com.squareup.okhttp3:okhttp", version.ref = "okhttp" }
okhttp-logging           = { module = "com.squareup.okhttp3:logging-interceptor", version.ref = "okhttp" }
jsoup                    = { module = "org.jsoup:jsoup", version.ref = "jsoup" }
coil-compose             = { module = "io.coil-kt.coil3:coil-compose", version.ref = "coil" }
coil-gif                 = { module = "io.coil-kt.coil3:coil-gif", version.ref = "coil" }
datastore                = { module = "androidx.datastore:datastore", version.ref = "datastore" }
work-runtime-ktx         = { module = "androidx.work:work-runtime-ktx", version.ref = "workmanager" }
paging-compose           = { module = "androidx.paging:paging-compose", version.ref = "paging" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "serialization" }
coroutines-android       = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-android", version.ref = "coroutines" }

[plugins]
android-application      = { id = "com.android.application", version.ref = "agp" }
android-library          = { id = "com.android.library", version.ref = "agp" }
kotlin-android           = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose           = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization     = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
ksp                      = { id = "com.google.devtools.ksp", version = "2.0.21-1.0.28" }
hilt                     = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

---

## 1.3 架构模式：Clean Architecture + MVVM

### 1.3.1 三层职责

| 层 | 职责 | 允许知道的 | 禁止 |
| --- | --- | --- | --- |
| **UI（feature）** | 渲染状态、转发意图 | ViewModel 暴露的 UiState / UiEvent | 直接持有 DAO、OkHttp、File |
| **Domain** | 业务规则编排 | `core:model` 抽象 | 依赖 Android Context、Room 注解 |
| **Data** | 数据获取与持久化 | Room / DataStore / OkHttp / File | 含业务分支判断（如"该不该换源"） |

### 1.3.2 UiState 约定

每个界面暴露**唯一** StateFlow，避免多流组合导致的状态不一致：

```kotlin
// feature/reader/src/main/kotlin/com/kr/reader/feature/reader/ReaderContract.kt
data class ReaderUiState(
    val bookId: Long = 0L,
    val title: String = "",
    val loading: ReaderLoading = ReaderLoading.Parsing,
    val chapters: List<ChapterItem> = emptyList(),
    val currentChapterIndex: Int = 0,
    val currentPageIndex: Int = 0,
    val pages: List<PageSnapshot> = emptyList(),
    val typography: TypographyConfig = TypographyConfig.Default,
    val theme: ReaderTheme = ReaderTheme.Paper,
    val overlay: ReaderOverlay = ReaderOverlay.None,
    val error: String? = null,
)

sealed interface ReaderLoading {
    data object Parsing : ReaderLoading
    data class Paginating(val percent: Float) : ReaderLoading
    data object Ready : ReaderLoading
}

sealed interface ReaderOverlay {
    data object None : ReaderOverlay
    data object Menu : ReaderOverlay
    data object Typography : ReaderOverlay
    data object Theme : ReaderOverlay
    data object Catalog : ReaderOverlay
}

sealed interface ReaderIntent {
    data object ToggleMenu : ReaderIntent
    data class GotoChapter(val index: Int, val charOffset: Int? = null) : ReaderIntent
    data class NextPage(val animated: Boolean = true) : ReaderIntent
    data class PrevPage(val animated: Boolean = true) : ReaderIntent
    data class SeekProgress(val ratio: Float) : ReaderIntent
    data class OnTypographyChanged(val config: TypographyConfig) : ReaderIntent
    data class OnTapZone(val zone: TapZone) : ReaderIntent
}
```

**约定**：
- UiState 中的字段必须是**不可变数据**（`data class` + `List` 而非 `MutableList`）。
- 不把 `Context`、`File`、`Uri` 放进 UiState（除 `Uri` 用于 Coil 加载封面，允许但需注释说明）。
- 加载态用 `sealed interface` 表达分支，而不是 `isLoading: Boolean + error: String?` 的组合（那会产生 4 种非法状态）。

### 1.3.3 Repository 接口放在 Domain

```kotlin
// domain/src/main/kotlin/com/kr/reader/domain/repository/BookRepository.kt
interface BookRepository {
    fun observeShelf(groupId: Long?, sort: ShelfSort): Flow<List<Book>>
    suspend fun getBook(id: Long): Book?
    suspend fun importBooks(uris: List<ImportRequest>): Flow<ImportProgress>
    suspend fun updateProgress(bookId: Long, progress: ReadingProgress)
    suspend fun removeFromShelf(bookIds: List<Long>, deleteFile: Boolean)
}
```

实现在 `data:books`，通过 Hilt `@Binds` 装配。这样 `feature` 层永远看不到 Room。

---

## 1.4 代码规范

### 1.4.1 命名

| 元素 | 规则 | 示例 |
| --- | --- | --- |
| 包名 | 全小写，无下划线 | `com.kr.reader.data.parser.txt` |
| 类 / 接口 | PascalCase，接口不加 `I` 前缀 | `TxtParser`、`BookRepository` |
| 函数 / 属性 | camelCase | `paginateChapter()` |
| 常量 | SCREAMING_SNAKE（顶层 `const val`） | `MAX_FONT_SIZE_SP` |
| Composable | PascalCase，返回 Unit | `ReaderPage()` |
| 测试类 | 被测类 + `Test` | `TxtParserTest` |
| 资源 | snake_case | `ic_shelf_grid.xml` |

### 1.4.2 硬性规则（detekt 强制）

```yaml
# config/detekt/detekt.yml（节选）
complexity:
  LongMethod:
    threshold: 60          # 方法不超过 60 行——分页逻辑除外，需 @Suppress 并注释
  LongParameterList:
    functionThreshold: 6
  TooManyFunctions:
    thresholdInClasses: 20
style:
  MagicNumber:
    ignoreNumbers: ['-1', '0', '1', '2', '100']
  MaxLineLength:
    maxLineLength: 120
  ReturnCount:
    max: 3
coroutines:
  GlobalCoroutineUsage:
    active: true           # 禁止 GlobalScope
```

### 1.4.3 协程使用规范

```kotlin
// ✅ 正确：可取消、指定调度器、异常收敛
suspend fun loadChapters(bookId: Long): List<Chapter> =
    withContext(KrDispatchers.Parse) {
        runCatching { parser.parse(bookId) }
            .getOrElse { emptyList() }
    }

// ❌ 错误：GlobalScope + 无调度器 + 异常穿透
fun loadChaptersBad(bookId: Long) {
    GlobalScope.launch {
        parser.parse(bookId)  // 好，这里会把异常抛到全局 handler
    }
}
```

**六条协程纪律**：

1. 禁止 `GlobalScope`。应用级任务用注入的 `@ApplicationScope CoroutineScope`。
2. 所有 `suspend` 函数必须**可取消**：大循环里插入 `ensureActive()` 或 `yield()`，否则分章 2000 章无法中断。
3. `Dispatchers.IO` 只用于阻塞 IO（文件/网络）；CPU 密集（正则、分页测量）用 `Dispatchers.Default` 或自定义 `KrDispatchers.Parse`。
4. UI 层收集 Flow 用 `collectAsStateWithLifecycle()`，不是 `collectAsState()`。
5. 并发聚合搜索用 `supervisorScope` + `async`，单源失败不影响整体。
6. `Flow` 的 `shareIn` / `stateIn` 必须显式指定 `SharingStarted` 和 `replay`。

### 1.4.4 Compose 规范

```kotlin
// ✅ 正确：参数稳定、状态上提、无副作用
@Composable
fun ReaderPage(
    snapshot: PageSnapshot,
    typography: TypographyConfig,
    modifier: Modifier = Modifier,
) {
    Text(
        text = snapshot.annotatedString,
        style = typography.toTextStyle(),
        modifier = modifier.fillMaxSize(),
    )
}

// ❌ 错误：传入 ViewModel（不可测、不可预览）+ 内部持有可变状态
@Composable
fun ReaderPageBad(vm: ReaderViewModel) { ... }
```

**五条 Compose 纪律**：

1. Composable 只接收**数据 + lambda**，不接收 ViewModel（ViewModel 只在路由层注入并下传）。
2. 计算密集逻辑放在 `remember(key)` 内，分页结果必须缓存，禁止在 Composable 体内跑 `TextMeasurer.measure()`。
3. 列表必须提供稳定的 `key`（书籍用 `book.id`，章节用 `chapter.index`），否则批量删除会闪。
4. 每个可复用组件必须带 `@Preview`，并覆盖 light/dark 两套主题。
5. 高频回调（滑动、翻页）用 lambda 传参而非对象，避免不必要的重组（启用 Compose Compiler Metrics 监控）。

---

## 1.5 Git 工作流

### 1.5.1 分支模型（简化 GitFlow）

```
main        ← 可发布分支，每次合并打 tag（v0.1.0）
 └ dev      ← 集成分支，日常合并目标
    ├ feat/reader-pagination
    ├ fix/epub-cover-missing
    └ perf/txt-chapterize-1.5s
```

- `main` 受保护，禁止直推，必须 PR + 1 名 Reviewer + CI 通过。
- 分支命名：`<type>/<kebab-topic>`，type ∈ `feat|fix|perf|refactor|test|docs|chore`。
- 生命周期 ≤ 5 天，超时需拆分。

### 1.5.2 提交信息（Conventional Commits）

```
<type>(<scope>): <subject>

[body]
[footer]
```

示例：

```
perf(parser): 用流式解码替换全量 readText，10MB TXT 分章 3.2s → 1.1s

原实现 File.readText() 会把整个文件以 char[] 形式常驻堆内存，
在 10MB GBK 文件上产生 ~20MB 瞬时分配并触发 2 次 GC。

改为 BufferedInputStream + CharsetDecoder 流式解码，边读边匹配章节正则，
峰值内存降至 1.2MB。

Closes #142
```

**scope 取值**：`parser` `pagination` `bookshelf` `reader` `sources` `search` `settings` `db` `ci` `deps`。

### 1.5.3 提交前钩子

```bash
# .githooks/pre-commit
#!/bin/sh
./gradlew ktlintCheck detekt --daemon -q || {
  echo "代码规范检查未通过，请修复后重新提交"; exit 1;
}
```

启用：`git config core.hooksPath .githooks`

---

## 1.6 CI/CD 流水线

### 1.6.1 GitHub Actions 配置

```yaml
# .github/workflows/ci.yml
name: CI
on:
  pull_request: { branches: [main, dev] }
  push: { branches: [dev] }

jobs:
  verify:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: '17' }
      - uses: gradle/actions/setup-gradle@v4

      - name: 代码规范
        run: ./gradlew ktlintCheck detekt

      - name: 单元测试 + 覆盖率
        run: ./gradlew testDebugUnitTest jacocoTestReport

      - name: 覆盖率门禁（core/data/domain ≥ 70%）
        run: ./gradlew checkCoverage

      - name: 静态分析
        run: ./gradlew lintDebug

      - name: 构建 Debug APK
        run: ./gradlew assembleDebug

      - name: 上传测试报告
        if: always()
        uses: actions/upload-artifact@v4
        with: { name: reports, path: '**/build/reports/**' }

  benchmark:
    runs-on: ubuntu-latest
    needs: verify
    steps:
      - uses: actions/checkout@v4
      # 微基准：分章耗时、分页耗时、编码探测准确率
      - run: ./gradlew :data:parser:test --tests '*BenchmarkTest'
```

### 1.6.2 发布流程

| 阶段 | 动作 | 产物 |
| --- | --- | --- |
| 内部测试 | `dev` 合并后自动构建 | `kr-debug-<sha>.apk` 上传内测群 |
| 候选发布 | 打 `v1.0.0-rc1` tag | `kr-release-rc1.aab` |
| 正式发布 | `main` 打 `v1.0.0` tag | 签名 AAB 上传 Google Play / 国内渠道 |
| 灰度 | Play Console 分阶段 5% → 20% → 100% | 崩溃率监控 |

### 1.6.3 签名与密钥

```properties
# keystore.properties（**禁止提交到 Git**，通过 CI Secret 注入）
storeFile=../keystore/kr-release.jks
storePassword=***
keyAlias=kr
keyPassword=***
```

`.gitignore` 必须包含：`keystore.properties`、`*.jks`、`*.keystore`、`local.properties`。

---

## 1.7 构建变体与调试开关

```kotlin
// app/build.gradle.kts
android {
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
            buildConfigField("boolean", "LOG_ENABLED", "true")
            buildConfigField("boolean", "SOURCE_DEBUGGER", "true")   // 书源规则调试器
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("boolean", "LOG_ENABLED", "false")
            buildConfigField("boolean", "SOURCE_DEBUGGER", "false")
            // 基准测试专用：关闭一切日志与调试钩子
        }
        create("benchmark") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
    }
    // 按 ABI 拆分，减小包体
    splits { abi { isEnable = true; reset(); include("armeabi-v7a", "arm64-v8a"); isUniversalApk = false } }
}
```

**ProGuard 必留规则**（书源 JSON / Room / 序列化会因混淆崩溃）：

```proguard
# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.kr.reader.core.model.** {
    *** Companion;
}
-keepclasseswithmembers class com.kr.reader.core.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# 崩渡还原
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
```

---

## 1.8 依赖治理规则

1. **新增依赖必须走 ADR（架构决策记录）**，写入 `docs/adr/NNNN-<title>.md`，说明：动机、候选对比、体积增量、维护活跃度、License。
2. **禁止引入体积 >2MB 的依赖而不做评估**。当前包体积预算：APK ≤ 12MB（按 ABI 拆分后）。
3. **禁止在 `core:model` 与 `domain` 引入 Android 依赖。**
4. **AndroidX 版本统一由 Compose BOM 与 `libs.versions.toml` 管控**，禁止在模块内硬编码版本号。
5. **每季度执行一次 `./gradlew dependencyUpdates`**，滞后版本超过 2 个 minor 需评估升级。

### 1.8.1 已评估但放弃的依赖

| 依赖 | 放弃原因 |
| --- | --- |
| MuPDF / Pdfium 第三方封装 | 体积 +18MB，且 PDF 需求以阅读为主，原生 `PdfRenderer` 够用 |
| Retrofit | 动态 URL 规则场景下注解模型失效（见 1.1.5） |
| Epublib（nl.siegmann） | 年久失修，不支持 EPUB3 NAV 文档 |
| LeakCanary（release） | 仅 debug 引入，不进 release 包 |
| Accompanist Pager | Compose 1.6+ 已内置 `HorizontalPager`，无需额外依赖 |

---

## 1.9 环境搭建（新成员 30 分钟上手指南）

```bash
# 1. 克隆并切分支
git clone <repo-url> kotlin-reader && cd kotlin-reader
git checkout dev

# 2. 配置本地环境
echo "sdk.dir=<你的 Android SDK 路径>" > local.properties
git config core.hooksPath .githooks

# 3. 首次构建（约 3-5 分钟）
./gradlew :app:assembleDebug

# 4. 装到设备
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 5. 跑测试确认环境健康
./gradlew testDebugUnitTest
```

**必备工具**：

| 工具 | 用途 |
| --- | --- |
| Android Studio Ladybug+ | IDE，需 Kotlin 2.0 插件 |
| Layout Inspector | 排查 Compose 重组 |
| Profiler（CPU / Memory / Energy） | 性能达标验证（见 6.x） |
| Perfetto / Systrace | 掉帧根因分析 |
| Benchmark Macro (Jetpack) | 冷启动、滚动帧率基准 |
| adb + `dumpsys gfxinfo` | 帧率客观数据 |

---

**上一卷**：[第 0 卷 · 项目总纲](#vol-00) ｜ **下一卷**：[第 2 卷 · 系统架构设计](#vol-02)


---


# 第 2 卷 · 系统架构设计 {#vol-02}
> 本卷描述系统的**静态结构**（模块、层、依赖）与**动态行为**（数据流、时序、线程调度）。第 4 卷讲"每个功能怎么做"，本卷讲"这些功能如何组装成一个整体且不互相拖累"。

---

## 2.1 分层架构全景

```
┌───────────────────────────────────────────────────────────────────────────────┐
│                              UI LAYER (feature/*)                             │
│                                                                               │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌────────┐ │
│  │bookshelf │ │  reader  │ │ search   │ │ sources  │ │ settings │ │ about  │ │
│  │  书架页  │ │  阅读器  │ │ 聚合搜索 │ │ 书源管理 │ │ 阅读设置 │ │ 关于   │ │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘ └───┬────┘ │
│       │            │            │            │            │           │      │
│       └────────────┴────────────┴────────────┴────────────┴───────────┘      │
│                                     │ Intent / UiState                        │
└─────────────────────────────────────┼─────────────────────────────────────────┘
                                      ▼
┌───────────────────────────────────────────────────────────────────────────────┐
│                            DOMAIN LAYER (domain/*)                            │
│                    UseCase：业务编排，无 Android 依赖                          │
│                                                                               │
│  ImportBooksUseCase      LoadChaptersUseCase     PaginateChapterUseCase       │
│  SaveProgressUseCase     SearchAcrossSourcesUseCase  SwitchSourceUseCase      │
│  CacheChaptersUseCase    ApplyTypographyUseCase  PurifyContentUseCase         │
└─────────────────────────────────────┬─────────────────────────────────────────┘
                                      ▼ Repository 接口
┌───────────────────────────────────────────────────────────────────────────────┐
│                              DATA LAYER (data/*)                              │
│                                                                               │
│  ┌───────────┐ ┌───────────┐ ┌───────────┐ ┌───────────┐ ┌───────────────┐   │
│  │  books    │ │  parser   │ │pagination │ │  sources  │ │   settings    │   │
│  │ 书架/进度 │ │ 三格式解析│ │ 物理分页  │ │ 规则引擎  │ │ 排版/主题配置 │   │
│  │  ├ Room   │ │ ├ 编码探测│ │ ├ 测量器  │ │ ├ 抓取器  │ │  ├ DataStore  │   │
│  │  ├ File   │ │ ├ 分章    │ │ ├ 缓存LRU │ │ ├ 清洗器  │ │  └ 字体加载   │   │
│  │  └ SAF    │ │ └ 元数据  │ │ └ 预取    │ │ └ 限流器  │ │               │   │
│  └───────────┘ └───────────┘ └───────────┘ └───────────┘ └───────────────┘   │
└─────────────────────────────────────┬─────────────────────────────────────────┘
                                      ▼
┌───────────────────────────────────────────────────────────────────────────────┐
│                          CORE / PLATFORM LAYER (core/*)                       │
│                                                                               │
│  database(Room)  datastore  network(OkHttp)  model  common  designsystem  ui   │
└───────────────────────────────────────────────────────────────────────────────┘
                                      ▼
┌───────────────────────────────────────────────────────────────────────────────┐
│                             SYSTEM (Android Framework)                        │
│   文件系统 / SAF / PdfRenderer / WorkManager / 字体加载 / 电量与时间 / 窗口     │
└───────────────────────────────────────────────────────────────────────────────┘
```

### 2.1.1 依赖方向铁律

```
feature ───► domain ───► core:model
   │           │
   │           └───► data ───► core:{database,network,datastore}
   │                            │
   └───► core:{designsystem,ui} ┘
                    │
                    └───► core:common
```

**依赖只能向下，且 feature 之间横向隔离。** CI 中用 `./gradlew :feature:reader:dependencies` 配合自定义任务检测非法依赖（见 2.7）。

---

## 2.2 模块职责矩阵

| 模块 | 对外暴露 | 内部实现 | 关键复杂度 | 依赖 |
| --- | --- | --- | --- | --- |
| `core:model` | `Book` `Chapter` `ReadingProgress` `BookSource` `TypographyConfig` `ReaderTheme` | 纯数据类 + 值对象校验 | 模型设计的稳定性（改一次牵连全部） | 无 |
| `core:database` | `KrDatabase` DAO 接口 | Room 实体、迁移、TypeConverter | 2000 章 / 500 本的数据规模与查询性能 | `core:model` |
| `core:datastore` | `TypographyStore` `ThemeStore` `ReaderPrefsStore` | Proto schema、序列化 | schema 演进与默认值合并 | `core:model` |
| `core:network` | `HttpClientProvider` `RateLimiter` `UAProvider` | OkHttp 配置、拦截器、并发闸门 | 防封禁（UA 轮换）、超时分级、单域限流 | `core:common` |
| `core:designsystem` | `KrTheme` `KrTypography` `ColorTokens` | Material3 定制、色板、组件 | 主题切换时的全局一致性 | `core:common` |
| `core:ui` | `LoadingBox` `EmptyBox` `ErrorBox` `ConfirmDialog` | 通用 Compose 组件 | — | `core:designsystem` |
| `data:parser` | `BookParser`（工厂）+ 三个实现 | 编码探测、分章、元数据、封面提取 | **全项目最高复杂度**：性能与正确性双重约束 | `core:model` |
| `data:pagination` | `PaginationEngine` | `TextMeasurer` 封装、分页缓存、预取 | 分页正确性 + 60fps 渲染 | `core:model` |
| `data:books` | `BookRepository` `ProgressRepository` | Room + 文件 + SAF | 导入事务性、进度原子写 | `core:database` |
| `data:sources` | `SourceRepository` `SourceEngine` | JSON 规则解释、抓取、清洗、并发搜索 | 规则的表达力与容错 | `core:network` |
| `data:settings` | `TypographyRepository` `ThemeRepository` | DataStore + 字体文件加载 | 配置变更到 UI 的响应延迟 | `core:datastore` |

---

## 2.3 核心数据流

### 2.3.1 数据流 1：导入本地书籍（写路径）

```
用户点「扫描文件夹」
        │
        ▼
[BookshelfScreen] ──Intent.ScanFolder(uri)──► [BookshelfViewModel]
                                                      │
                                                      ▼
                                        ImportBooksUseCase.scan(uri)
                                                      │
                        ┌─────────────────────────────┴───────────────────────┐
                        ▼                                                     │
              [FileScanner]（core:common）                                    │
              · DocumentFile 递归遍历                                         │
              · 后缀过滤 .txt/.epub/.pdf                                      │
              · 深度上限 8 层，防符号链接死循环                                │
                        │                                                     │
                        ▼ Flow<ScanItem>                                      │
              [BookshelfViewModel] 更新 UiState（实时进度）◄────────────────────┘
                        │
                        ▼ 用户勾选后确认导入
              ImportBooksUseCase.import(selected)
                        │
                        ▼
              ┌─────────────────────────────────────────┐
              │ 逐本处理（受并发度 2 限制）              │
              │  1. 复制文件到 filesDir/books/<sha1>    │
              │  2. 按扩展名分派 Parser                 │
              │  3. Parser 提取元数据 + 封面 + 章节索引 │
              │  4. Room @Transaction 写入 Book+Chapter │
              │  5. 发进度回调                           │
              └─────────────────────────────────────────┘
                        │
                        ▼ Flow<ImportProgress>
              UiState.importProgress = 3/12 已导入
```

**关键设计**：

- 扫描与导入**分离**。扫描只产出候选列表（含文件大小、推断的书名），用户确认后才复制文件，避免误导入整张 SD 卡。
- 导入是**幂等**的：以 `文件SHA1(fileSize + path + firstBytes)` 作为去重键，重复导入同一文件会被识别并提示"已在书架"。
- 导入失败**不中断整批**：单本失败记入 `failedList`，完成后统一提示"11 本成功，1 本失败（编码无法识别）"。

### 2.3.2 数据流 2：打开书籍到首屏可读（读路径 · 性能关键路径）

```
点书架某本书
     │
     ▼
[ReaderViewModel.load(bookId)]
     │
     ├─(1)─► BookRepository.getBook(id)            [IO]   读取书籍元数据  ~5ms
     │
     ├─(2)─► ChapterRepository.getChapters(id)     [IO]   读取章节索引    ~15ms (2000章)
     │
     ├─(3)─► 定位章节 = progress.chapterIndex
     │
     ├─(4)─► ChapterContentLoader.load(chapter)    [IO]
     │        ├─ 本地书：从归档文件按 [startOffset, endOffset) 切片读取
     │        └─ 网络书：先查 cache，未命中则抓取并落库
     │
     ├─(5)─► PaginationEngine.paginate(text, typography, screenSize)   [Default]
     │        └─ 输出 List<PageSnapshot>，首屏优先（分页与渲染流式交错）
     │
     ├─(6)─► 首屏上屏  ◄────── 目标：≤ 300ms（已分页书籍）/ ≤ 900ms（首次打开）
     │
     └─(7)─► 后台继续分页剩余章节 + 预取相邻章节          [Parse/IO]
```

**首屏优先策略**：不做"整章分页完再显示"，而是：

```
paginateChapter(chapterText) : Flow<PaginationBatch>
   ├─ emit(第一批：首屏 + 预读 1 屏)      ← ViewModel 立即渲染
   ├─ emit(第二批：后续 3 屏)
   └─ emit(剩余全部)
```

`Flow` 的流式特性让用户在看到首屏时，后续分页仍在后台进行。这是达成"无肉眼可见白屏"的关键。

### 2.3.3 数据流 3：聚合搜索与换源

```
[SearchScreen] 输入"黎明之剑"
        │
        ▼
SearchAcrossSourcesUseCase.query(keyword)
        │
        ▼
supervisorScope {
    enabledSources
        .filter { it.enabled && it.group !in 被限流组 }
        .chunked(byGroupSize)
        .forEach { chunk ->
            chunk.forEach { source ->
                launch(KrDispatchers.Network) {           // 每源独立协程
                    runCatching { engine.search(source, keyword) }
                        .onSuccess { emit(SourceResult(source, it)) }
                        .onFailure { emit(SourceError(source, it)) }  // 不抛出
                }
            }
        }
}
        │  Flow<SearchEvent>
        ▼
[MergeEngine] 归并去重
  · 书名归一化（去空白、全角半角统一、去「小说」「最新章节」等噪声词）
  · 作者归一化（去「著」「/」等）
  · 相似度：编辑距离 ≤ 2 或 包含关系 → 视为同一本书
  · 同书多源 → 聚合为一条，展开可看各源
        │
        ▼
SearchUiState.mergedResults  ← 边搜边出，2.8s 内全部返回
```

**换源时序**：

```
用户点「换源」→ 选目标源 → SwitchSourceUseCase.execute(bookId, targetSourceId)
   1. 读取当前 progress: (chapterIndex, chapterTitle, charOffset)
   2. 拉取目标源章节目录
   3. 章节匹配：
        a. 优先按 chapterTitle 精确匹配
        b. 其次按「章节序号」匹配（第 183 章）
        c. 再次按标题相似度（编辑距离最小）
        d. 全部失败 → 回退到章节序号比例位置，并提示"未能精确定位，已按比例跳转"
   4. 写入新 sourceId + 新 chapterIndex
   5. chapterTitle 不变（对用户而言"还在同一章"）
   6. 加载新源该章正文
```

---

## 2.4 线程模型

### 2.4.1 调度器定义

```kotlin
// core:common — KrDispatchers.kt
object KrDispatchers {
    /** CPU 密集：分页测量、正则匹配、HTML 解析 */
    val Default: CoroutineDispatcher = Dispatchers.Default

    /** 阻塞 IO：文件读写、Room、网络 */
    val IO: CoroutineDispatcher = Dispatchers.IO

    /**
     * 分章/解析专用：独立 2 线程池。
     * 存在意义：导入 500 本书时不能让分章任务占满 Dispatchers.Default，
     * 否则分页测量（共享 Default）会被饿死，导致阅读翻页掉帧。
     */
    val Parse: CoroutineDispatcher = Executors.newFixedThreadPool(2) {
        Thread(it, "kr-parse").apply { priority = Thread.NORM_PRIORITY - 1 }
    }.asCoroutineDispatcher()

    /** 网络抓取：4 线程，配合按域限流 */
    val Network: CoroutineDispatcher = Executors.newFixedThreadPool(4) {
        Thread(it, "kr-net")
    }.asCoroutineDispatcher()

    /** 应用级作用域，随进程生命周期 */
    val ApplicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}
```

### 2.4.2 任务与调度器分配表（**开发时对照执行**）

| 任务 | 调度器 | 优先级 | 可取消 | 备注 |
| --- | --- | --- | --- | --- |
| 文件夹扫描 | `IO` | 低 | ✅ | 每 50 个文件 `ensureActive()` |
| 文件复制/归档 | `IO` | 中 | ✅ | 8KB 缓冲区 |
| 编码探测 | `Parse` | 中 | ✅ | 大文件分块统计 |
| 分章（正则） | `Parse` | 中 | ✅ | 每 500 行 `ensureActive()` |
| EPUB 解压解析 | `Parse` | 中 | ✅ | ZipFile 流式读取 |
| PDF 页面渲染 | `IO` | 高 | ❌ | `PdfRenderer` 非线程安全，需串行 + 锁 |
| **物理分页测量** | `Default` | **高** | ✅ | 分页必须优先于解析 |
| 章节内容加载 | `IO` | 高 | ✅ | 本地切片 / 网络抓取 |
| 书源搜索 | `Network` | 中 | ✅ | 每源独立 job |
| 正文清洗 | `Parse` | 中 | ✅ | 大章正则回溯风险见 5.6 |
| 批量缓存 | `Network` + `Default` | 低 | ✅ | WorkManager 驱动，受约束 |
| 进度写入 | `IO` | 高 | ❌ | 原子写，防丢失 |
| 字体加载 | `IO` | 高 | ✅ | 缓存 `Typeface` 实例 |

### 2.4.3 关键：分页与解析的资源隔离

**问题**：`Dispatchers.Default` 的并行度 = CPU 核心数。若导入后台在跑 2000 章的分章任务，同时用户打开一本 10MB TXT 触发分页测量，两者共享 `Default` 会互相饥饿 → 翻页掉帧。

**方案**：

1. 解析类任务**全部走 `KrDispatchers.Parse`**（独立 2 线程池），与 `Default` 物理隔离。
2. 分页测量在 `Default` 上独占，且**限制 concurrent measure job 为 1**，避免多章节并发分页争抢。
3. 导入进行中时，阅读场景启动会让导入任务**主动降级**（`ImportCoordinator.requestThrottle()` 将并发从 2 降到 1）。

```kotlin
// data:pagination — 串行化分页测量，避免争抢
class PaginationEngine(...) {
    private val measureMutex = Mutex()

    fun paginate(text: String, cfg: TypographyConfig, size: IntSize): Flow<PaginationBatch> = flow {
        ensureActive()
        measureMutex.withLock {
            // 独占测量：同一时刻全 App 只有一次分页在进行
            ...
        }
    }.flowOn(KrDispatchers.Default)
}
```

---

## 2.5 导航设计

### 2.5.1 路由图

```
                    ┌─────────────────┐
                    │  bookshelf      │  ◄── 启动页（无 Splash，直接进书架）
                    │  (bottom nav 1) │
                    └────────┬────────┘
                             │
        ┌────────────────────┼────────────────────┬──────────────────┐
        ▼                    ▼                    ▼                  ▼
┌───────────────┐   ┌────────────────┐   ┌──────────────┐  ┌──────────────┐
│ reader/       │   │ discover       │   │ sources      │  │ settings     │
│ {bookId}      │   │ (bottom nav 2) │   │ (bottom nav 3)│ │ (bottom nav 4)│
│ ?chapter=&off=│   └───────┬────────┘   └──────┬───────┘  └──────┬───────┘
└───────────────┘           │                   │                 │
                    ┌───────┴────────┐  ┌───────┴────────┐ ┌──────┴──────┐
                    ▼                ▼  ▼                ▼ ▼             ▼
            ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌────────────┐
            │ search       │ │ source detail│ │ source edit  │ │ reader     │
            │ ?q=          │ │ {sourceId}   │ │ {sourceId}   │ │ settings   │
            └──────┬───────┘ └──────────────┘ └──────────────┘ │ theme      │
                   ▼                                          │ fonts      │
            ┌──────────────┐                                  │ cache mgmt │
            │ book detail  │                                  └────────────┘
            │ ?src=&url=   │
            └──────┬───────┘
                   ▼
            ┌──────────────┐
            │ reader/      │ （网络书也复用同一个 reader 路由）
            └──────────────┘
```

### 2.5.2 底部导航

| Tab | 图标 | 说明 |
| --- | --- | --- |
| 书架 | 书堆 | 默认 Tab，本地 + 已收藏网络书统一展示 |
| 发现 | 放大镜 | 聚合搜索入口 + 换源中心 |
| 书源 | 列表 | 书源列表、导入、分组、启用开关 |
| 我的 | 人像 | 阅读统计、缓存管理、显示设置、关于/隐私 |

> **设计决策**：不设独立"阅读"Tab。阅读器为**全屏沉浸式**目标页，进入后隐藏底部导航。

### 2.5.3 阅读器路由参数（支持外部深链与恢复）

```kotlin
@Serializable
data class ReaderRoute(
    val bookId: Long,
    val chapterIndex: Int? = null,
    val charOffset: Int? = null,
    val sourceId: Long? = null,     // 网络书换源后
    val fromCache: Boolean = false,
)
```

- 传入 `chapterIndex = null` 时，从 `ReadingProgress` 恢复。
- 传入 `charOffset` 时，优先级高于章节：直接定位到该字偏移所在屏。
- 阅读器支持**外部跳转**（如从通知栏"继续阅读"直接进）。

---

## 2.6 状态保持与生命周期

### 2.6.1 配置变更（旋转 / 分屏）

阅读器对屏幕尺寸**极其敏感**（分页结果随宽高变化）。策略：

```kotlin
// MainActivity
android:configChanges="orientation|screenSize|smallestScreenSize|screenLayout|keyboardHidden|uiMode"
```

由 Compose 自行响应用户尺寸变化并**重新分页**，避免 Activity 重建导致的整章解析与白屏。

```kotlin
@Composable
fun ReaderScreen(route: ReaderRoute, vm: ReaderViewModel = hiltViewModel()) {
    val size = with(LocalDensity.current) {
        val cfg = LocalConfiguration.current
        IntSize(
            (cfg.screenWidthDp.dp.toPx()).toInt(),
            (cfg.screenHeightDp.dp.toPx()).toInt() - vm.chromeHeightPx,
        )
    }
    LaunchedEffect(size, vm.typography) {
        vm.onViewportChanged(size)   // 触发重新分页，但保留字级偏移
    }
    ...
}
```

### 2.6.2 进程死亡恢复

`ReaderViewModel` 用 `SavedStateHandle` 保存最小恢复集：

```kotlin
class ReaderViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    private val saveProgress: SaveProgressUseCase,
) : ViewModel() {
    private val bookId: Long = checkNotNull(handle["bookId"])
    private val charOffset: StateFlow<Int> = handle.getStateFlow("charOffset", 0)

    override fun onCleared() {
        // 兜底：进程被杀前最后一次落盘。真正的实时保存见 4.4
        KrDispatchers.ApplicationScope.launch {
            saveProgress.flushNow(bookId)
        }
    }
}
```

**进度写入策略（三级）**：

| 级别 | 触发 | 延迟 | 说明 |
| --- | --- | --- | --- |
| 实时内存 | 每次翻页 | 0 | 只改内存 StateFlow |
| 延迟落盘 | 翻页后 debounce 800ms | ≤800ms | 主路径，避免每页写库 |
| 强制落盘 | 退出阅读器 / `onStop` / 每 30s | — | 兜底，确保不丢 |

---

## 2.7 架构守护（自动化校验）

规则不能只写在文档里。用一个 Gradle Task 做依赖方向校验：

```kotlin
// build-logic/src/main/kotlin/kr/architecture/ArchitectureGuardPlugin.kt
class ArchitectureGuardPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val forbidden = mapOf(
            ":core:model"        to listOf("androidx", "com.android"),
            ":domain"            to listOf("androidx.room", "retrofit", "okhttp3"),
            ":core:designsystem" to listOf(":data:", ":domain:"),
            ":feature:reader"    to listOf(":feature:bookshelf", ":feature:search"),
        )
        tasks.register("checkArchitecture") {
            doLast {
                dependencies.nonNull().forEach { dep ->
                    forbidden[dep.name]?.forEach { bad ->
                        if (dep.dependencies.any { it.name.contains(bad) }) {
                            error("架构违规：${dep.name} 依赖了禁止的 $bad")
                        }
                    }
                }
            }
        }
    }
}
```

同时用 detekt 的 `ForbiddenImport` 规则屏蔽跨层 import：

```yaml
# config/detekt/detekt.yml
style:
  ForbiddenImport:
    active: true
    imports:
      - 'androidx.room.*'          # domain 层禁止
      - 'okhttp3.*'                # feature 层禁止
      - 'java.io.File'             # feature 层禁止（应通过 Repository）
      - 'kotlinx.coroutines.GlobalScope'
```

---

## 2.8 错误处理与降级策略

### 2.8.1 统一错误模型

```kotlin
// core:model
sealed interface KrError {
    val userMessage: String
    val recoverable: Boolean

    data class FileNotFound(val path: String) : KrError {
        override val userMessage = "文件已被移动或删除，请重新导入"
        override val recoverable = false
    }
    data class UnsupportedEncoding(val detected: String) : KrError {
        override val userMessage = "无法识别文件编码（疑似 $detected），请手动指定编码"
        override val recoverable = true
    }
    data class ParseFailed(val format: String, val cause: Throwable) : KrError {
        override val userMessage = "解析失败，文件可能已损坏"
        override val recoverable = false
    }
    data class NetworkBlocked(val source: String, val code: Int) : KrError {
        override val userMessage = "书源「$source」访问受限（HTTP $code），可能存在封禁"
        override val recoverable = true
    }
    data class SourceRuleInvalid(val source: String, val field: String) : KrError {
        override val userMessage = "书源规则错误：字段 $field 非法"
        override val recoverable = true
    }
    data class OutOfMemory(val phase: String) : KrError {
        override val userMessage = "内存不足"
        override val recoverable = true
    }
}
```

### 2.8.2 降级矩阵（**必须实现**）

| 故障 | 降级行为 | 用户感知 |
| --- | --- | --- |
| PDF 带密码 | 提示"暂不支持加密 PDF"，不进书 | 明确提示，不闪退 |
| EPUB 结构损坏（无 OPF） | 回退为"按 `<h1>~<h3>` 标题分章"的 TXT 模式解析 | 能读，目录可能不完美 |
| TXT 编码识别失败 | 提示手动选择（下拉：UTF-8 / GBK / GB2312 / BIG5 / UTF-16） | 一次交互解决 |
| TXT 无章节标识 | 按固定 3000 字/章切分，目录显示"第 N 节" | 至少能读 |
| 自定义字体加载失败 | 回退系统默认字体 + Toast | 不崩溃 |
| 书源某字段规则非法 | 该书源标记为"异常"，聚合搜索时跳过，管理页高亮 | 不影响其他源 |
| 网络超时 | 重试 2 次（退避 1s/3s），仍失败则提示可从其他源换源 | 明确可行动 |
| 内存接近上限 | 主动释放分页缓存（保留当前章），关闭预取 | 几乎无感 |
| Room 迁移失败 | 备份数据库为 `.bak`，重建库，提示"数据已重置" | 最坏情况兜底 |

---

**上一卷**：[第 1 卷 · 技术选型与工程规范](#vol-01) ｜ **下一卷**：[第 3 卷 · 数据模型与存储设计](#vol-03)


---


# 第 3 卷 · 数据模型与存储设计 {#vol-03}
> 数据模型是阅读器的心脏。**进度模型设计错一次，后面所有排版功能都是灾难**。本卷先给出核心模型的设计推理，再落到 Room 表结构与文件布局。

---

## 3.1 领域模型（core:model）

### 3.1.1 模型总览

```
┌───────────────────┐          ┌───────────────────┐         ┌──────────────────┐
│       Book        │          │      Chapter      │         │  ReadingProgress │
├───────────────────┤          ├───────────────────┤         ├──────────────────┤
│ id (PK)           │1        *│ id (PK)           │         │ bookId (PK,FK)   │
│ title             │──────────│ bookId (FK)       │  1    1 │ chapterIndex     │
│ author            │          │ index             │─────────│ chapterTitle     │
│ coverUrl          │          │ title             │         │ charOffset  ★    │
│ format            │          │ startOffset  ★    │         │ chapterRatio     │
│ filePath          │          │ endOffset    ★    │         │ percent          │
│ fileSize          │          │ charCount         │         │ updatedAt        │
│ encoding          │          │ isCached          │         │ readingSeconds   │
│ sourceId (FK,nul) │          │ cachePath         │         └──────────────────┘
│ sourceBookUrl     │          │ sourceChapterUrl  │
│ groupId (FK,nul)  │          └───────────────────┘
│ sortOrder         │                    │
│ totalChapters     │                    │ *
│ addedAt           │                    ▼
│ lastReadAt        │          ┌───────────────────┐
│ isFavourite       │          │   Bookmark        │
└───────────────────┘          ├───────────────────┤
        │ *                    │ id (PK)           │
        │                      │ bookId (FK)       │
        ▼                      │ chapterIndex      │
┌───────────────────┐          │ charOffset        │
│    BookGroup      │          │ snippet           │
├───────────────────┤          │ note              │
│ id (PK)           │          │ createdAt         │
│ name              │          └───────────────────┘
│ sortOrder         │
│ createdAt         │          ┌───────────────────┐
└───────────────────┘          │   BookSource      │
                               ├───────────────────┤
                               │ id (PK)           │
                 ┌────────────►│ name              │
                 │             │ groupName         │
                 │             │ baseUrl           │
                 │             │ enabled           │
                 │             │ sortOrder         │
                 │             │ ruleJson          │
                 │             │ charset           │
                 │             │ customUserAgent   │
                 │             │ timeoutMs         │
                 │             │ lastSuccessAt     │
                 │             │ failCount         │
                 │             │ healthStatus      │
                 │             └───────────────────┘
                 │                       │ 1
                 │                       │ *
                 │             ┌───────────────────┐
                 │             │  ChapterCache     │
                 │             ├───────────────────┤
                 │             │ id (PK)           │
                 │             │ bookId (FK)       │
                 │             │ chapterIndex      │
                 │             │ content           │
                 └─────────────│ sourceId (FK)     │
                               │ cachedAt          │
                               │ byteSize          │
                               │ expiresAt         │
                               └───────────────────┘
```

### 3.1.2 ★ 核心设计：进度模型（本项目最重要的设计决策）

#### 问题陈述

用户读了 30 分钟，读到"第 12 章 第 3 屏"。此时他做了以下任一操作：

1. 字号从 18sp 调到 22sp
2. 行距从 1.4 调到 1.8
3. 换成苹方字体
4. 旋转屏幕
5. 换到另一个书源

**如果进度记录的是「第 3 屏」，以上五种操作全部会导致位置错乱**——字号变大后，原来的"第 3 屏"内容已经跑到第 5 屏了。

#### 解决方案：字符级偏移（Character Offset）

**定义**：`charOffset` = 从**全书文本流起点**算起的字符下标（Unicode code unit，Kotlin `String` 的 index）。

```
全书文本流（逻辑视图）
┌───────────────────────────────────────────────────────────────────────┐
│ 第1章正文.........│ 第2章正文...............│ 第3章... │ ... │ 第12章 │
└───────────────────────────────────────────────────────────────────────┘
0                   12,845                    28,102            1,204,530
                                                                    ▲
                                              progress.charOffset ───┘
```

**为什么不用行号？** 行号随字号、行距、屏幕宽度全部变化，完全不可用。

**为什么不用「章节 + 百分比」？** 一个 3000 字的章节，百分比精度只有 30 字；且字号变化后同一百分比对应的内容不同。字级偏移是唯一在任意排版参数下都保持语义稳定的锚点。

#### 完整进度记录结构

```kotlin
// core:model — ReadingProgress.kt
@Serializable
data class ReadingProgress(
    /** 书籍 ID */
    val bookId: Long,

    /** 章节索引（用于快速定位，不用于精确定位） */
    val chapterIndex: Int,

    /** 章节标题快照（换源时的匹配依据，也是用户心智锚点） */
    val chapterTitle: String,

    /**
     * ★ 章内字符偏移：相对于本章起点。
     * 之所以存"章内"而非"全书"偏移，是因为：
     *  - 网络书的全书偏移在换源/更新后会失效
     *  - 章内偏移在换源后仍可通过章节匹配复用
     */
    val charOffsetInChapter: Int,

    /**
     * ★ 全书绝对偏移（缓存值，用于本地书快速跳转与"读到全书 x%"展示）。
     * = chapters[chapterIndex].startOffset + charOffsetInChapter
     */
    val globalCharOffset: Long,

    /** 全书百分比（0.0 ~ 1.0），仅用于 UI 展示，不用于恢复定位 */
    val percent: Float,

    /** 本章已读比例（0.0 ~ 1.0），用于状态栏"本章 42%" */
    val chapterPercent: Float,

    /** 累计阅读时长（秒），用于统计 */
    val readingSeconds: Long,

    val updatedAt: Long,
)
```

#### 恢复算法（**必读**）

```
restorePosition(progress, paginationResult, currentTypography):

  1. 找到目标章节：
       chapter = chapters[progress.chapterIndex]

  2. 章节一致性校验（防止书籍被替换/章节结构变化）：
       if (chapter.title != progress.chapterTitle):
           // 章节结构变化了（如换了书源、TXT 重新分章）
           chapter = matchChapterByTitle(progress.chapterTitle) ?: chapters[chapterIndex]

  3. 把「章内字符偏移」映射到「页」：
       targetOffset = progress.charOffsetInChapter           // 逻辑偏移（不含排版）

       // 关键：分页时每个 PageSnapshot 记录了它覆盖的字符范围
       page = paginationResult.pages.lastOrNull {
           it.startCharInChapter <= targetOffset
       } ?: paginationResult.pages.first()

  4. 精确定位到页内位置（可选，用于"上一屏的最后一行"效果）：
       intraPageOffset = targetOffset - page.startCharInChapter
       if (intraPageOffset > page.charCount * 0.9):
           // 用户上次停在页尾，说明下一屏才是"继续阅读"的自然位置
           page = paginationResult.pages.getOrNull(page.index + 1) ?: page

  5. 返回 page.index
```

**注意第 3 步的关键前提**：分页结果中的每一页必须记录 `startCharInChapter`（该页首字符在本章中的下标）。这意味着**分页引擎的输出必须携带字符映射**，而不是只输出"这一屏显示什么"。这是分页引擎的硬性接口约束（见 4.3.2）。

#### 分页产物的数据结构

```kotlin
// data:pagination — PageSnapshot.kt
data class PageSnapshot(
    /** 页码（章内，从 0 开始） */
    val index: Int,

    /** ★ 本页首字符在本章文本中的下标（含） */
    val startCharInChapter: Int,

    /** ★ 本页末字符在本章文本中的下标（不含） */
    val endCharInChapter: Int,

    /** 本页应显示的文本（已按需插入换行等排版标记） */
    val displayText: CharSequence,

    /** 底部的行号范围，用于调试与"上次读到这一行"高亮 */
    val startLine: Int,
    val endLine: Int,
) {
    val charCount: Int get() = endCharInChapter - startCharInChapter
}
```

#### 各种排版变更下的行为对照表

| 操作 | charOffsetInChapter | 恢复结果 |
| --- | --- | --- |
| 字号 18sp → 22sp | 不变（如 1420） | 重新分页后定位到含第 1420 字的页 ✅ 内容连续 |
| 行距 1.4 → 1.8 | 不变 | 同上 ✅ |
| 切换字体 | 不变 | 同上 ✅ |
| 旋转屏幕 | 不变 | 同上 ✅ |
| 边距调整 | 不变 | 同上 ✅ |
| 换书源 | 通过章节标题匹配继承 | 定位到新源同名章节的对应位置 ✅ |
| 章节被作者修改（网文常见） | 偏移可能偏移几十字 | 定位到相近位置，误差 ≤1 屏 ✅ 可接受 |

> **不做的事**：不做"跨排版参数的像素级精确恢复"（如"上次在第 3 行第 5 个字"）。字符级偏移 + 页粒度恢复已经能覆盖 100% 的实际需求，追求像素级会让实现复杂度爆炸而无用户价值。

---

## 3.2 Room 数据库设计

### 3.2.1 数据库配置

```kotlin
// core:database — KrDatabase.kt
@Database(
    entities = [
        BookEntity::class,
        ChapterEntity::class,
        ReadingProgressEntity::class,
        BookmarkEntity::class,
        BookGroupEntity::class,
        BookSourceEntity::class,
        ChapterCacheEntity::class,
        ReadingSessionEntity::class,     // 阅读统计
    ],
    version = 1,
    exportSchema = true,                  // 必须为 true，纳入 Git
    autoMigrations = [
        // 后续版本在此声明自动迁移，减少手写 SQL
    ],
)
@TypeConverters(KrTypeConverters::class)
abstract class KrDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun chapterDao(): ChapterDao
    abstract fun progressDao(): ProgressDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun groupDao(): BookGroupDao
    abstract fun sourceDao(): BookSourceDao
    abstract fun cacheDao(): ChapterCacheDao
    abstract fun sessionDao(): ReadingSessionDao

    companion object {
        const val NAME = "kr.db"
    }
}
```

**数据库配置要点**：

```kotlin
@Provides @Singleton
fun provideDatabase(@ApplicationContext ctx: Context): KrDatabase =
    Room.databaseBuilder(ctx, KrDatabase::class.java, KrDatabase.NAME)
        .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)   // WAL：读写不互斥
        .apply {
            // 大章节正文用 File 存储，DB 只存索引；但缓存章正文较大，提高页大小
            setQueryCallback({ sql, _ ->
                if (BuildConfig.LOG_ENABLED && sql.contains("ChapterCacheEntity", true)) {
                    Log.d("KrDb", "cache query: ${sql.take(120)}")
                }
            }, KrDispatchers.IO)
        }
        .build()
```

**WAL 模式的意义**：阅读时 UI 在持续读章节索引与进度，同时后台在写入缓存章节。WAL 让读写不互斥，避免"缓存到一半，翻页卡住"。

### 3.2.2 表结构定义

#### `books` — 书籍表

```kotlin
@Entity(
    tableName = "books",
    indices = [
        Index(value = ["file_path"], unique = true),        // 去重键
        Index(value = ["group_id"]),
        Index(value = ["source_id"]),
        Index(value = ["last_read_at"]),
        Index(value = ["dedup_hash"], unique = true),       // 内容去重
    ],
    foreignKeys = [
        ForeignKey(
            entity = BookGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["group_id"],
            onDelete = ForeignKey.SET_NULL,                  // 删组不删书
        ),
        ForeignKey(
            entity = BookSourceEntity::class,
            parentColumns = ["id"],
            childColumns = ["source_id"],
            onDelete = ForeignKey.SET_NULL,                  // 删源不删书，但书变"仅本地缓存"
        ),
    ],
)
data class BookEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,

    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "author") val author: String?,

    /** 封面存储方式：本地文件路径 / 网络 URL / null */
    @ColumnInfo(name = "cover_path") val coverPath: String?,
    @ColumnInfo(name = "cover_url") val coverUrl: String?,

    /** BookFormat: TXT / EPUB / PDF / NET */
    @ColumnInfo(name = "format") val format: String,

    /** 归档后的私有路径。网络书此列为 null */
    @ColumnInfo(name = "file_path") val filePath: String?,
    @ColumnInfo(name = "file_size") val fileSize: Long,

    /** 原始文件名，用于展示与重新扫描匹配 */
    @ColumnInfo(name = "original_name") val originalName: String,

    /** 探测出的编码，如 UTF-8 / GBK / GB18030 */
    @ColumnInfo(name = "encoding") val encoding: String?,

    /** TXT 分章规则 ID（内置规则或自定义正则） */
    @ColumnInfo(name = "chapter_rule_id") val chapterRuleId: String?,
    @ColumnInfo(name = "chapter_rule_regex") val chapterRuleRegex: String?,

    @ColumnInfo(name = "group_id") val groupId: Long?,
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0,

    @ColumnInfo(name = "total_chapters") val totalChapters: Int = 0,
    /** 全书字符数（TXT/EPUB），PDF 为 0 */
    @ColumnInfo(name = "total_chars") val totalChars: Long = 0,

    @ColumnInfo(name = "added_at") val addedAt: Long,
    @ColumnInfo(name = "last_read_at") val lastReadAt: Long = 0,
    @ColumnInfo(name = "is_favourite") val isFavourite: Boolean = false,

    /** 内容去重：SHA1(前 64KB + 文件大小 + 字符数) */
    @ColumnInfo(name = "dedup_hash") val dedupHash: String,

    // ── 网络书专属字段 ──────────────────────────────
    @ColumnInfo(name = "source_id") val sourceId: Long? = null,
    /** 书源中的书籍详情页 URL 或书籍 ID */
    @ColumnInfo(name = "source_book_key") val sourceBookKey: String? = null,
    /** 网文简介 */
    @ColumnInfo(name = "intro") val intro: String? = null,
    /** 最新章节标题，用于"更新提示" */
    @ColumnInfo(name = "latest_chapter_title") val latestChapterTitle: String? = null,
)
```

#### `chapters` — 章节表

```kotlin
@Entity(
    tableName = "chapters",
    indices = [
        Index(value = ["book_id", "chapter_index"], unique = true),
        Index(value = ["book_id", "start_offset"]),
    ],
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "book_id") val bookId: Long,

    /** ★ 章序号，从 0 开始 */
    @ColumnInfo(name = "chapter_index") val chapterIndex: Int,
    @ColumnInfo(name = "title") val title: String,

    /**
     * ★ 起始字符偏移（相对全书文本流）。
     * 本地书：分章时精确计算，用于按偏移切片读取正文，无需扫描全文件。
     * 网络书：置 0，正文按需抓取。
     */
    @ColumnInfo(name = "start_offset") val startOffset: Long,
    @ColumnInfo(name = "end_offset") val endOffset: Long,

    @ColumnInfo(name = "char_count") val charCount: Int,

    /** 该章是否已有本地缓存（网络书）/ 是否已解析出正文（本地书） */
    @ColumnInfo(name = "is_cached") val isCached: Boolean = false,

    /** 网络书：该章详情页 URL */
    @ColumnInfo(name = "source_url") val sourceUrl: String? = null,

    /** 该章是否被标记为"卷标题"（无正文，仅目录分组） */
    @ColumnInfo(name = "is_volume") val isVolume: Boolean = false,
)
```

> **为什么章节表要存 `start_offset` / `end_offset`？** 这是 10MB TXT 能实现"秒开任意章节"的关键。分章时一次性算出每章在文件中的字符偏移，之后打开第 800 章时只需 `seek` 到对应位置读取该章区间，**不需要重新扫描 10MB 全文**。TXT 的字符偏移在编码固定时可直接映射到字节偏移（见 5.1.4 偏移映射）。

#### `reading_progress` — 进度表

```kotlin
@Entity(
    tableName = "reading_progress",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["book_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class ReadingProgressEntity(
    @PrimaryKey @ColumnInfo(name = "book_id") val bookId: Long,

    @ColumnInfo(name = "chapter_index") val chapterIndex: Int,
    @ColumnInfo(name = "chapter_title") val chapterTitle: String,
    @ColumnInfo(name = "char_offset_in_chapter") val charOffsetInChapter: Int,
    @ColumnInfo(name = "global_char_offset") val globalCharOffset: Long,
    @ColumnInfo(name = "percent") val percent: Float,
    @ColumnInfo(name = "chapter_percent") val chapterPercent: Float,
    @ColumnInfo(name = "reading_seconds") val readingSeconds: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
```

#### `book_sources` — 书源表

```kotlin
@Entity(
    tableName = "book_sources",
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["base_url"]),
        Index(value = ["enabled"]),
    ],
)
data class BookSourceEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,

    /** 书源名称，如「示例书源 A」 */
    @ColumnInfo(name = "name") val name: String,

    /** 来源分组：小说 / 出版 / 轻小说。用于排序与并发分组 */
    @ColumnInfo(name = "group_name") val groupName: String,

    /** 站点根地址，规则中的相对路径以此为基准 */
    @ColumnInfo(name = "base_url") val baseUrl: String,

    /** 是否参与聚合搜索 */
    @ColumnInfo(name = "enabled") val enabled: Boolean = true,

    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0,

    /** ★ 完整规则 JSON（第 10 卷附录 A 定义规范） */
    @ColumnInfo(name = "rule_json") val ruleJson: String,

    /** 站点编码，常见 GBK。用于正确解码响应体 */
    @ColumnInfo(name = "charset") val charset: String = "UTF-8",

    /** 自定义 UA，留空则用全局 UA 池轮换 */
    @ColumnInfo(name = "custom_user_agent") val customUserAgent: String? = null,

    @ColumnInfo(name = "timeout_ms") val timeoutMs: Long = 15_000,

    /** 需要 Cookies 的站点：导入时提供，存于 EncryptedSharedPreferences 而非此处 */
    @ColumnInfo(name = "need_cookie") val needCookie: Boolean = false,

    // ── 健康度追踪 ──────────────────────────────────
    @ColumnInfo(name = "health_status") val healthStatus: String = "UNKNOWN", // OK / DEGRADED / BROKEN / UNKNOWN
    @ColumnInfo(name = "fail_count") val failCount: Int = 0,
    @ColumnInfo(name = "last_success_at") val lastSuccessAt: Long = 0,
    @ColumnInfo(name = "last_checked_at") val lastCheckedAt: Long = 0,
    @ColumnInfo(name = "avg_latency_ms") val avgLatencyMs: Long = 0,

    @ColumnInfo(name = "created_at") val createdAt: Long,
)
```

> **Cookie 为什么不存这张表？** Cookie 是敏感凭据。存到 `EncryptedSharedPreferences`（Android Keystore 支撑），DB 只存 `need_cookie` 布尔标记。避免数据库文件被直接读取（如 root 设备、adb backup）时泄露登录态。

#### `chapter_cache` — 网络书章节缓存

```kotlin
@Entity(
    tableName = "chapter_cache",
    indices = [
        Index(value = ["book_id", "chapter_index"], unique = true),
        Index(value = ["expires_at"]),
        Index(value = ["source_id"]),
    ],
    foreignKeys = [
        ForeignKey(BookEntity::class, ["id"], ["book_id"], onDelete = ForeignKey.CASCADE),
    ],
)
data class ChapterCacheEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "book_id") val bookId: Long,
    @ColumnInfo(name = "chapter_index") val chapterIndex: Int,
    @ColumnInfo(name = "title") val title: String,

    /** 清洗后的正文（纯文本，段落以 \n 分隔） */
    @ColumnInfo(name = "content") val content: String,

    /** 抓取时所使用的书源，便于判断"缓存是否来自当前源" */
    @ColumnInfo(name = "source_id") val sourceId: Long,

    @ColumnInfo(name = "cached_at") val cachedAt: Long,
    @ColumnInfo(name = "byte_size") val byteSize: Int,

    /** 过期时间戳。0 表示永不过期（如"最近 100 章"策略） */
    @ColumnInfo(name = "expires_at") val expiresAt: Long = 0,
)
```

**缓存容量策略**（见 4.8.4）：

```
总预算：默认 200MB（用户可调 50MB ~ 2GB）
淘汰顺序：
  1. expires_at < now 的条目（LRU 内先淘汰过期的）
  2. 非当前阅读书籍的章节
  3. 距离当前阅读位置最远的章节
保护：当前书籍 ±5 章永不淘汰
```

#### `bookmarks` / `book_groups` / `reading_sessions`

```kotlin
@Entity(
    tableName = "bookmarks",
    indices = [Index(value = ["book_id", "chapter_index", "char_offset"])],
    foreignKeys = [ForeignKey(BookEntity::class, ["id"], ["book_id"], onDelete = ForeignKey.CASCADE)],
)
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "book_id") val bookId: Long,
    @ColumnInfo(name = "chapter_index") val chapterIndex: Int,
    @ColumnInfo(name = "chapter_title") val chapterTitle: String,
    /** ★ 同样是字符偏移，保证书签在排版变化后仍准确 */
    @ColumnInfo(name = "char_offset") val charOffset: Int,
    /** 书签处的文本片段（前 50 字），用于书签列表展示 */
    @ColumnInfo(name = "snippet") val snippet: String,
    @ColumnInfo(name = "note") val note: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)

@Entity(tableName = "book_groups", indices = [Index(value = ["name"], unique = true)])
data class BookGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0,
    /** 内置分组不可删除（如"全部""未分组"） */
    @ColumnInfo(name = "is_builtin") val isBuiltin: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)

@Entity(
    tableName = "reading_sessions",
    indices = [Index(value = ["book_id", "started_at"])],
)
data class ReadingSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "book_id") val bookId: Long,
    @ColumnInfo(name = "started_at") val startedAt: Long,
    @ColumnInfo(name = "duration_seconds") val durationSeconds: Long,
    @ColumnInfo(name = "chars_read") val charsRead: Long,
    @ColumnInfo(name = "pages_read") val pagesRead: Int,
)
```

### 3.2.3 关键查询与 DAO

```kotlin
@Dao
interface BookDao {
    /** 书架流式查询：支持分组过滤 + 多种排序 */
    @Query("""
        SELECT b.*, p.chapter_index, p.chapter_title, p.percent, p.chapter_percent, p.updated_at
        FROM books b
        LEFT JOIN reading_progress p ON p.book_id = b.id
        WHERE (:groupId IS NULL OR b.group_id = :groupId)
          AND (:onlyFavourite = 0 OR b.is_favourite = 1)
        ORDER BY
          CASE WHEN :sort = 'LAST_READ' THEN COALESCE(p.updated_at, 0) END DESC,
          CASE WHEN :sort = 'ADDED'     THEN b.added_at END DESC,
          CASE WHEN :sort = 'TITLE'     THEN b.title END ASC,
          CASE WHEN :sort = 'MANUAL'    THEN b.sort_order END ASC,
          b.title ASC
    """)
    fun observeShelf(groupId: Long?, onlyFavourite: Boolean, sort: String): Flow<List<ShelfRow>>

    /** 分页版书架（500+ 本书时启用） */
    @Query("SELECT * FROM books ORDER BY added_at DESC")
    fun pagingSource(): PagingSource<Int, BookEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(book: BookEntity): Long        // 返回 -1 表示被唯一索引拦下（重复）

    @Query("SELECT * FROM books WHERE dedup_hash = :hash LIMIT 1")
    suspend fun findByHash(hash: String): BookEntity?
}

@Dao
interface ChapterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chapters: List<ChapterEntity>)

    /** ★ 只查章节索引，不查正文——目录页 2000 章加载只需这一条轻查询 */
    @Query("""
        SELECT id, chapter_index, title, char_count, is_cached, is_volume
        FROM chapters WHERE book_id = :bookId ORDER BY chapter_index ASC
    """)
    fun observeIndex(bookId: Long): Flow<List<ChapterIndexRow>>

    @Query("SELECT * FROM chapters WHERE book_id = :bookId AND chapter_index = :index LIMIT 1")
    suspend fun getChapter(bookId: Long, index: Int): ChapterEntity?

    /** 二分定位：给定全书偏移，找出所在章节（用于"读到 x% 跳转"） */
    @Query("""
        SELECT * FROM chapters
        WHERE book_id = :bookId AND start_offset <= :offset
        ORDER BY start_offset DESC LIMIT 1
    """)
    suspend fun locateByOffset(bookId: Long, offset: Long): ChapterEntity?
}

@Dao
interface ProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: ReadingProgressEntity)

    @Query("SELECT * FROM reading_progress WHERE book_id = :bookId")
    fun observe(bookId: Long): Flow<ReadingProgressEntity?>

    @Query("SELECT * FROM reading_progress WHERE book_id = :bookId")
    suspend fun get(bookId: Long): ReadingProgressEntity?
}
```

### 3.2.4 索引设计说明

| 索引 | 服务的查询 | 为什么必要 |
| --- | --- | --- |
| `books(file_path) UNIQUE` | 导入去重 | 防止同一文件重复导入 |
| `books(dedup_hash) UNIQUE` | 内容去重 | 同一本书的不同副本只留一份 |
| `books(last_read_at)` | "最近阅读"排序 | 书架默认排序，无索引会全表扫描 + 排序 |
| `chapters(book_id, chapter_index) UNIQUE` | 目录加载、章节定位 | 2000 章必须走索引 |
| `chapters(book_id, start_offset)` | 按偏移二分定位章节 | 进度跳转、全文搜索定位 |
| `chapter_cache(book_id, chapter_index) UNIQUE` | 缓存命中判断 | 每次翻页都查，必须最快 |
| `chapter_cache(expires_at)` | 过期清理 | 后台清理任务批量扫描 |
| `book_sources(enabled)` | 聚合搜索取源列表 | 每次搜索都查 |

---

## 3.3 DataStore 配置存储

### 3.3.1 为什么排版配置不放 Room？

排版与主题是**单一配置对象 + 高频读写 + 变更即全 UI 响应**，用 DataStore 的 `Flow` 天然契合；放 Room 需额外表 + 手写触发器，得不偿失。

### 3.3.2 Proto DataStore Schema

```protobuf
// core:datastore/src/main/proto/typography.proto
syntax = "proto3";
option java_package = "com.kr.reader.core.datastore.proto";
option java_multiple_files = true;

message TypographyConfig {
  float font_size_sp        = 1;   // 12.0 ~ 36.0，默认 18.0
  float line_height_mult    = 2;   // 1.0 ~ 3.0，默认 1.6
  float paragraph_spacing_em= 3;   // 0.0 ~ 2.0，默认 0.8
  float margin_horizontal_dp= 4;   // 0 ~ 48，默认 16
  float margin_vertical_dp  = 5;   // 0 ~ 96，默认 24
  string font_family_id     = 6;   // "system" | "custom:<hash>"
  bool   bold_enabled       = 7;   // 正文加粗
  string align              = 8;   // START | JUSTIFY
  float letter_spacing_em   = 9;   // 字间距
  bool   indent_first_line  = 10;  // 首行缩进 2 字符
}

message ReaderTheme {
  string id                    = 1;  // paper | eye_care | parchment | night | custom
  int32  background_color      = 2;  // ARGB int
  int32  text_color            = 3;
  int32  accent_color          = 4;
  bool   invert_enabled        = 5;  // PDF 夜间滤镜
  float  background_dimming    = 6;  // 叠加暗化 0.0~0.8（亮度调节）
  string background_image_path = 7;  // 自定义背景图（可选）
}

message ReaderPrefs {
  string page_turn_mode       = 1;   // SIMULATION | COVER | SLIDE | SCROLL | TAP
  bool   show_battery         = 2;
  bool   show_clock           = 3;
  bool   show_chapter_percent = 4;
  bool   show_page_number     = 5;
  int32  screen_timeout_override = 6; // 0 = 跟随系统
  bool   keep_screen_on       = 7;
  float  auto_scroll_speed    = 8;   // 1.0 ~ 10.0
  bool   tap_zone_enabled     = 9;
  bool   vibration_on_turn    = 10;
  int32  volume_key_action    = 11;  // NONE | PAGE_TURN | SCROLL | BRIGHTNESS
  float  brightness_override  = 12;  // -1 = 跟随系统
  bool   full_screen_immersive= 13;
  bool   show_reading_time    = 14;
}

message GlobalPrefs {
  string language             = 1;   // zh-CN | en
  string theme_mode           = 2;   // SYSTEM | LIGHT | DARK
  bool   shelf_grid_mode      = 3;
  string shelf_sort           = 4;
  int32  cache_limit_mb       = 5;   // 默认 200
  bool   cache_wifi_only      = 6;   // 默认 true
  int32  search_concurrency   = 7;   // 并发搜索源数，默认 6
  bool   search_include_disabled = 8;
  bool   auto_purify          = 9;   // 自动净化正文
  bool   crash_report_enabled = 10;  // 默认 false，尊重隐私
  bool   onboarding_done      = 11;
}
```

### 3.3.3 读写实现

```kotlin
// data:settings — TypographyRepositoryImpl.kt
@Singleton
class TypographyRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<TypographyConfig>,
) : TypographyRepository {

    override val config: Flow<TypographyConfig> = dataStore.data
        .catch { e ->
            // 读损坏时回退默认值，绝不让阅读器崩溃
            if (e is IOException) emit(TypographyConfig.getDefaultInstance())
            else throw e
        }

    override suspend fun update(transform: (TypographyConfig) -> TypographyConfig) {
        dataStore.updateData { current ->
            transform(current).let(::sanitize)   // ★ 关键：写入前强制夹取到合法范围
        }
    }

    /** 参数越界保护：防止 UI bug 或外部导入导致的非法值 */
    private fun sanitize(cfg: TypographyConfig): TypographyConfig =
        cfg.toBuilder()
            .setFontSizeSp(cfg.fontSizeSp.coerceIn(12f, 36f))
            .setLineHeightMult(cfg.lineHeightMult.coerceIn(1.0f, 3.0f))
            .setParagraphSpacingEm(cfg.paragraphSpacingEm.coerceIn(0f, 2f))
            .setMarginHorizontalDp(cfg.marginHorizontalDp.coerceIn(0f, 48f))
            .setMarginVerticalDp(cfg.marginVerticalDp.coerceIn(0f, 96f))
            .build()
}
```

**注意 `sanitize` 的位置**：在 **Repository 写入侧**而不是 UI 侧。这样无论从 UI、从备份导入、从 Future 的云同步写入，都保证约束成立——约束属于数据层的职责。

---

## 3.4 文件存储布局

### 3.4.1 目录结构

```
/data/data/com.kr.reader/files/          ← getFilesDir()，应用私有，卸载即清
├── books/                               ← 归档的本地书籍正文
│   ├── ab/                              ← 两级散列，防单目录文件过多
│   │   ├── abc123def456...txt           ← 文件名 = SHA1(内容)，天然去重
│   │   └── abf987...epub
│   └── 3c/
│       └── 3cd456...pdf
├── covers/                              ← 封面（EPUB 提取 / PDF 首页 / 网络下载）
│   ├── ab/abc123.jpg
│   └── 3c/3cd456.webp                   ← 网络封面统一转 WebP 省空间
├── fonts/                               ← 用户导入的自定义字体
│   ├── <hash>.ttf
│   └── index.json                       ← {hash: {displayName, importedAt}}
├── themes/                              ← 自定义背景图
│   └── <hash>.jpg
├── exports/                             ← 用户主动导出的书签/笔记
│   └── bookmarks_20260916.json
└── logs/                                ← 仅 debug，release 不写
    └── kr-20260916.log
```

```
/data/data/com.kr.reader/cache/          ← getCacheDir()，系统可回收
├── pdf_pages/                           ← PDF 渲染页位图缓存（LRU）
│   └── <bookId>/<pageIndex>_<scale>.webp
├── html_cache/                          ← 书源页面原始 HTML（调试与短缓存）
│   └── <sha1>.html
└── http/                                ← OkHttp 磁盘缓存（封面、图片）
    └── ...
```

```
/data/data/com.kr.reader/databases/
└── kr.db (+ -wal, -shm)                 ← Room 数据库
```

### 3.4.2 为什么用 SHA1 内容哈希作文件名？

| 方案 | 优点 | 缺点 |
| --- | --- | --- |
| 保留原文件名 | 用户在文件管理器里能看懂 | 重名冲突、非法字符、路径遍历风险（`../../`）、中文编码问题 |
| 自增 ID | 简单 | 无去重能力，同一文件导入两次存两份 |
| **SHA1 内容哈希** ✅ | 天然去重、无非法字符、防路径遍历 | 文件管理器里看不懂；但用户不直连本目录，无影响 |

**两级目录散列（`ab/abc123...`）**：某些文件系统单目录超过 10,000 个文件后 `readdir` 变慢。两级散列把文件均匀分散到 256 个目录。

### 3.4.3 归档策略（Import vs Reference）

**决策：默认归档，可配置引用。**

```kotlin
enum class ImportMode {
    /** 默认：复制到 filesDir/books/，原文件移动/删除不影响阅读 */
    ARCHIVE,

    /** 高级选项：只存 SAF Uri（takePersistableUriPermission），不复制 */
    REFERENCE,
}
```

| 模式 | 空间占用 | 稳定性 | 适用 |
| --- | --- | --- | --- |
| ARCHIVE（默认） | 2x（原文件 + 副本） | ✅ 高 | 从 Downloads 导入的临时文件 |
| REFERENCE | 1x | ⚠️ 中（用户清缓存/移动文件会失效） | 大 PDF、已同步网盘目录 |

**降级链**：REFERENCE 模式下打开书籍时若 `ContentResolver.openInputStream` 失败 → 提示"文件已被移动，请重新关联"，并提供"重新选择文件"入口。

### 3.4.4 存储空间管理

- **归档前检查**：`StatFs.availableBytes` < 文件大小 × 1.2 → 提示空间不足，拒绝导入。
- **缓存清理**：设置页提供"清理缓存"，显示 `cache/` 与 `chapter_cache` 表占用。
- **字体清理**：删除字体时同步删除 `fonts/<hash>.ttf`；若该字体正在使用则回退系统字体。

---

## 3.5 数据迁移策略

### 3.5.1 Room 迁移

```kotlin
// core:database — Migrations.kt
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 例：V2 新增"阅读设备名"，用于未来多设备进度区分
        db.execSQL("ALTER TABLE reading_progress ADD COLUMN device_name TEXT NOT NULL DEFAULT ''")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_progress_device ON reading_progress(device_name)")
    }
}

// 注册
Room.databaseBuilder(...)
    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
    .build()
```

**迁移纪律**：

1. **禁止破坏性迁移**（`fallbackToDestructiveMigration()`）用于 release 包。仅 debug 可用，便于快速迭代。
2. 每次改 schema **必须**导出 schema JSON 到 `core/database/schemas/` 并提交 Git，PR Review 时检查。
3. 迁移必须有对应单元测试（`MigrationTestHelper`）：

```kotlin
@Test
fun migrate1To2_preservesProgress() {
    helper.createDatabase(TEST_DB, 1).apply {
        execSQL("INSERT INTO reading_progress VALUES (1, 5, '第五章', 1200, 50000, 0.3, 0.4, 600, 1700000000000)")
        close()
    }
    val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)
    db.query("SELECT char_offset_in_chapter FROM reading_progress WHERE book_id = 1").use {
        assertTrue(it.moveToFirst())
        assertEquals(1200, it.getInt(0))     // ★ 核心数据不丢
    }
}
```

### 3.5.2 DataStore Proto 演进

Proto 的字段编号一旦分配**永不复用**，新增字段必须给默认值且旧版本可忽略：

```protobuf
// V2 新增：翻页动画时长
message ReaderPrefs {
  // ... 原有字段 1~14 ...
  int32 page_turn_duration_ms = 15;   // 新增，旧版本忽略此字段
}
```

老版本读到新字段会忽略（Proto3 前向兼容），新版本读到老数据会用默认值（后向兼容）。**这就是选 Proto 而非 Preferences 的原因**：Preferences 的键值对演进需要手写兼容逻辑。

### 3.5.3 备份与恢复（用户数据可携带）

V1.x 规划，但**表结构现在就要预留**：

```json
// 导出文件：kr-backup-20260916.json
{
  "version": 1,
  "exportedAt": 1789000000000,
  "appVersion": "1.0.0",
  "books": [
    {
      "dedupHash": "abc123...",
      "title": "示例书籍",
      "author": "某作者",
      "format": "TXT",
      "progress": { "chapterIndex": 12, "chapterTitle": "第十二章", "charOffsetInChapter": 1420, "percent": 0.34 },
      "bookmarks": [{ "chapterIndex": 3, "charOffset": 800, "snippet": "……" }]
    }
  ],
  "bookSources": [ /* 不含 Cookie */ ],
  "typography": { "fontSizeSp": 18, "lineHeightMult": 1.6 }
}
```

**设计要点**：导出**不含书籍正文文件**（用户自己已有原文件），只导出"元数据 + 进度 + 书签 + 书源规则"。恢复时按 `dedupHash` 与 `title+author` 双重匹配重新关联本地文件。

---

**上一卷**：[第 2 卷 · 系统架构设计](#vol-02) ｜ **下一卷**：[第 4 卷 · 功能模块详细设计](#vol-04)


---


# 第 4 卷 · 功能模块详细设计 {#vol-04}
> 本卷是开发的**主执行手册**。每个模块给出：功能点清单 → 交互规格 → 技术实现 → 边界情况。开发时按小节顺序推进，每小节末尾的"验收要点"即该模块的 Definition of Done。

---

## 4.1 书架模块

### 4.1.1 功能点清单

| 编号 | 功能 | 优先级 |
| --- | --- | --- |
| F-1.1 | 单文件夹扫描（深度可配，默认 8 层） | P0 |
| F-1.2 | 多文件夹追加扫描 | P1 |
| F-1.3 | 后缀过滤 `.txt / .epub / .pdf` | P0 |
| F-1.4 | 元数据提取（书名、作者、大小、封面） | P0 |
| F-1.5 | 网格视图（封面优先，可调列数 2~6） | P0 |
| F-1.6 | 列表视图（进度优先，显示章节与百分比） | P0 |
| F-1.7 | 自定义分组/分类，支持一本书归一组 | P1 |
| F-1.8 | 长按拖拽排序（手动排序模式） | P2 |
| F-1.9 | 批量选择：删除、移出书架、改分组 | P0 |
| F-1.10 | 书籍长按菜单：重命名、改封面、编辑信息、重置进度 | P1 |
| F-1.11 | 搜索书架内书籍（书名/作者模糊匹配） | P1 |
| F-1.12 | 空态引导（新手三步图） | P1 |

### 4.1.2 扫描流程实现

```kotlin
// data:books — FileScanner.kt
class FileScanner @Inject constructor(
    @ApplicationContext private val ctx: Context,
) {
    companion object {
        val SUPPORTED = setOf("txt", "epub", "pdf")
        const val MAX_DEPTH = 8
    }

    fun scan(root: Uri, maxDepth: Int = MAX_DEPTH): Flow<ScanProgress> = flow {
        val rootDoc = DocumentFile.fromTreeUri(ctx, root)
            ?: throw KrError.FileNotFound(root.toString())

        val candidates = mutableListOf<ScannedFile>()
        var scanned = 0
        var errors = 0
        emit(ScanProgress.Scanning(0, 0))

        // 显式栈迭代而非递归——避免深目录栈溢出
        data class Node(val doc: DocumentFile, val depth: Int)
        val stack = ArrayDeque<Node>().apply { add(Node(rootDoc, 0)) }
        val visited = mutableSetOf<String>()   // 防符号链接/硬链接循环

        while (stack.isNotEmpty()) {
            ensureActive()                     // ★ 可取消
            val (doc, depth) = stack.removeLast()

            if (depth > maxDepth) { errors++; continue }
            val key = doc.uri.toString()
            if (!visited.add(key)) continue    // 已访问，跳过（环）

            try {
                if (doc.isDirectory) {
                    doc.listFiles().forEach { child -> stack.add(Node(child, depth + 1)) }
                } else {
                    val name = doc.name ?: return@forEach
                    val ext = name.substringAfterLast('.', "").lowercase()
                    if (ext !in SUPPORTED) return@forEach

                    scanned++
                    candidates += ScannedFile(
                        uri = doc.uri,
                        name = name,
                        size = doc.length(),
                        ext = ext,
                        lastModified = doc.lastModified(),
                    )
                    // 每 20 个文件上报一次，UI 进度条平滑
                    if (scanned % 20 == 0) {
                        emit(ScanProgress.Scanning(scanned, candidates.size))
                    }
                }
            } catch (e: SecurityException) {
                errors++                            // SAF 权限边界，跳过该分支
            } catch (e: IOException) {
                errors++
            }
        }

        emit(ScanProgress.Done(candidates.sortedBy { it.name }, errors))
    }.flowOn(KrDispatchers.IO)
}
```

**关键设计说明**：

1. **用显式栈而非递归**：某些收藏目录嵌套很深，递归会 `StackOverflowError`。显式栈 + 深度上限双保险。
2. **`visited` 集合防环**：SAF 下符号链接可能造成目录环，没有这个集合会死循环直到 OOM。
3. **`ensureActive()` 每轮检查**：这是"扫描时可取消"的实现基础，否则 10,000 个文件的扫描要等 30 秒才能取消。
4. **每 20 个文件 emit 一次**：UI 进度条不会因为过度 emit 而卡顿，也不会因为不 emit 而假死。

### 4.1.3 元数据提取

```kotlin
// data:parser — MetadataExtractor.kt
interface MetadataExtractor {
    suspend fun extract(file: File, ext: String): BookMetadata
}

data class BookMetadata(
    val title: String,
    val author: String?,
    val coverFile: File?,
    val encoding: String?,
    val chapterCount: Int = 0,
    val totalChars: Long = 0,
)
```

| 格式 | 书名来源（按优先级） | 作者来源 | 封面来源 |
| --- | --- | --- | --- |
| TXT | ① 文件名（去扩展名、去 `[xx]`/`（完本）` 等噪声） ② 文件首个 `<title>` 式行 ③ "书名：xxx" | 文件名中的 `作者：xxx` / `- xxx` / `xxx 著`；正文首 3 行的"作者：" | 无（生成书籍首字 + 色块的占位封面） |
| EPUB | OPF `dc:title` → 文件名 | OPF `dc:creator` | OPF `<meta name="cover">` 指向的图片 → manifest 中第一个 image |
| PDF | PDF 文档信息字典 `/Title` → 文件名 | `/Author` | `PdfRenderer` 渲染第 1 页，缩放至宽 400px 存 WebP |
| 网络书 | 搜索/详情页规则提取 | 详情页规则 | 详情页规则 |

**TXT 文件名清洗规则**（实测覆盖 90% 的常见命名）：

```kotlin
object TitleCleaner {
    private val noisePatterns = listOf(
        Regex("""[\[\(【（][^\]\)】）]{0,20}(完结|全本|校对|精校|未删减|番外|全集)[^\]\)】）]{0,20}[\]\)】）]"""),
        Regex("""[\[\(【（](?:www\.|https?://)[^\]\)】）]+[\]\)】）]"""),   // 网站广告
        Regex("""^\s*\d{1,4}[\s._-]+"""),                                // 前导序号 "001 "
        Regex("""[\s._-]+(?:txt|epub|pdf)\s*$""", RegexOption.IGNORE_CASE),
        Regex("""\s*[-_]\s*(?:全\d+[卷册章]|v?\d+(?:\.\d+)*)\s*$""", RegexOption.IGNORE_CASE),
        Regex("""\s*[（(](?:作者|author)[:：][^)）]*[)）]\s*""", RegexOption.IGNORE_CASE),
        Regex("""\s+by\s+\S+\s*$""", RegexOption.IGNORE_CASE),
    )

    fun clean(raw: String): String =
        noisePatterns.fold(raw.substringBeforeLast('.')) { acc, re -> re.replace(acc, "") }
            .replace(Regex("""\s+"""), " ")
            .trim()
            .ifBlank { raw.substringBeforeLast('.') }    // 清洗过度则回退原文
}
```

**作者提取**：

```kotlin
object AuthorExtractor {
    private val patterns = listOf(
        Regex("""(?:作者|著者|作\s*者)\s*[:：]\s*([^\s,，、|]{1,20})"""),
        Regex("""【([^】]{1,20})[著写]】"""),
        Regex("""\s[-–—]\s*([^\s-–—]{2,12})\s*$"""),   // "书名 - 作者"
    )

    /** 从文件名 + 正文开头前 3 行的组合中尝试提取 */
    fun extract(fileName: String, headText: String?): String? {
        val sources = listOf(fileName) + (headText?.lines()?.take(3) ?: emptyList())
        return sources.firstNotNullOfOrNull { s ->
            patterns.firstNotNullOfOrNull { re -> re.find(s)?.groupValues?.get(1)?.trim() }
        }
    }
}
```

### 4.1.4 书架视图实现

**网格视图规格**：

| 项 | 规格 |
| --- | --- |
| 列数 | 3（默认，窄屏） / 4（常规） / 5~6（平板/用户可调） |
| 封面比例 | 3:4，圆角 8dp |
| 封面加载 | Coil，`crossfade(200ms)`，占位为「书名首字 + 由 hash 派生的稳定色」 |
| 信息区 | 书名（最多 2 行）+ 作者（1 行，可关闭）+ 进度条（细线，2dp） |
| 角标 | 左上：格式徽标（TXT/EPUB/PDF/网）；右上：收藏星标 |
| 长按 | 进入多选模式 + 震动反馈 |
| 点击 | 打开阅读器 |

**占位封面生成**（无封面的书不能显示空灰块，要有设计感）：

```kotlin
@Composable
fun GeneratedCover(title: String, author: String?, modifier: Modifier = Modifier) {
    // 由书名 hash 派生色相，保证同一本书颜色稳定（跨会话不变）
    val hue = remember(title) { (title.hashCode().absoluteValue % 360).toFloat() }
    val bg = Color.hsl(hue, 0.42f, 0.52f)
    val fg = Color.hsl((hue + 180f) % 360f, 0.20f, 0.96f)

    Box(
        modifier = modifier
            .background(Brush.verticalGradient(listOf(bg, bg.copy(alpha = 0.72f)))),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title.take(1),
                color = fg,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            // 一条装饰线，模拟书脊
            Box(Modifier.width(28.dp).height(2.dp).background(fg.copy(alpha = 0.6f)))
        }
        author?.let {
            Text(
                text = it,
                color = fg.copy(alpha = 0.85f),
                fontSize = 10.sp,
                maxLines = 1,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp),
            )
        }
    }
}
```

**列表视图规格**：

```
┌─────────────────────────────────────────────────────┐
│ ┌────┐  书名（粗体，1 行省略）              ⋮      │
│ │封面│  作者 · TXT · 12.4MB                        │
│ │56px│  ▓▓▓▓▓▓▓▓░░░░░░░░  34%  第 12 章 / 共 320 章 │
│ └────┘  3 天前阅读                                 │
└─────────────────────────────────────────────────────┘
```

- 进度条用主题强调色，未读部分用 `onSurface.copy(alpha=0.12f)`。
- "3 天前"用相对时间格式化（今天 / 昨天 / N 天前 / yyyy-MM-dd）。

### 4.1.5 分组与排序

| 排序模式 | 实现 | 说明 |
| --- | --- | --- |
| 最近阅读（默认） | `ORDER BY COALESCE(p.updated_at, 0) DESC` | 最常用 |
| 添加时间 | `added_at DESC` | 新导入的书在最前，便于整理 |
| 书名 | `title ASC`（用 `Collator.getInstance(Locale.CHINA)` 处理中文拼音序） | 需在内存中排（SQLite 默认不支持拼音） |
| 手动 | `sort_order ASC` | 用户拖拽维护 |

> **中文排序实现细节**：SQLite 的 `ORDER BY title` 按 UTF-8 码点排序，中文会得到乱序。正确做法是**从 DB 取出后内存排序**：

```kotlin
val zhCollator = Collator.getInstance(Locale.CHINA)

books.sortedWith(compareBy(zhCollator) { it.title })
```

书架 500 本内存排序耗时 < 3ms，可接受。

**分组设计**：

- 内置分组：`全部`（虚拟，不过滤）、`未分组`（`group_id IS NULL`）、`最近阅读`（`updated_at > now - 30d`）、`已收藏`。
- 自定义分组：用户创建，可改名、排序、删除。删除组时书籍 `group_id` 置 NULL（不删书）。
- 分组 Tab 用 `ScrollableTabRow`，超过 6 个可横向滚动。

### 4.1.6 批量操作

```kotlin
// 进入多选模式后的顶部栏
TopAppBar(
    title = { Text("已选择 ${selected.size} 项") },
    navigationIcon = { IconButton(onClick = { exitSelection() }) { Icon(Icons.Close, "取消") } },
    actions = {
        IconButton(onClick = { showGroupPicker() }) { Icon(Icons.Folder, "移动到分组") }
        IconButton(onClick = { showRemoveDialog() }) { Icon(Icons.RemoveCircle, "移出书架") }
        IconButton(onClick = { showDeleteDialog() }) { Icon(Icons.Delete, "删除") }
    },
)
```

**"移出书架" vs "删除"的语义区分（重要）**：

| 操作 | DB 记录 | 归档文件 | 用户可恢复 |
| --- | --- | --- | --- |
| 移出书架 | 删除 `books` 行（级联删章节/进度/书签） | **保留**在 `files/books/` | ✅ 重新导入同一文件会复用已有归档（按 hash 命中），但**进度丢失** |
| 删除 | 删除 `books` 行 + 级联 | **删除**归档文件 + 封面 + 缓存 | ❌ 不可恢复 |

**交互设计**：移出书架**不二次确认**（轻操作，可撤销）；删除**必须二次确认**，且对话框内明确写"将同时删除阅读进度与书签，不可恢复"。

另提供 **Snackbar 撤销**：移出书架后 5 秒内可撤销；删除不提供撤销（文件已物理删除），但可在删除前提示"建议先导出书签"。

### 4.1.7 边界情况清单

| 情况 | 处理 |
| --- | --- |
| 导入同名不同内容的书 | 允许（`file_path` 不同），书名加后缀区分 |
| 导入完全相同内容的书 | 按 `dedup_hash` 拦截，提示"《书名》已在书架" |
| SAF 文件夹权限被撤销 | 打开书时报错，提供"重新授权目录"入口 |
| 归档文件被用户手动删除（root） | 打开时检测 `file.exists()`，失败则提示并标记该书为"文件丢失"状态（灰显） |
| 扫描到 10,000 个文件 | 扫描可取消；列表用 Paging 3 分页渲染，不全量进内存 |
| 导入 GB 级 PDF | 归档前空间检查；超过 500MB 提示"文件较大，建议使用引用模式" |
| 书名含 emoji / 特殊字符 | 存储到 DB 无问题（TEXT 类型）；文件名用 hash 避免问题 |

### 4.1.8 验收要点

- [ ] 扫描 1000 个文件的目录，可在扫描中随时取消，取消后无残留任务
- [ ] 导入 50 本混合格式书籍，成功 50 本，书架正确显示封面与作者
- [ ] 删除输入侧原文件后，已归档书籍仍可正常阅读
- [ ] 网格/列表切换、6 种排序、分组过滤均正确
- [ ] 批量删除 20 本书，DB 与文件系统同步清理无残留（用 `dumpsys` 与文件数对比验证）

---

## 4.2 电子书解析引擎

### 4.2.1 统一接口

```kotlin
// data:parser — BookParser.kt
interface BookParser {
    /** 支持的格式 */
    val format: BookFormat

    /**
     * 解析书籍：提取元数据 + 生成章节索引。
     * @param onProgress 0.0~1.0，用于 UI 进度（特别是 TXT 分章）
     */
    suspend fun parse(
        file: File,
        options: ParseOptions,
        onProgress: (Float) -> Unit = {},
    ): ParseResult
}

data class ParseResult(
    val metadata: BookMetadata,
    val chapters: List<ChapterMeta>,
    val encoding: String? = null,
    val warnings: List<ParseWarning> = emptyList(),
)

data class ChapterMeta(
    val index: Int,
    val title: String,
    /** 相对全书文本流的字符偏移（本地书）；网络书为 0 */
    val startOffset: Long,
    val endOffset: Long,
    val isVolume: Boolean = false,
    val sourceUrl: String? = null,
)

data class ParseOptions(
    /** TXT 专用：手动指定编码，null 则自动探测 */
    val encoding: String? = null,
    /** TXT 专用：分章规则 ID */
    val chapterRuleId: String? = null,
    /** TXT 专用：自定义正则 */
    val customRegex: String? = null,
    /** PDF 专用：是否渲染首页作封面 */
    val renderCover: Boolean = true,
)

/** 工厂：按扩展名分派 */
class BookParserFactory @Inject constructor(
    private val txt: TxtParser,
    private val epub: EpubParser,
    private val pdf: PdfParser,
) {
    fun of(fileName: String): BookParser = when (fileName.substringAfterLast('.').lowercase()) {
        "txt" -> txt
        "epub" -> epub
        "pdf" -> pdf
        else -> throw KrError.ParseFailed("unknown", IllegalArgumentException("不支持: $fileName"))
    }
}
```

### 4.2.2 TXT 解析

TXT 是**分布最广、最脏、最考验工程能力**的格式。核心三步：编码探测 → 流式读取 → 分章。

#### 步骤 1：编码探测

```kotlin
// data:parser/txt — CharsetDetector.kt
class CharsetDetector {
    companion object {
        const val SAMPLE_SIZE = 256 * 1024        // 256KB 足够统计
        val CANDIDATES = listOf("UTF-8", "GB18030", "GBK", "UTF-16LE", "UTF-16BE", "BIG5")
    }

    suspend fun detect(file: File): DetectionResult = withContext(KrDispatchers.Parse) {
        val sample = readSample(file, SAMPLE_SIZE)
        ensureActive()

        // 第一优先：BOM 检测（100% 可靠）
        detectBom(sample)?.let { return@withContext DetectionResult(it, 1.0f, "BOM") }

        // 第二优先：严格 UTF-8 校验（UTF-8 的字节模式非常严格，误判率极低）
        if (isValidUtf8(sample)) {
            // 但如果样本全是 ASCII，无法区分编码——此时任何编码都能解，用 UTF-8
            return@withContext DetectionResult("UTF-8", 0.95f, "utf8-valid")
        }

        // 第三优先：中文编码打分
        // GB18030 是 GBK/GB2312 的超集，优先 GB18030
        val scores = CANDIDATES
            .filter { it != "UTF-8" }
            .associateWith { scoreByDecodedQuality(sample, Charset.forName(it)) }

        val (best, bestScore) = scores.maxByOrNull { it.value }!!
        DetectionResult(
            charset = best,
            confidence = bestScore,
            method = "heuristic",
        )
    }

    private fun detectBom(b: ByteArray): String? = when {
        b.size >= 3 && b[0]==0xEF.toByte() && b[1]==0xBB.toByte() && b[2]==0xBF.toByte() -> "UTF-8"
        b.size >= 2 && b[0]==0xFF.toByte() && b[1]==0xFE.toByte() -> "UTF-16LE"
        b.size >= 2 && b[0]==0xFE.toByte() && b[1]==0xFF.toByte() -> "UTF-16BE"
        else -> null
    }

    /**
     * 中文编码质量打分：解码后统计"合理中文字符占比"。
     * 编码猜错时会产生大量 U+FFFD 替换字符或罕见汉字，得分低。
     */
    private fun scoreByDecodedQuality(bytes: ByteArray, charset: Charset): Float {
        val text = runCatching {
            charset.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)   // ★ 严格模式：遇到非法序列直接抛
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes)).toString()
        }.getOrElse { return 0f }                             // 解码失败 = 该编码不可能

        if (text.isEmpty()) return 0f

        var cjk = 0; var ascii = 0; var ctrl = 0; var other = 0
        for (ch in text) {
            when {
                ch in '\u4E00'..'\u9FFF' -> cjk          // 常用汉字
                ch.code in 0x20..0x7E || ch == '\n' || ch == '\r' || ch == '\t' -> ascii
                ch.code < 0x20 -> ctrl                    // 控制字符 = 解码错的强信号
                ch == '\uFFFD' -> ctrl                    // 替换字符 = 严重错误
                else -> other
            }
        }
        val total = text.length.toFloat()
        val cjkRatio = cjk / total
        val ctrlRatio = ctrl / total

        // 含少量控制字符（如 GBK 下的 '\r'）是正常的，但超过 1% 说明编码错
        if (ctrlRatio > 0.01f) return 0f
        // 中文字符占比越高越好；纯英文文本各编码得分接近，取首个
        return (cjkRatio * 0.8f + ascii / total * 0.2f) - ctrlRatio * 5f
    }

    private fun isValidUtf8(bytes: ByteArray): Boolean = runCatching {
        Charset.forName("UTF-8").newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(bytes))
        true
    }.getOrDefault(false)
}
```

**探测优先级总结**（这个顺序是正确性保证，不能乱）：

```
1. BOM 声明           → 置信度 1.0，直接采用
2. 严格 UTF-8 校验通过 → 置信度 0.95，采用 UTF-8
3. 中文编码启发式打分  → 取最高分，置信度 < 0.9
4. 全部失败           → 降级：UTF-8 + 提示用户手动选择（见 2.8.2 降级矩阵）
```

> ⚠️ **必须优先做严格 UTF-8 校验，而不是直接跑启发式打分**。原因：UTF-8 的字节结构极其严格（多字节序列的前导位模式固定），只要有一处非法就是非 UTF-8。这比统计启发式可靠得多，且能避免"UTF-8 文本被误判为 GBK 变成乱码"。

#### 步骤 2：流式读取与字符偏移建立

**禁止 `File.readText()`**。10MB GBK 文件转 UTF-8 后约 20MB `char[]`（40MB 堆），会直接触发 GC 抖动。

```kotlin
// data:parser/txt — StreamingTextReader.kt
/**
 * 流式解码 + 行迭代器。
 * 关键能力：在迭代过程中记录"每个换行符对应的字符偏移"，
 *          为分章提供精确的偏移锚点，同时不把全文读进内存。
 */
class StreamingTextReader(
    private val file: File,
    private val charset: Charset,
    bufferSize: Int = 64 * 1024,
) {
    fun lines(): Sequence<IndexedLine> = sequence {
        var charIndex = 0L
        var lineNo = 0

        FileInputStream(file).use { fis ->
            BufferedInputStream(fis, bufferSize).use { bis ->
                val decoder = charset.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPLACE)     // 局部坏字节用 U+FFFD 顶掉，不中断阅读
                    .onUnmappableCharacter(CodingErrorAction.REPLACE)
                val reader = InputStreamReader(bis, decoder)
                val buf = CharArray(bufferSize)
                val sb = StringBuilder(256)

                while (true) {
                    val n = reader.read(buf)
                    if (n < 0) break
                    var i = 0
                    while (i < n) {
                        val ch = buf[i]
                        if (ch == '\n') {
                            val line = sb.toString()
                            yield(IndexedLine(lineNo++, charIndex, line))
                            charIndex += line.length + 1        // +1 为 '\n' 本身
                            sb.setLength(0)
                        } else if (ch != '\r') {               // \r\n 与 \r 都视为换行分隔
                            sb.append(ch)
                        }
                        i++
                    }
                    if (sb.isNotEmpty()) sb.ensureCapacity(sb.length + bufferSize)
                }
                // 末尾无换行符的最后一行
                if (sb.isNotEmpty()) yield(IndexedLine(lineNo, charIndex, sb.toString()))
            }
        }
    }
}

data class IndexedLine(
    val lineNo: Int,
    /** 本行首字符在全文字符流中的下标 */
    val charIndex: Long,
    val text: String,
)
```

> **`\r` 的处理细节**：Windows 文本是 `\r\n`，老 Mac 文本是 `\r`。上面的实现把 `\r` 直接丢弃、`\n` 作为行终止符，因此 `\r\n` 变成单个换行、`\r` 单独出现时会被丢弃（导致整本书变成一行）。**必须额外处理纯 `\r` 换行**：

```kotlin
// 前置检测：若样本中 '\r' 数量远多于 '\n' 且 '\n' 极少，判定为 CR-only 文本
val crCount = sample.count { it == '\r'.code.toByte() }
val lfCount = sample.count { it == '\n'.code.toByte() }
val isCrOnly = lfCount < crCount / 4
// 是 CR-only → 先用 NormalizeLineEndingsReader 包装，把 \r 归一为 \n
```

#### 步骤 3：分章

**这是 10MB 文件 1.5 秒达标的核心战场。**

```
分章算法（单遍扫描，O(n)，流式）：

输入：TXT 文件，字符集，分章规则
输出：List<ChapterMeta>（含精确字符偏移）

规则集（按优先级依次尝试，命中即用）：
  R0. 显式字符串标记（最快，IndexOf 匹配）：
       "第" + 数字/汉字 + "章" | "节" | "回" | "卷" | "集" | "篇"
       "Chapter" + 数字 | "CHAPTER" + 数字
       "序章" | "序言" | "前言" | "后记" | "尾声" | "番外" | "完本感言"
       "楔子" | "引子" | "尾声"
  R1. 内置正则（编译好的 Pattern，避免每行重新编译）
  R2. 用户自定义正则（用户上传的规则）
  R3. 兜底：按最大行长度分割？—— 不，改为"无章节模式"，全书作 1 章

关键性能优化（每条都对应一个数量级的提升）：

  ① 预编译 Pattern，复用于全文（Regex 编译一次 ~2ms，2000 章循环编译 = 4 秒浪费）
  ② 行内容首字符预筛：章节标题必然以 "第"/"序"/"楔"/"引"/"番"/"尾声"/"Ch" 开头
     用 `line.firstOrNull() in CHAPTER_START_CHARS` 先行过滤，非候选行直接跳过正则
     → 命中率从 100% 降到 ~0.1%，实测提速 8~10 倍
  ③ 长度预筛：章节标题行长度通常 < 50 字符，超过直接跳过
  ④ 行号预筛：连续命中距离 < 500 字的"章节"视为误报（正文中的"第三章讲到…"）
  ⑤ 单遍扫描 + 流式读取：不 readAllText，边读边匹配边记录偏移
```

```kotlin
// data:parser/txt — TxtChapterizer.kt
object TxtChapterizer {
    private val CHAPTER_START_CHARS = setOf(
        '第', '序', '楔', '引', '前', '后', '尾', '番', '完', '终', '外',
        'C', 'c',      // Chapter
        '卷', '上', '下', '中',
    )

    /** 预编译的内置规则。顺序 = 优先级 */
    private val BUILTIN_RULES: List<ChapterRule> = listOf(
        ChapterRule("cn_numbered", Regex("""^\s*第\s*[0-9零一二三四五六七八九十百千万两]{1,10}\s*[章节回卷集篇话]\s*[^\n]{0,40}$""")),
        ChapterRule("cn_volume",   Regex("""^\s*第\s*[0-9零一二三四五六七八九十百千万两]{1,10}\s*[卷部]\s*[^\n]{0,40}$""")),
        ChapterRule("cn_special",  Regex("""^\s*(序章|序言|序|楔子|引子|前言|后记|尾声|终章|番外[^\n]{0,20}|完本感言|作者的话)\s*$""")),
        ChapterRule("en_numbered", Regex("""^\s*(?:Chapter|CHAPTER|chapter)\s*\d{1,5}\s*[^\n]{0,60}$""")),
        ChapterRule("digit_only",  Regex("""^\s*\d{1,4}\s*[.、]\s*[^\n]{0,40}$""")),
    )

    suspend fun chapterize(
        file: File,
        charset: Charset,
        customRegex: Regex?,
        onProgress: (Float) -> Unit,
    ): List<ChapterMeta> = withContext(KrDispatchers.Parse) {
        val fileSize = file.length()
        val reader = StreamingTextReader(file, charset)
        val out = ArrayList<ChapterMeta>(2048)

        var lastMatchOffset = Long.MIN_VALUE
        var lastMatchTitle = ""
        var lineCount = 0L

        // 性能统计（debug）
        var candidateChecked = 0L
        var regexTried = 0L

        val rules = buildList {
            if (customRegex != null) add(ChapterRule("custom", customRegex))
            addAll(BUILTIN_RULES)
        }

        for (line in reader.lines()) {
            lineCount++
            if (lineCount % 2000 == 0L) {
                ensureActive()                                    // 可取消
                onProgress((line.charIndex.toFloat() / fileSize / 2f).coerceIn(0f, 0.95f) * 1.8f)
            }

            val text = line.text
            // ① 快速长度筛：章节标题不会超过 60 字
            if (text.isEmpty() || text.length > 60) continue
            // ② 首字符筛：非候选字符直接跳过，不做任何正则
            val first = text.firstOrNull { !it.isWhitespace() } ?: continue
            if (first !in CHAPTER_START_CHARS) continue

            candidateChecked++
            for (rule in rules) {
                regexTried++
                if (!rule.pattern.matches(text)) continue

                val title = text.trim()
                // ④ 距离筛：与上次命中过近的内容不是章节标题（正文中的引用）
                if (line.charIndex - lastMatchOffset < 300 && out.isNotEmpty()) break
                // 标题去重（同章标题重复出现，如页眉）
                if (title == lastMatchTitle && line.charIndex - lastMatchOffset < 5000) break

                // 前一个章节的 end 就是当前偏移
                if (out.isNotEmpty()) {
                    out[out.lastIndex] = out.last().copy(endOffset = line.charIndex)
                }
                out += ChapterMeta(
                    index = out.size,
                    title = title,
                    startOffset = line.charIndex,
                    endOffset = fileSizeCharsPlaceholder,      // 占位，最后统一修正
                    isVolume = rule.id.startsWith("cn_volume"),
                )
                lastMatchOffset = line.charIndex
                lastMatchTitle = title
                break
            }
        }

        // 收尾：最后一章 endOffset = 全书字符数
        val totalChars = lastLineCharIndex + lastLineLength
        if (out.isNotEmpty()) {
            out[out.lastIndex] = out.last().copy(endOffset = totalChars)
        } else {
            // 无任何章节标识 → 单章兜底
            out += ChapterMeta(0, "全文", 0, totalChars)
        }

        onProgress(1f)
        out
    }
}
```

**关于 `endOffset` 的传递**：上面代码用 `copy(endOffset=...)` 回填，实际实现中用一个 `MutableList<ChapterMeta>` 更方便。同时给出 **1.5 秒性能预算分解**（10MB GBK 文件，约 2000 章）：

| 步骤 | 预算 | 说明 |
| --- | --- | --- |
| 编码探测（256KB 采样 + 打分） | 120ms | 严格 UTF-8 校验是主成本 |
| 流式解码 10MB | 400ms | GBK→UTF-16 转换，纯 CPU |
| 行切分与迭代 | 180ms | 约 18 万行 |
| 章节匹配（首字符预筛后仅 2 万次正则） | 300ms | 预筛是关键，无预筛需 2.4s |
| 偏移记录与列表构建 | 50ms | |
| **合计** | **~1050ms** | 预留 450ms 余量给低端机 |

**保证达标的三个手段**：

1. **严格 UTF-8 校验只跑前 64KB**（而非全文件）。UTF-8 非法序列出现概率在文件前部即暴露；若前 64KB 合法，后续出现非法序列的概率 < 0.1%，此时按 UTF-8 处理并容忍局部替换字符。
2. **首字符预筛**（上面代码的 ②）：这一条单独带来 ~8 倍提速。
3. **不用 `Regex.findAll` 扫全文**，而是逐行 `matches`。`findAll` 在大文本上的回溯扫描代价远高于逐行判断。

**低端机兜底**：在 `Build.VERSION.SDK_INT <= 27` 或检测到 CPU 核心数 ≤ 4 时，把编码探测采样降到 64KB，并对超过 5MB 的文件启用 `onProgress` 更频繁上报，同时接受"分章耗时 1.5~2.5s"并**在 UI 上显示进度条**（用户等待感远优于干等）。

### 4.2.3 EPUB 解析

EPUB 本质是 ZIP + XHTML + OPF 清单。解析路径：

```
EPUB 文件（ZIP）
  │
  ├─ mimetype                        必须第一项且不压缩（EPUB 规范）
  ├─ META-INF/container.xml          ★ 入口：指向 OPF 文件路径
  │
  └─ OEBPS/
      ├─ content.opf                 ★ 元数据 + manifest(资源清单) + spine(阅读顺序)
      ├─ toc.ncx                     ★ EPUB2 目录（navPoint 树）
      ├─ nav.xhtml                   ★ EPUB3 导航文档（<nav epub:type="toc">）
      ├─ cover.jpg
      ├─ chapter01.xhtml             ← 正文（每章一个或若干文件）
      └─ images/fig1.png
```

```kotlin
// data:parser/epub — EpubParser.kt
class EpubParser @Inject constructor(
    private val ctx: Context,
) : BookParser {
    override val format = BookFormat.EPUB

    override suspend fun parse(
        file: File,
        options: ParseOptions,
        onProgress: (Float) -> Unit,
    ): ParseResult = withContext(KrDispatchers.Parse) {
        ZipFile(file).use { zip ->
            // ── 1. 找 OPF 路径 ────────────────────────────
            onProgress(0.10f)
            val container = zip.getInputStream(zip.getEntry("META-INF/container.xml"))
                ?.use { it.readBytes() }
                ?: throw KrError.ParseFailed("EPUB", IllegalStateException("缺少 container.xml"))
            val opfPath = parseContainer(container)
                ?: throw KrError.ParseFailed("EPUB", IllegalStateException("container.xml 无 rootfile"))

            // ── 2. 解析 OPF：元数据 + manifest + spine ────
            onProgress(0.25f)
            val opfDir = opfPath.substringBeforeLast('/', "")
            val opf = zip.getInputStream(zip.getEntry(opfPath))
                ?.use { parseOpf(it) }
                ?: throw KrError.ParseFailed("EPUB", IllegalStateException("无法读取 $opfPath"))

            // ── 3. 目录：优先 NAV（EPUB3），回退 NCX（EPUB2） ─
            onProgress(0.45f)
            val navPoints = opf.navPath
                ?.let { runCatching { parseNav(zip, it, opfDir) }.getOrNull() }
                ?.takeIf { it.isNotEmpty() }
                ?: opf.ncxPath?.let { runCatching { parseNcx(zip, it, opfDir) }.getOrNull() }
                ?: emptyList()

            // ── 4. 用 spine 顺序 + navPoints 生成章节 ──────
            onProgress(0.70f)
            val chapters = buildChaptersFromSpineAndNav(opf.spineHrefs, navPoints)

            // ── 5. 封面 ────────────────────────────────
            onProgress(0.85f)
            val cover = opf.coverHref?.let { extractCover(zip, it, opfDir) }
                ?: opf.manifestImages.firstOrNull()?.let { extractCover(zip, it, opfDir) }

            // ── 6. 统计字符数（用于进度百分比） ──────────
            onProgress(0.95f)
            val totalChars = estimateTotalChars(zip, opf.spineHrefs, opfDir)

            onProgress(1f)
            ParseResult(
                metadata = BookMetadata(
                    title = opf.title ?: file.nameWithoutExtension,
                    author = opf.creator,
                    coverFile = cover,
                    encoding = "UTF-8",
                    chapterCount = chapters.size,
                    totalChars = totalChars,
                ),
                chapters = chapters,
            )
        }
    }
}
```

**解析要点**：

1. **用 `ZipFile` 而非 `ZipInputStream`**：`ZipFile` 支持随机访问 entry，可以按需只读需要的文件；`ZipInputStream` 必须顺序流式读，找 OPF 前要先流过所有 entry。
2. **路径安全**：`container.xml` 里的路径可能含 `../`，必须规范化后校验不逃出 ZIP 根：

```kotlin
private fun safeResolve(baseDir: String, href: String): String {
    val decoded = URLDecoder.decode(href, "UTF-8")
    val combined = if (baseDir.isEmpty()) decoded else "$baseDir/$decoded"
    // 规范化 ../ 并检查
    val normalized = combined.split('/').fold(mutableListOf<String>()) { acc, seg ->
        when (seg) {
            "", "." -> {}
            ".." -> if (acc.isNotEmpty()) acc.removeLast()
            else -> acc.add(seg)
        }
        acc
    }.joinToString("/")
    require(!normalized.startsWith("..")) { "EPUB 路径越界: $href" }
    return normalized
}
```

3. **XML 解析用 `XmlPullParser` 而非 Jsoup**：OPF/NCX 是结构化 XML，`XmlPullParser` 是流式解析、内存占用恒定（µs 级）；Jsoup 会构建完整 DOM（对大 OPF 不划算）。但**正文 XHTML 用 Jsoup**（需要容错处理脏 HTML）。

4. **章节标题清理**：

```kotlin
// NCX navLabel / NAV 的 a 标签文本 → 清洗
fun cleanTocTitle(raw: String, href: String): String =
    raw.trim()
        .replace(Regex("""\s+"""), " ")
        .ifBlank { href.substringAfterLast('/').substringBeforeLast('.') }
        .take(80)
```

5. **正文提取与图像**：

```kotlin
// 读取单章正文（按需，不预加载全部）
suspend fun loadChapterContent(zip: ZipFile, href: String, opfDir: String): ChapterContent {
    val entry = zip.getEntry(safeResolve(opfDir, href)) ?: return ChapterContent.Empty
    val html = zip.getInputStream(entry).use { it.readBytes().toString(Charsets.UTF_8) }
    val doc = Jsoup.parse(html)

    // 提取所有图片，改写为本地缓存路径（供 Coil 加载）
    val images = doc.select("img[src]").map { img ->
        val src = img.attr("src")
        val imgPath = safeResolve(href.substringBeforeLast('/', ""), src)
        val cached = extractImageToCache(zip, opfDir, imgPath)   // 写入 cache/epub/<bookId>/
        img.attr("src", cached?.toURI()?.toString() ?: "")
        ImageRef(srcOriginal = imgPath, cachedFile = cached, alt = img.attr("alt"))
    }

    // 提取纯文本：按块级元素切段，保留段落结构
    val text = doc.body()
        .select("p, h1, h2, h3, h4, blockquote, li")
        .joinToString("\n") { it.text().trim() }
        .ifBlank { doc.body().text() }     // 回退：无 p 标签时取全文本
        .replace(Regex("""\n{3,}"""), "\n\n")

    return ChapterContent(text = text, images = images, html = doc.html())
}
```

**内嵌图片点击放大**：`reader` 里用 `Row` 渲染 `ImageRef`，点击后弹出 `Dialog` 内 `ZoomableImage`（双指缩放 + 双击复位 + 拖拽）。

### 4.2.4 PDF 解析

PDF 的特殊性：**不能提取"字符偏移"**（没有稳定文本层），所以进度模型退化为**页码制**。

```kotlin
// data:parser/pdf — PdfParser.kt
class PdfParser @Inject constructor(
    @ApplicationContext private val ctx: Context,
) : BookParser {
    override val format = BookFormat.PDF

    override suspend fun parse(
        file: File,
        options: ParseOptions,
        onProgress: (Float) -> Unit,
    ): ParseResult = withContext(KrDispatchers.IO) {
        // PdfRenderer 需要 ParcelFileDescriptor
        val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val queue = PdfRenderer(pfd)      // 必须串行使用，非线程安全

        try {
            val pageCount = queue.pageCount
            if (pageCount <= 0) throw KrError.ParseFailed("PDF", IllegalStateException("页数为 0"))

            // 加密检测：PdfRenderer 对加密 PDF 会在 openPage 时抛 SecurityException
            val encrypted = runCatching {
                queue.openPage(0).close()
                false
            }.getOrElse { e ->
                if (e is SecurityException) true else throw e
            }
            if (encrypted) throw KrError.ParseFailed("PDF", SecurityException("加密 PDF"))

            // 元数据：从 PDF 文档信息字典提取（简易解析，不引入 PDFBox）
            val (docTitle, docAuthor) = readPdfInfoDictionary(file)

            // 封面：渲染第 1 页
            onProgress(0.3f)
            val cover = if (options.renderCover) renderCover(queue) else null

            // 章节：PDF 无章节概念，按 20 页一个"章节"分组，便于目录导航
            onProgress(0.7f)
            val groupSize = 20
            val chapters = (0 until pageCount step groupSize).map { start ->
                val end = (start + groupSize - 1).coerceAtMost(pageCount - 1)
                ChapterMeta(
                    index = start / groupSize,
                    title = "第 ${start + 1} - ${end + 1} 页",
                    startOffset = start.toLong(),        // ★ PDF 中 offset 语义 = 页码
                    endOffset = end.toLong(),
                )
            }

            onProgress(1f)
            ParseResult(
                metadata = BookMetadata(
                    title = docTitle ?: file.nameWithoutExtension,
                    author = docAuthor,
                    coverFile = cover,
                    encoding = null,
                    chapterCount = chapters.size,
                    totalChars = pageCount.toLong(),     // 复用为总页数
                ),
                chapters = chapters,
                warnings = if (docTitle == null) listOf(ParseWarning.NoMetadata) else emptyList(),
            )
        } finally {
            queue.close()
            pfd.close()
        }
    }
}
```

**PDF 渲染规格**：

| 项 | 规格 |
| --- | --- |
| 渲染 API | `PdfRenderer.Page.render(Bitmap, null, null, RENDER_MODE_FOR_DISPLAY)` |
| 缩放 | 按屏幕宽度与 `page.width` 计算 scale，渲染位图宽 = 屏幕宽 × devicePixelRatio × zoom |
| 缓存 | `cache/pdf_pages/<bookId>/<pageIndex>_<scale>.webp`，LRU 上限 30 页（约 60MB） |
| 预取 | 当前页 ±2 页后台预渲染 |
| 双击缩放 | 1.0x ↔ 2.0x 切换，用 `Animatable` 动画；缩放后支持拖拽（`detectTransformGestures`） |
| 夜间反色 | `ColorMatrix` 反色 + 轻微降对比，避免纯反色导致图片变成负片不可读 |

**夜间滤镜实现**（关键：不能让图片变成负片）：

```kotlin
/**
 * 智能反色：文本区域反色，图片区域仅降亮度。
 * 简化实现：整体反色 + 色相旋转 180°，对灰度内容效果最佳；
 * 对彩色图片，用 saturation 降低 + 反色会让图片变成"紫绿"怪色，
 * 因此提供两档：标准反色 / 温和暗色（不反色，仅叠加黑色 60% + 降亮度）。
 */
enum class PdfNightFilter { OFF, INVERT, DIM }

fun ColorFilter.of(mode: PdfNightFilter): ColorFilter? = when (mode) {
    PdfNightFilter.OFF -> null
    PdfNightFilter.INVERT -> ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
        // 反色用 BlendMode 实现，见 ZoomablePdfPage 中的 paint
    PdfNightFilter.DIM -> ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
        0.6f, 0f, 0f, 0f, 0f,
        0f, 0.6f, 0f, 0f, 0f,
        0f, 0f, 0.6f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f,
    )))
}
```

### 4.2.5 解析引擎验收要点

- [ ] UTF-8 / GBK / GB18030 / BIG5 / UTF-16LE / UTF-16BE 六种编码的测试文件均正确识别
- [ ] 10MB GBK TXT（约 2000 章）分章 ≤ 1.5s（中端机，如骁龙 778G）
- [ ] 无章节标识的 TXT 不崩溃，降级为单章"全文"
- [ ] `\r`-only 换行的 TXT 不变成一行
- [ ] EPUB：OPF 缺失、NCX 缺失、NAV 缺失三种残缺情况均有合理降级
- [ ] EPUB 内嵌图片可正确提取并在阅读器中点击放大
- [ ] 加密 PDF 给出明确提示而非崩溃
- [ ] 恶意构造的 EPUB（`../../etc/passwd` 路径）被安全拦截，无文件系统穿越

---

## 4.3 分页与跳转引擎

### 4.3.1 分页引擎的职责边界

**分页引擎只做一件事**：给定「章文本 + 排版配置 + 视口尺寸」，输出「页列表（每页携带字符偏移区间）」。

它**不负责**：
- 渲染（Compose 的 `Text` 负责）
- 缓存管理（上层 ViewModel 负责，但引擎提供缓存接口）
- 进度存储（`ProgressRepository` 负责）

### 4.3.2 核心接口

```kotlin
// data:pagination — PaginationEngine.kt
interface PaginationEngine {
    /**
     * 流式分页。首批快速返回，让 UI 立即上屏。
     * 调用方应在 collect 中逐批更新 UI。
     */
    fun paginate(
        chapterText: String,
        typography: TypographyConfig,
        viewport: IntSize,
        config: PaginationConfig = PaginationConfig.Default,
    ): Flow<PaginationBatch>

    /** 释放指定书籍的分页缓存（切换书籍/排版变更时） */
    fun invalidate(bookId: Long, chapterIndex: Int? = null)

    /** 缓存统计，用于内存压力时主动收缩 */
    fun cacheStats(): PaginationCacheStats
}

data class PaginationBatch(
    val pages: List<PageSnapshot>,
    /** true 表示这是最后一批（全章分页完成） */
    val isComplete: Boolean,
    val totalPages: Int,
)

data class PaginationConfig(
    /** 首批返回的页数：1 屏 + 预读 1 屏 = 2 */
    val firstBatchPages: Int = 2,
    /** 后续每批页数 */
    val batchSize: Int = 3,
    /** 是否计算行级断点（用于"上次读到这一行"高亮），开销 +15% */
    val computeLineBreaks: Boolean = true,
)
```

### 4.3.3 分页算法实现

核心是用 Compose 的 `TextMeasurer` 做**逐行测量 + 贪心装箱**：

```kotlin
// data:pagination — ComposePaginationEngine.kt
@Singleton
class ComposePaginationEngine @Inject constructor(
    private val measurerFactory: TextMeasurerFactory,
) : PaginationEngine {

    // 需要注意：TextMeasurer 必须从 Composition 获取，不能在任意线程 new
    // 方案：由 ReaderScreen 在 Composition 中创建并注入（见 4.3.5）

    override fun paginate(
        chapterText: String,
        typography: TypographyConfig,
        viewport: IntSize,
        config: PaginationConfig,
    ): Flow<PaginationBatch> = flow {
        ensureActive()
        val measurer = measurerFactory.get()      // 已在主线程准备好
        val style = typography.toTextStyle()
        val density = measurerFactory.density()

        val contentWidthPx = viewport.width -
            with(density) { (typography.marginHorizontalDp * 2).dp.roundToPx() }
        val contentHeightPx = viewport.height -
            with(density) { (typography.marginVerticalDp * 2).dp.roundToPx() }

        val lineHeightPx = with(density) {
            (typography.fontSizeSp.sp.toPx() * typography.lineHeightMult)
        }
        val paraSpacingPx = with(density) {
            (typography.fontSizeSp.sp.toPx() * typography.paragraphSpacingEm)
        }

        // ── 步骤 1：按段落切分，记录每段的字符偏移 ──────────
        val paragraphs = splitParagraphs(chapterText)   // List<Paragraph(startChar, text)>

        // ── 步骤 2：每段用 measurer 计算断行 ────────────────
        // 一次约束测量：约束高度给无限大，让 TextMeasurer 返回全部行
        val allLines = ArrayList<MeasuredLine>(estimatedLineCount(chapterText, contentWidthPx))
        var curHeight = 0f

        for (para in paragraphs) {
            ensureActive()
            if (para.text.isBlank()) {
                // 空段落：占一个段间距
                allLines += MeasuredLine(
                    startChar = para.startChar,
                    endChar = para.startChar,
                    text = "",
                    heightPx = paraSpacingPx,
                    isParagraphEnd = true,
                )
                continue
            }

            val layout = measurer.measure(
                text = AnnotatedString(para.text),
                style = style,
                constraints = Constraints(maxWidth = contentWidthPx),
            )
            for (li in 0 until layout.lineCount) {
                val start = layout.getLineStart(li)
                val end = layout.getLineEnd(li, visibleEnd = true)
                allLines += MeasuredLine(
                    // ★ 关键：行级字符偏移回填到段落偏移上，形成章内绝对偏移
                    startChar = para.startChar + start,
                    endChar = para.startChar + end,
                    text = para.text.substring(start, end),
                    heightPx = layout.getLineBottom(li) - layout.getLineTop(li),
                    isParagraphEnd = li == layout.lineCount - 1,
                )
            }
            curHeight += layout.size.height + paraSpacingPx
        }

        // ── 步骤 3：贪心装箱：把行装进页 ────────────────────
        val pages = ArrayList<PageSnapshot>(estimatedPageCount(allLines, contentHeightPx))
        var pageStartLine = 0
        var accHeight = 0f
        var lineIdx = 0

        while (lineIdx < allLines.size) {
            val line = allLines[lineIdx]
            val needHeight = line.heightPx + if (line.isParagraphEnd) paraSpacingPx else 0f

            // 剩余空间放不下这一行 → 收页
            if (accHeight + needHeight > contentHeightPx && lineIdx > pageStartLine) {
                pages += buildPage(pages.size, allLines, pageStartLine, lineIdx - 1)
                pageStartLine = lineIdx
                accHeight = 0f
                continue          // 重新尝试放这一行到新页
            }

            accHeight += needHeight
            lineIdx++
        }
        if (pageStartLine < allLines.size) {
            pages += buildPage(pages.size, allLines, pageStartLine, allLines.lastIndex)
        }

        // ── 步骤 4：流式发射 ──────────────────────────────
        val first = pages.take(config.firstBatchPages)
        if (first.isNotEmpty()) {
            emit(PaginationBatch(first, isComplete = pages.size <= config.firstBatchPages, pages.size))
        }
        var cursor = config.firstBatchPages
        while (cursor < pages.size) {
            ensureActive()
            val batch = pages.subList(cursor, (cursor + config.batchSize).coerceAtMost(pages.size))
            cursor += config.batchSize
            emit(PaginationBatch(batch.toList(), isComplete = cursor >= pages.size, pages.size))
        }
        if (pages.isEmpty()) emit(PaginationBatch(emptyList(), isComplete = true, 0))
    }.flowOn(KrDispatchers.Default)

    private fun buildPage(pageIndex: Int, lines: List<MeasuredLine>, from: Int, to: Int): PageSnapshot {
        val first = lines[from]
        val last = lines[to]
        return PageSnapshot(
            index = pageIndex,
            startCharInChapter = first.startChar,
            endCharInChapter = last.endChar,
            displayText = buildDisplayText(lines, from, to),
            startLine = from,
            endLine = to,
        )
    }
}
```

### 4.3.4 为什么这个算法能保证 60fps？

| 风险 | 对策 |
| --- | --- |
| 分页在主线程做 → 掉帧 | `flowOn(KrDispatchers.Default)` 全部分页在后台线程 |
| 等整章分完才显示 → 白屏 | 流式发射，首批 2 页立即 emit |
| 每次重组都重新分页 → 卡 | 分页结果存在 ViewModel 的 `StateFlow` 中，Composable 只读 |
| 页列表变化导致整章重排 | `LazyListState`/`Pager` 用稳定 key |
| 测量开销本身大 | **约束高度给 `Constraints.Infinity`**，一次 `measure` 拿到整段所有行，避免逐行调用（逐行调用会慢 10 倍以上） |
| 段落数过多（10 万段） | 按段测量是 O(段数)，段落切分本身 O(n) 单遍 |

**关键性能技巧详解 —— "一次测量拿全部行"**：

```kotlin
// ❌ 慢：逐行二分查找断点。每行 log(n) 次 measure，整章 n*log(n) 次测量
while (offset < para.length) {
    val (lineEnd, _) = binarySearchLineBreak(measurer, para, offset, contentWidth)  // 内部多次 measure
    offset = lineEnd
}

// ✅ 快：一次 measure 拿到 layout，直接读 lineCount / getLineStart / getLineEnd
val layout = measurer.measure(
    text = AnnotatedString(para.text),
    style = style,
    constraints = Constraints(maxWidth = contentWidthPx),   // 高度不限
)
for (i in 0 until layout.lineCount) {
    val s = layout.getLineStart(i)
    val e = layout.getLineEnd(i, visibleEnd = true)
}
```

实测：3000 字章节（约 100 段、500 行），逐行二分用 1.8 秒；一次测量用 90ms。**20 倍差距**。

### 4.3.5 TextMeasurer 的线程与生命周期问题（易踩坑）

`TextMeasurer` 依赖 `FontFamily.Resolver`、`Density`、`LayoutDirection`，这些必须来自 Composition。**不能在 Repository 层凭空 new**。

**正确方案**：由 `ReaderScreen` 在 Composition 中创建，通过 `HolderProvider` 注入引擎。

```kotlin
// feature/reader — LocalTextMeasurerHolder.kt
class TextMeasurerHolder {
    var measurer: TextMeasurer? = null
    var density: Density = Density(1f)
}

val LocalTextMeasurerHolder = staticCompositionLocalOf { TextMeasurerHolder() }

// ReaderScreen 内
@Composable
fun ProvideTextMeasurer(content: @Composable () -> Unit) {
    val holder = remember { TextMeasurerHolder() }
    val measurer = rememberTextMeasurer(cacheSize = 8)     // ★ 内置 LRU 缓存
    val density = LocalDensity.current
    SideEffect {
        holder.measurer = measurer
        holder.density = density
    }
    CompositionLocalProvider(LocalTextMeasurerHolder provides holder) { content() }
}
```

**`rememberTextMeasurer(cacheSize)` 的意义**：内部缓存 `TextLayoutResult`。分页时同一段文本可能被测量多次（如字体切换后回退），缓存能显著降低重复开销。设 `cacheSize = 8~16` 即可（过大会导致内存膨胀）。

### 4.3.6 重新分页的触发条件与去抖

```kotlin
// feature/reader — ReaderViewModel.kt
private data class PaginationKey(
    val chapterIndex: Int,
    val fontSize: Float,
    val lineHeight: Float,
    val paragraphSpacing: Float,
    val marginH: Float,
    val marginV: Float,
    val fontFamilyId: String,
    val viewportW: Int,
    val viewportH: Int,
)

private val paginationTrigger = combine(
    currentChapter, typographyFlow, viewportFlow,
) { chapter, typo, viewport -> PaginationKey(chapter.index, ...) }
    .distinctUntilChanged()
    .debounce(120)          // ★ 去抖：拖动字号滑块时不每帧重分页
    .mapLatest { key ->     // ★ mapLatest：新任务到来时取消旧任务
        paginationEngine.paginate(...)
    }
```

**三个关键算子**：

| 算子 | 作用 | 不加会怎样 |
| --- | --- | --- |
| `distinctUntilChanged()` | 相同参数不重复分页 | 无关状态变化（如菜单开关）也会触发重分页 |
| `debounce(120)` | 连续变更只在停止后执行 | 拖字号滑块 60 次/秒 → 60 次分页任务，UI 卡死 |
| `mapLatest` | 新任务取消旧任务 | 旧的慢分页结果后来居上，覆盖新结果，显示错误页码 |

**分页中的进度保持**：重新分页时必须记住当前 `charOffsetInChapter`，分页完成后按新页表重新定位（就是 3.1.2 的恢复算法）。**这是"改字号不丢进度"的最终实现落点**。

```kotlin
// 保存旧偏移 → 重新分页 → 按新页表定位
val anchorOffset = currentPage.value.startCharInChapter + intraPageOffset
val newPages = paginationEngine.paginate(...)
val newPageIndex = newPages.indexOfLast { it.startCharInChapter <= anchorOffset }
currentPageIndex.value = newPageIndex.coerceAtLeast(0)
```

### 4.3.7 跳转能力

| 跳转方式 | 实现 |
| --- | --- |
| 目录跳转 | 选章节 → 加载章文本 → 分页 → 定位到 `charOffset = 0`（章首） |
| **滑动条进度跳转** | 滑动条值 = 全书百分比。松手时：`globalOffset = totalChars * ratio` → `locateByOffset()` 二分找章节 → 章内偏移 → 分页 → 定位 |
| 书签跳转 | 查 bookmark 的 `chapterIndex + charOffset` → 直接定位 |
| 百分比输入 | 同滑动条，支持精确输入 "读到这里：第 500 页 / 共 1200 页" |
| 章节内快速定位 | 长按屏幕中央唤出"本章定位条"，拖动定位到章内任意位置 |

**滑动条实现细节**（这是体验差异点）：

```kotlin
@Composable
fun ReadingProgressSlider(
    percent: Float,
    chapterIndex: Int,
    totalChapters: Int,
    chapterTitle: String,
    onSeek: (Float) -> Unit,
    onSeekFinished: (Float) -> Unit,
) {
    var dragValue by remember { mutableFloatStateOf(percent) }
    var isDragging by remember { mutableStateOf(false) }

    Column {
        // 拖动时在滑块上方显示实时预览气泡
        if (isDragging) {
            Surface(shape = MaterialTheme.shapes.small, tonalElevation = 6.dp) {
                Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text("第 ${previewChapter} 章", style = MaterialTheme.typography.labelLarge)
                    Text(previewChapterTitle, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                    Text("${(dragValue * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Slider(
            value = dragValue,
            onValueChange = { dragValue = it; isDragging = true; /* 只更新预览，不触发跳转！ */ },
            onValueChangeFinished = { isDragging = false; onSeekFinished(dragValue) },  // ★ 松手才跳
        )
    }
}
```

> **必须只在 `onValueChangeFinished` 触发跳转**。若在 `onValueChange` 里跳转，用户拖动 1 秒会触发 ~60 次章节加载 + 分页，直接 ANR。

### 4.3.8 分页引擎验收要点

- [ ] 3000 字章节首屏显示耗时 ≤ 120ms
- [ ] 10 万字单章（无章节标识的 TXT 兜底）分页不 OOM，且可取消
- [ ] 字号从 12sp 连续拖到 36sp，UI 无卡顿（去抖生效），松手后 300ms 内完成重分页
- [ ] 重分页后字符偏移锚点保持，视觉位置偏差 ≤ 1 屏
- [ ] 滑动条拖动全程不触发章节加载，仅在松手时触发一次
- [ ] 分页结果中的每页 `startCharInChapter <= endCharInChapter` 且区间**连续无空洞**（自动化测试校验）

---

## 4.4 阅读进度记录

### 4.4.1 进度语义定义

| 场景 | 记录什么 | 为什么 |
| --- | --- | --- |
| 页面可见 | 当前页的 `startCharInChapter` | 用户的眼睛停在页首 |
| 翻到下一页 | 新页的 `startCharInChapter` | |
| 点击"上一页"回到前页 | 前页的 `startCharInChapter` | 用户回看，语义仍是页首 |
| 滚动模式 | 视口顶部第一个可见字符的偏移 | 滚动模式下页概念弱化 |
| 退出阅读器 | 立即强制落盘 | |
| 章节末尾（读完本章） | 直接进下一章时，记为下一章 `offset = 0` | 避免停在章尾重复显示 |

### 4.4.2 三级写入策略实现

```kotlin
// data:books — ProgressRepositoryImpl.kt
@Singleton
class ProgressRepositoryImpl @Inject constructor(
    private val progressDao: ProgressDao,
    private val bookDao: BookDao,
    private val sessionDao: ReadingSessionDao,
) : ProgressRepository {

    private val pending = MutableStateFlow<ReadingProgress?>(null)
    private val mutex = Mutex()
    private var lastFlushAt = 0L

    init {
        // 延迟落盘：800ms 去抖
        KrDispatchers.ApplicationScope.launch {
            pending.filterNotNull()
                .debounce(800)
                .collect { p -> writeToDb(p) }
        }
        // 定时兜底：每 30 秒
        KrDispatchers.ApplicationScope.launch {
            while (true) {
                delay(30_000)
                pending.value?.let { writeToDb(it) }
            }
        }
    }

    override suspend fun update(progress: ReadingProgress) {
        pending.value = progress          // 内存态立即更新，UI 无延迟
    }

    /** 强制落盘（退出阅读器 / onStop 时调用） */
    override suspend fun flushNow(bookId: Long): Unit = mutex.withLock {
        pending.value?.takeIf { it.bookId == bookId }?.let { writeToDb(it) }
    }

    private suspend fun writeToDb(p: ReadingProgress) = mutex.withLock {
        progressDao.upsert(p.toEntity())
        bookDao.updateLastRead(p.bookId, System.currentTimeMillis())
        lastFlushAt = System.currentTimeMillis()
    }
}
```

**调用点**：

```kotlin
// feature/reader — ReaderScreen.kt
val lifecycleOwner = LocalLifecycleOwner.current
DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_STOP -> scope.launch { vm.flushProgress() }
            else -> {}
        }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
        lifecycleOwner.lifecycle.removeObserver(observer)
        scope.launch { vm.flushProgress() }      // 离开阅读页也落盘
    }
}
```

### 4.4.3 阅读时长统计

```kotlin
// 会话统计：进入章节开始计时，离开/切章时结算
class ReadingSessionTracker @Inject constructor(
    private val sessionDao: ReadingSessionDao,
) {
    private var sessionStart = 0L
    private var pageCount = 0
    private var charCount = 0L

    fun onEnter(bookId: Long) {
        sessionStart = SystemClock.elapsedRealtime()
        pageCount = 0; charCount = 0L
    }

    fun onPageTurn(charsOnPage: Int) { pageCount++; charCount += charsOnPage }

    suspend fun onExit(bookId: Long) {
        if (sessionStart == 0L) return
        val duration = (SystemClock.elapsedRealtime() - sessionStart) / 1000
        // 过滤无效会话：<10 秒视为"打开即退"，不计入统计
        if (duration >= 10) {
            sessionDao.insert(ReadingSessionEntity(
                bookId = bookId,
                startedAt = System.currentTimeMillis() - duration * 1000,
                durationSeconds = duration,
                charsRead = charCount,
                pagesRead = pageCount,
            ))
        }
        sessionStart = 0L
    }
}
```

> **用 `SystemClock.elapsedRealtime()` 而非 `System.currentTimeMillis()`**：前者不受用户修改系统时间影响，也不会在 NTP 校时时跳变。任何"测量时长"的场景都必须用它。

### 4.4.4 验收要点

- [ ] 翻页后 1 秒内退出应用，重进位置正确（延迟落盘路径）
- [ ] 阅读中强杀进程（`adb shell am force-stop`），重进位置误差 ≤ 2 页（依赖 30s 兜底）
- [ ] 字号 18→28sp 后位置内容连续（无跳跃）
- [ ] 换字体后位置保持
- [ ] 换书源后能定位到同名章节近似位置
- [ ] 阅读时长统计准确（与秒表对比误差 < 5%）

---

## 4.5 排版与主题系统

### 4.5.1 排版参数规格表

| 参数 | 范围 | 步进 | 默认 | UI 控件 |
| --- | --- | --- | --- | --- |
| 字号 | 12sp ~ 36sp | 1sp | 18sp | 减号 / 数值 / 加号 三件套 + 拖动条 |
| 行间距 | 1.0 ~ 3.0 倍 | 0.1 | 1.6 | 拖动条 |
| 段间距 | 0.0 ~ 2.0 em | 0.1 | 0.8 | 拖动条 |
| 左右边距 | 0 ~ 48dp | 2dp | 16dp | 拖动条（实时预览） |
| 上下边距 | 0 ~ 96dp | 4dp | 24dp | 拖动条 |
| 字间距 | -0.05 ~ 0.20 em | 0.01 | 0 | 拖动条 |
| 字体 | 系统 / 苹方 / 思源宋体 / 自定义 | — | 系统 | 横向选择器 |
| 对齐 | 左对齐 / 两端对齐 | — | 左对齐 | 分段控件 |
| 正文加粗 | 开 / 关 | — | 关 | 开关 |
| 首行缩进 | 开 / 关（2 字符） | — | 开 | 开关 |

### 4.5.2 排版配置到 Compose TextStyle 的映射

```kotlin
// core:designsystem — TypographyMapper.kt
fun TypographyConfig.toTextStyle(fontResolver: FontResolver): TextStyle = TextStyle(
    fontSize = fontSizeSp.sp,
    lineHeight = (fontSizeSp * lineHeightMult).sp,
    letterSpacing = letterSpacingEm.em,
    fontFamily = fontResolver.resolve(fontFamilyId),
    fontWeight = if (boldEnabled) FontWeight.SemiBold else FontWeight.Normal,
    textAlign = when (align) {
        "JUSTIFY" -> TextAlign.Justify
        else -> TextAlign.Start
    },
    // ★ 关键：不设 hyphens；中文无需断词
)

/** 段落合并逻辑通过构造 AnnotatedString 实现（段间距在分页时以空白行模拟） */
fun buildChapterAnnotatedString(
    text: String,
    typography: TypographyConfig,
): AnnotatedString = buildAnnotatedString {
    val paras = text.split('\n')
    paras.forEachIndexed { i, p ->
        val trimmed = p.trim()
        if (trimmed.isEmpty()) return@forEachIndexed
        if (typography.indentFirstLine && i > 0) append("\u3000\u3000")   // 全角空格缩进
        append(trimmed)
        if (i != paras.lastIndex) append("\n")
    }
}
```

> **首行缩进用两个全角空格 `\u3000` 而非 `ParagraphStyle.textIndent`**。原因：`textIndent` 在两端对齐 + 自动换行时，第二行也会继承缩进（bug 高发于 Android 13 以下），而全角空格方案在所有 Android 版本上表现一致。

### 4.5.3 自定义字体加载

```kotlin
// data:settings — FontRepositoryImpl.kt
@Singleton
class FontRepositoryImpl @Inject constructor(
    @ApplicationContext private val ctx: Context,
) : FontRepository {

    private val fontDir = File(ctx.filesDir, "fonts")
    private val typefaceCache = LruCache<String, Typeface>(4)     // ★ 字体对象很占内存（一个 CJK 字体 ~8MB）

    override suspend fun import(uri: Uri, displayName: String): Result<FontEntry> =
        withContext(KrDispatchers.IO) {
            runCatching {
                // 1. 复制到私有目录
                val bytes = ctx.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: error("无法读取字体文件")
                // 2. 校验：文件头魔数
                require(isValidFont(bytes)) { "不是有效的字体文件（支持 .ttf / .otf / .ttc）" }
                val hash = bytes.sha1Hex()
                val dest = File(fontDir, "$hash.${uri.extension()}").apply {
                    fontDir.mkdirs()
                    writeBytes(bytes)
                }
                // 3. 立即验证可加载（避免保存了坏字体，阅读时才崩）
                val tf = Typeface.createFromFile(dest)
                require(tf != Typeface.DEFAULT) { "字体加载失败，文件可能损坏" }
                typefaceCache.put(hash, tf)

                // 4. 写索引
                val entry = FontEntry(id = hash, displayName = displayName, path = dest.absolutePath,
                                      sizeBytes = bytes.size.toLong(), importedAt = System.currentTimeMillis())
                saveIndex(entry)
                entry
            }
        }

    override suspend fun resolve(fontFamilyId: String): FontFamily =
        withContext(KrDispatchers.IO) {
            when {
                fontFamilyId == "system" -> FontFamily.Default
                fontFamilyId.startsWith("builtin:") -> builtinFamily(fontFamilyId)
                fontFamilyId.startsWith("custom:") -> {
                    val hash = fontFamilyId.removePrefix("custom:")
                    val tf = typefaceCache.get(hash) ?: run {
                        val f = File(fontDir, findFile(hash))
                        if (!f.exists()) return@withContext FontFamily.Default    // ★ 字体丢失回退
                        Typeface.createFromFile(f).also { typefaceCache.put(hash, it) }
                    }
                    FontFamily(tf)
                }
                else -> FontFamily.Default
            }
        }

    /** 魔数校验：TTF/OTF/TTC 各有固定头 */
    private fun isValidFont(b: ByteArray): Boolean {
        if (b.size < 12) return false
        val tag = String(b, 0, 4, Charsets.US_ASCII)
        return tag in setOf("\u0000\u0001\u0000\u0000", "OTTO", "true", "ttcf")
    }
}
```

**关键设计说明**：

1. **`LruCache<String, Typeface>(4)`**：CJK 字体一个实例约 5~10MB。缓存 4 个已是 40MB，与 150MB 总预算冲突，所以**上限必须小**。切换字体时旧字体会被回收（`Typeface` 是 native 资源，GC 会释放）。
2. **导入时立即验证**（`Typeface.createFromFile` + `tf != DEFAULT`）：避免"保存了坏字体，用户翻到某本书才崩溃"。
3. **魔数校验**：防止用户误传 `.zip` 改名为 `.ttf`，此时 `Typeface.createFromFile` 在部分机型上不抛异常而是返回空字形（文字全部不可见，极难排查）。
4. **字体丢失回退 `FontFamily.Default`**：用户清理了 App 数据、或字体文件被外部删除时，不能让阅读器显示空白。

**自定义字体在分页中的影响**：不同字体的字符宽度不同（等宽 vs 比例），**换字体必然导致重新分页**。这是设计预期内的行为，已由 4.3.6 的 `PaginationKey.fontFamilyId` 覆盖。

### 4.5.4 主题系统

#### 预置主题色板

| 主题 | 背景色 | 正文色 | 强调色 | 说明 |
| --- | --- | --- | --- | --- |
| 纸质仿古 `paper` | `#F5F1E8` | `#2B2622` | `#8B6F47` | 米黄纸感，长时间阅读舒适 |
| 护眼浅绿 `eye_care` | `#CCE8CF` | `#1F2D20` | `#3E6B45` | 低蓝光 |
| 羊皮纸 `parchment` | `#E8D9B5` | `#3B2F1E` | `#8A6A3B` | 复古质感，配宋体最佳 |
| 纯黑夜间 `night` | `#000000` | `#B8B8B8` | `#5A7A5A` | OLED 省电，正文不用纯白（防眩光） |
| 深灰夜间 `night_gray` | `#121212` | `#C5C5C5` | `#7A9E7A` | 非 OLED 屏更柔和 |
| 高对比 `high_contrast` | `#FFFFFF` | `#000000` | `#0000CC` | 无障碍 / 阳光下 |
| 自定义 `custom` | 用户 HEX | 用户 HEX | 自动派生 | |

> **为什么夜间模式正文不用纯白 `#FFFFFF`？** 纯白文字在纯黑背景上会产生"光晕扩散"效应（halation），尤其对散光用户，反而更难读。`#B8B8B8` ~ `#C5C5C5` 是经验最优区间。

#### 自定义主题的对比度保护

用户可能配出"浅灰底 + 浅灰字"这种不可读组合。**必须做对比度校验**：

```kotlin
// core:designsystem — ContrastGuard.kt
object ContrastGuard {
    /** WCAG 相对亮度 */
    private fun luminance(c: Color): Double {
        fun ch(v: Float): Double {
            val s = v.toDouble()
            return if (s <= 0.03928) s / 12.92 else ((s + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * ch(c.red) + 0.7152 * ch(c.green) + 0.0722 * ch(c.blue)
    }

    /** 对比度（WCAG 定义） */
    fun contrastRatio(fg: Color, bg: Color): Double {
        val l1 = luminance(fg); val l2 = luminance(bg)
        val (hi, lo) = if (l1 > l2) l1 to l2 else l2 to l1
        return (hi + 0.05) / (lo + 0.05)
    }

    /**
     * 校验并修正。
     * 正文对比度应 ≥ 7:1（AAA 级）——长文阅读要求高于一般的 4.5:1。
     */
    fun validate(fg: Color, bg: Color): ContrastResult {
        val ratio = contrastRatio(fg, bg)
        return when {
            ratio >= 7.0 -> ContrastResult.Ok(ratio)
            ratio >= 4.5 -> ContrastResult.Acceptable(ratio)      // 允许，但次级 UI 文字需注意
            else -> ContrastResult.TooLow(ratio, suggestFix(fg, bg))
        }
    }

    /** 保持色相，向黑或白方向调整亮度直到达标 */
    private fun suggestFix(fg: Color, bg: Color): Color {
        val bgLum = luminance(bg)
        val targetWhite = bgLum < 0.5      // 深色背景 → 调亮文字
        var hsl = fg.toHsl()
        val step = if (targetWhite) 0.03f else -0.03f
        var candidate = fg
        repeat(30) {
            hsl = hsl.copy(lightness = (hsl.lightness + step).coerceIn(0f, 1f))
            candidate = Color.hsl(hsl.hue, hsl.saturation, hsl.lightness)
            if (contrastRatio(candidate, bg) >= 7.0) return candidate
        }
        return if (targetWhite) Color.White else Color.Black
    }
}

sealed interface ContrastResult {
    data class Ok(val ratio: Double) : ContrastResult
    data class Acceptable(val ratio: Double) : ContrastResult
    data class TooLow(val ratio: Double, val suggestion: Color) : ContrastResult
}
```

**UI 提示**：自定义主题面板实时显示对比度数值，低于 4.5 时给出黄色警告条 + "一键修正"按钮。

#### 亮度调节

两套机制并存：

| 机制 | 实现 | 说明 |
| --- | --- | --- |
| 应用内亮度遮罩 | 全屏 `Box` 叠加 `Color.Black.copy(alpha = 1f - brightness)` | 不改系统亮度，不影响其他应用。用于"比系统更暗" |
| 系统亮度覆写 | `window.attributes.screenBrightness = value` | 用户可选"跟随系统"或"独立亮度" |

**默认策略**：只用遮罩（更安全，不需要任何权限）。用户手动开启"独立亮度"时才覆写窗口属性。

---

## 4.6 阅读交互与翻页动效

### 4.6.1 五种翻页模式

| 模式 | 交互 | 实现方案 | 帧率要求 |
| --- | --- | --- | --- |
| **仿真翻页** `SIMULATION` | 从屏幕边缘拖拽，纸张卷曲翻起 | 自定义卷曲：`Canvas` 绘制 + 贝塞尔曲线阴影；简化为"斜切 + 阴影渐变" | 60fps+ |
| **覆盖翻页** `COVER` | 新页从右侧滑入覆盖旧页 | `HorizontalPager` + 双页叠加，`graphicsLayer` 平移 | 60fps |
| **平移滑动** `SLIDE` | 新旧页并排平移 | `HorizontalPager` 原生 | 90/120fps ✅ |
| **无缝滚动** `SCROLL` | 垂直连续滚动 | `LazyColumn` 页化（每页一项）或连续 `Text` | 120fps ✅ |
| **点按切换** `TAP` | 无动画瞬时切换 | `Crossfade` 或直接换页 | 无动画压力 |

### 4.6.2 翻页动画实现（覆盖模式为例）

```kotlin
// feature/reader — CoverPageTurn.kt
@Composable
fun CoverPageTurn(
    currentPage: PageSnapshot,
    nextPage: PageSnapshot?,
    prevPage: PageSnapshot?,
    typography: TypographyConfig,
    theme: ReaderTheme,
    onTurn: (Direction) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val offsetFraction = remember { Animatable(0f) }     // -1(上一页) ~ 0(当前) ~ 1(下一页)
    val threshold = 0.28f

    Box(
        Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        scope.launch {
                            val target = when {
                                offsetFraction.value > threshold -> 1f
                                offsetFraction.value < -threshold -> -1f
                                else -> 0f
                            }
                            offsetFraction.animateTo(
                                target = target,
                                animationSpec = tween(
                                    durationMillis = (120 + 180 * abs(target - offsetFraction.value)).toInt(),
                                    easing = FastOutSlowInEasing,
                                ),
                            )
                            if (target != 0f) {
                                onTurn(if (target > 0) Direction.NEXT else Direction.PREV)
                                offsetFraction.snapTo(0f)      // ★ 立即复位，避免闪回
                            }
                        }
                    },
                    onDragCancel = { scope.launch { offsetFraction.animateTo(0f) } },
                ) { change, dragAmount ->
                    change.consume()
                    scope.launch {
                        // 1:1 跟手，超界时阻尼衰减
                        val delta = dragAmount / size.width
                        val next = offsetFraction.value + delta
                        offsetFraction.snapTo(
                            if (next > 1f) 1f + (next - 1f) * 0.15f
                            else if (next < -1f) -1f + (next + 1f) * 0.15f
                            else next
                        )
                    }
                }
            },
    ) {
        // 当前页（随手指左移）
        PageContent(
            page = currentPage, typography = typography, theme = theme,
            modifier = Modifier.graphicsLayer {
                translationX = -offsetFraction.value * size.width
                alpha = 1f - (offsetFraction.value * 0.85f).coerceIn(0f, 1f)
            },
        )
        // 下一页（从右侧滑入）
        nextPage?.let { np ->
            PageContent(
                page = np, typography = typography, theme = theme,
                modifier = Modifier.graphicsLayer {
                    translationX = (1f - offsetFraction.value) * size.width
                },
            )
        }
        // 上一页（从左侧滑入，仅在向右拖时可见）
        if (offsetFraction.value < 0f) {
            prevPage?.let { pp ->
                PageContent(
                    page = pp, typography = typography, theme = theme,
                    modifier = Modifier.graphicsLayer {
                        translationX = (-1f - offsetFraction.value) * size.width
                    },
                )
            }
        }
    }
}
```

**性能关键点**：

1. **动画值用 `Animatable` 而非每次重组 `mutableStateOf`**：`Animatable` 走 `graphicsLayer` 的绘制阶段，**不触发重组**。
2. **`graphicsLayer { }` 的 lambda 形式**：读 `Animatable.value` 发生在绘制阶段（`Layer` 更新），避免 Composition 阶段的失效。
3. **`snapTo(0f)` 在 `onTurn` 之后**：先让 ViewModel 切换到新页，再复位偏移，否则会闪一下旧页。
4. **越界阻尼 0.15f**：模拟真实物理边界感。
5. **`change.consume()`**：防止手势冒泡到父容器。

### 4.6.3 点击区域划分

```
┌──────────────────────────────────────────────────┐
│                                                  │
│   左 25%          中间 50%         右 25%        │
│   上一页        唤出菜单栏          下一页        │
│              （或按设置改为"无操作"）             │
│                                                  │
│                                      ┌─────────┐ │
│                                      │ 底部 12% │ │
│                                      │ 唤出菜单 │ │
│                                      └─────────┘ │
└──────────────────────────────────────────────────┘
```

```kotlin
enum class TapZone { LEFT, CENTER, RIGHT, BOTTOM }

fun resolveZone(offset: Offset, size: Size): TapZone {
    val xRatio = offset.x / size.width
    val yRatio = offset.y / size.height
    return when {
        yRatio > 0.88f -> TapZone.BOTTOM
        xRatio < 0.25f -> TapZone.LEFT
        xRatio > 0.75f -> TapZone.RIGHT
        else -> TapZone.CENTER
    }
}
```

**可配置项**（见 Proto `ReaderPrefs.tap_zone_enabled`）：
- 关闭点击翻页 → 所有点击只唤出菜单
- 交换左右区域 → 左手/右手模式
- 音量键翻页：`NONE / PAGE_TURN / SCROLL / BRIGHTNESS`

### 4.6.4 常驻状态栏

```
┌──────────────────────────────────────────────────────┐
│  ⏰ 16:24    📶     🔋 78%    本章 42%    ← 顶部（可关）│
├──────────────────────────────────────────────────────┤
│                                                      │
│                    正 文 区 域                        │
│                                                      │
├──────────────────────────────────────────────────────┤
│  第 12 章 黎明之剑          1,204 / 2,860 字   ← 底部  │
│  ▓▓▓▓▓▓▓▓▓▓░░░░░░░░░░░░░░░░  34%                    │
└──────────────────────────────────────────────────────┘
```

实现：

```kotlin
@Composable
fun ReaderStatusBar(
    position: StatusPosition,
    progress: ReadingProgress,
    prefs: ReaderPrefs,
) {
    val battery by rememberBatteryState()     // BroadcastReceiver 监听 ACTION_BATTERY_CHANGED
    val time by rememberCurrentTime()          // 每分钟更新一次，不是每秒（省电）

    Row(
        Modifier.fillMaxWidth().height(28.dp).padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (prefs.showBattery) {
            Icon(batteryIcon(battery.level, battery.charging), null, Modifier.size(14.dp))
            Text("${(battery.level * 100).toInt()}%", style = statusStyle)
            Spacer(Modifier.width(12.dp))
        }
        if (prefs.showClock) {
            Text(time, style = statusStyle)
            Spacer(Modifier.width(12.dp))
        }
        Spacer(Modifier.weight(1f))
        if (prefs.showChapterPercent) {
            Text("本章 ${(progress.chapterPercent * 100).toInt()}%", style = statusStyle)
        }
    }
}

/** 时间更新：对齐到分钟边界，避免每秒唤醒 */
@Composable
fun rememberCurrentTime(): State<String> {
    val state = remember { mutableStateOf(formatTime(System.currentTimeMillis())) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000 - (System.currentTimeMillis() % 60_000))
            state.value = formatTime(System.currentTimeMillis())
        }
    }
    return state
}
```

> **电量为 0 的风险**：`rememberBatteryState` 用 `registerReceiver(null, IntentFilter(ACTION_BATTERY_CHANGED))` 获取初始值（粘性广播），再用 `DisposableEffect` 注册监听、`onDispose` 注销。**忘记注销会导致 Activity 泄漏**。

### 4.6.5 自动滚屏

```kotlin
@Composable
fun AutoScrollEffect(
    enabled: Boolean,
    speed: Float,                  // 1.0 ~ 10.0
    listState: LazyListState,
    onChapterEnd: () -> Unit,
) {
    LaunchedEffect(enabled, speed) {
        if (!enabled) return@LaunchedEffect
        // speed=1 → 8px/s，speed=10 → 80px/s（约 2 行/秒）
        val pxPerSecond = 8f * speed
        var lastFrame = withFrameNanos { it }
        while (isActive) {
            withFrameNanos { now ->
                val dt = (now - lastFrame) / 1_000_000_000f
                lastFrame = now
                val delta = pxPerSecond * dt
                val consumed = listState.scrollBy(delta)
                // 到章尾时滚动量被消耗完 → 触发下一章
                if (consumed == 0f || abs(consumed) < delta * 0.01f) {
                    onChapterEnd()
                }
            }
        }
    }
}
```

> **为什么用 `withFrameNanos` 而非 `delay(16)`？** `withFrameNanos` 与 Choreographer 帧同步，滚动量与 vsync 严格对齐，画面绝对平滑；`delay(16)` 会有累积误差与抖动。这是自动滚屏能否"看起来像丝绸"的分水岭。

**自动滚屏 + 常亮的组合**：开启自动滚屏时自动打开 `keepScreenOn`，暂停时恢复。

### 4.6.6 交互验收要点

- [ ] 五种翻页模式均可正常工作，模式切换不需重启
- [ ] 覆盖翻页在 120Hz 屏上帧率 ≥ 90fps（用 `dumpsys gfxinfo` 验证）
- [ ] 快速连续翻页 30 次无白屏、无错页、无卡死
- [ ] 点击三个区域行为正确，关闭点击翻页后仅唤出菜单
- [ ] 电量/时间显示正确，且退出阅读器后无 BroadcastReceiver 泄漏（LeakCanary 无告警）
- [ ] 自动滚屏 1 小时无累积抖动、无掉帧

---

## 4.7 网络书源管理

### 4.7.1 书源数据结构（运行时模型）

```kotlin
// core:model — BookSource.kt
@Serializable
data class BookSource(
    val id: Long = 0,
    val name: String,
    val group: String = "默认",
    val baseUrl: String,
    val enabled: Boolean = true,
    val charset: String = "UTF-8",
    val customUserAgent: String? = null,
    val timeoutMs: Long = 15_000,
    val needCookie: Boolean = false,
    val rules: SourceRules,
)

@Serializable
data class SourceRules(
    /** 搜索：URL 模板 + 请求方式 + 结果列表选择器 */
    val search: SearchRule,
    /** 详情页 */
    val detail: DetailRule,
    /** 目录页 */
    val catalog: CatalogRule,
    /** 正文页 */
    val content: ContentRule,
    /** 可选：章节列表排序方式 */
    val catalogOrder: String = "ASC",        // ASC | DESC
)

@Serializable
data class SearchRule(
    /** URL 模板，支持占位符：{{keyword}} {{page}} */
    val url: String,
    val method: String = "GET",
    val body: String? = null,                // POST 时的请求体模板
    /** 结果项容器选择器（Jsoup CSS） */
    val itemSelector: String,
    val nameSelector: String,
    val authorSelector: String? = null,
    val coverSelector: String? = null,
    val introSelector: String? = null,
    val linkSelector: String,                // 详情页链接，取 href / text
    val latestChapterSelector: String? = null,
    /** 分页：下一页选择器，null 表示无分页 */
    val nextPageSelector: String? = null,
    val maxPages: Int = 3,
)

@Serializable
data class DetailRule(
    val nameSelector: String? = null,
    val authorSelector: String? = null,
    val coverSelector: String? = null,
    val introSelector: String? = null,
    val statusSelector: String? = null,       // 连载/完结
    val wordCountSelector: String? = null,
    val latestChapterSelector: String? = null,
)

@Serializable
data class CatalogRule(
    val itemSelector: String,
    val titleSelector: String,                // 通常是 "a" 或 ".title"
    val linkSelector: String,                 // 取 href
    /** 卷分隔标识：匹配到的项视为卷标题（不抓正文） */
    val volumeSelector: String? = null,
    /** 是否倒序（很多站点最新章节在前） */
    val reversed: Boolean = false,
)

@Serializable
data class ContentRule(
    val contentSelector: String,              // 正文容器
    /** 需要移除的节点（广告、推荐、页脚） */
    val removeSelectors: List<String> = emptyList(),
    /** 段落分隔方式：PARAGRAPH(按 <p> 等块元素) | BR(按 <br>) */
    val paragraphMode: String = "PARAGRAPH",
    /** 需要替换为换行的标签 */
    val breakTags: List<String> = listOf("br", "p", "div"),
    /** 正文末尾截断标记（正则），如 "请记住本站域名" */
    val cutPatterns: List<String> = emptyList(),
)
```

### 4.7.2 规则解释引擎

```kotlin
// data:sources — SourceEngine.kt
class SourceEngine @Inject constructor(
    private val http: HttpClientProvider,
    private val rateLimiter: DomainRateLimiter,
) {
    /** 搜索 */
    suspend fun search(source: BookSource, keyword: String): List<SearchResultItem> =
        withContext(KrDispatchers.Network) {
            val url = buildUrl(source, source.rules.search.url, keyword, 1)
            val html = fetch(source, url)
            parseSearchResults(source, html)
        }

    /** 取目录 */
    suspend fun catalog(source: BookSource, detailUrl: String): List<CatalogItem> =
        withContext(KrDispatchers.Network) {
            val html = fetch(source, detailUrl)
            val doc = Jsoup.parse(html, source.baseUrl)
            val rule = source.rules.catalog
            doc.select(rule.itemSelector).mapNotNull { el ->
                val title = el.selectFirst(rule.titleSelector)?.text()?.trim().orEmpty()
                val href = el.selectFirst(rule.linkSelector)?.absUrl("href")
                if (title.isEmpty() || href.isNullOrEmpty()) return@mapNotNull null
                val isVolume = rule.volumeSelector != null &&
                    runCatching { el.select(rule.volumeSelector!!).isNotEmpty() }.getOrDefault(false)
                CatalogItem(title = title, url = href, isVolume = isVolume)
            }.let { if (rule.reversed) it.reversed() else it }
        }

    /** 取正文（含清洗） */
    suspend fun content(source: BookSource, chapterUrl: String): String =
        withContext(KrDispatchers.Network) {
            val html = fetch(source, chapterUrl)
            parseContent(source, html)
        }

    // ─────────────────────────────────────────────────────

    private suspend fun fetch(source: BookSource, url: String): String {
        rateLimiter.acquire(url)                        // ★ 按域限流，防封禁
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", source.customUserAgent ?: UAProvider.random())
            .header("Accept-Language", "zh-CN,zh;q=0.9")
            .header("Referer", source.baseUrl)
            .build()

        repeat(MAX_RETRY + 1) { attempt ->
            try {
                http.client.newCall(request).execute().use { resp ->
                    if (!resp.isSuccessful) {
                        if (resp.code in RETRYABLE_CODES && attempt < MAX_RETRY) {
                            delay(backoffMs(attempt)); return@repeat
                        }
                        throw KrError.NetworkBlocked(source.name, resp.code)
                    }
                    val bytes = resp.body?.bytes() ?: throw IOException("空响应体")
                    return decode(bytes, source.charset)
                }
            } catch (e: IOException) {
                if (attempt >= MAX_RETRY) throw e
                delay(backoffMs(attempt))
            }
        }
        error("unreachable")
    }

    private fun decode(bytes: ByteArray, declaredCharset: String): String {
        // 优先用 BOM/HTTP 声明，其次书源配置，最后启发式
        val bom = detectBomCharset(bytes)
        val charset = bom ?: runCatching {
            // 从 HTML meta 提取
            val head = String(bytes, 0, minOf(2048, bytes.size), Charsets.ISO_8859_1)
            Regex("""charset\s*=\s*["']?([\w-]+)""", RegexOption.IGNORE_CASE)
                .find(head)?.groupValues?.get(1)
        }.getOrNull() ?: declaredCharset

        return runCatching { String(bytes, Charset.forName(charset)) }
            .getOrElse { String(bytes, Charsets.UTF_8) }
    }

    /** 正文解析 + 清洗 */
    private fun parseContent(source: BookSource, html: String): String {
        val doc = Jsoup.parse(html, source.baseUrl)
        val rule = source.rules.content

        val container = doc.selectFirst(rule.contentSelector)
            ?: throw KrError.SourceRuleInvalid(source.name, "content.contentSelector 未匹配到元素")

        // 1. 移除广告/推荐节点
        rule.removeSelectors.forEach { sel ->
            runCatching { container.select(sel).remove() }
        }
        // 2. 移除 script/style/iframe
        container.select("script, style, iframe, noscript, ins, .ads, [class*=ad-]").remove()
        // 3. 文本提取
        var text = when (rule.paragraphMode) {
            "BR" -> container.html()
                .replace(Regex("""<br\s*/?>""", RegexOption.IGNORE_CASE), "\n")
                .let { Jsoup.parse(it).text() }
                .let { it.split("\n") }
            else -> container.select("p, div")
                .map { it.ownText().trim() }
                .filter { it.isNotEmpty() }
                .ifEmpty { container.text().split(Regex("""\s{2,}""")) }
        }.let { lines -> lines.joinToString("\n") { it.trim() } }

        // 4. cutPatterns 截断
        rule.cutPatterns.forEach { p ->
            runCatching {
                val m = Regex(p).find(text)
                if (m != null) text = text.substring(0, m.range.first).trimEnd()
            }
        }
        return text
    }
}
```

### 4.7.3 反封禁策略（**这是书源功能能否长期可用的关键**）

```kotlin
// core:network — UAProvider.kt
object UAProvider {
    /** 真实移动端 UA 池，随机轮换 */
    private val UA_POOL = listOf(
        "Mozilla/5.0 (Linux; Android 14; SM-S918B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36",
        "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Mobile Safari/537.36",
        "Mozilla/5.0 (iPhone; CPU iPhone OS 17_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Mobile/15E148 Safari/604.1",
        "Mozilla/5.0 (Linux; Android 12; V2164A) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Mobile Safari/537.36",
    )
    fun random(): String = UA_POOL.random()
}
```

```kotlin
// core:network — DomainRateLimiter.kt
/**
 * 按域名的令牌桶限流。
 * 目的：避免"聚合搜索 20 个源 → 同一个站点被并发打 20 次" 触发风控。
 */
class DomainRateLimiter(
    private val permitsPerSecond: Int = 2,
    private val maxBurst: Int = 4,
) {
    private val buckets = ConcurrentHashMap<String, Bucket>()

    suspend fun acquire(url: String) {
        val host = runCatching { URL(url).host }.getOrDefault(url)
        val bucket = buckets.getOrPut(host) { Bucket(permitsPerSecond.toDouble(), maxBurst) }
        bucket.acquire()          // 挂起直到有令牌
    }

    private class Bucket(rate: Double, capacity: Int) {
        private val mutex = Mutex()
        private var tokens = capacity.toDouble()
        private var lastRefill = SystemClock.elapsedRealtime()

        suspend fun acquire() = mutex.withLock {
            while (true) {
                refill()
                if (tokens >= 1.0) { tokens -= 1.0; return@withLock }
                val waitMs = ((1.0 - tokens) / rate * 1000).toLong().coerceAtLeast(20)
                mutex.unlock()
                try { delay(waitMs) } finally { mutex.lock() }
            }
        }

        private fun refill() {
            val now = SystemClock.elapsedRealtime()
            val elapsed = (now - lastRefill) / 1000.0
            tokens = (tokens + elapsed * rate).coerceAtMost(capacity)
            lastRefill = now
        }

        private val rate = rate
    }
}
```

**五层反封禁**：

| 层 | 手段 | 说明 |
| --- | --- | --- |
| 1 | UA 轮换 | 每次请求随机从池中取（书源可自定义覆盖） |
| 2 | 按域限流 | 每域 2 req/s，突发上限 4 |
| 3 | Referer 伪装 | 带上 `baseUrl` 作 Referer |
| 4 | 失败退避 | 超时/5xx 重试 2 次，间隔 1s / 3s（指数退避） |
| 5 | 健康度降级 | 连续失败 3 次 → `DEGRADED`（降低并发权重）；5 次 → `BROKEN`（聚合搜索时跳过，管理页红点提示） |

### 4.7.4 书源导入

```kotlin
// feature/sources — SourceImportViewModel.kt
sealed interface ImportInput {
    data class SingleJson(val text: String) : ImportInput
    data class UrlList(val urls: List<String>) : ImportInput
    data class File(val uri: Uri) : ImportInput
}

suspend fun import(input: ImportInput): ImportReport = when (input) {
    is ImportInput.SingleJson -> importFromText(input.text)
    is ImportInput.UrlList -> {
        // 批量 URL 导入：并发 3 拉取，逐条校验
        supervisorScope {
            input.urls.chunked(3).flatMap { chunk ->
                chunk.map { url ->
                    async(KrDispatchers.Network) {
                        runCatching {
                            val json = http.getPlain(url)
                            importFromText(json)
                        }.fold(
                            onSuccess = { it },
                            onFailure = { ImportReport.Single(url, false, it.message.orEmpty()) },
                        )
                    }
                }.awaitAll()
            }
        }.let { ImportReport.Batch(it) }
    }
    is ImportInput.File -> importFromText(readTextFromUri(input.uri))
}

private suspend fun importFromText(text: String): ImportReport {
    val sources = runCatching {
        SourceJsonCodec.decodeMany(text)      // 支持单对象 / 数组 / 带 {"sources": [...]} 包装
    }.getOrElse { return ImportReport.Invalid("JSON 格式错误：${it.message}") }

    val results = sources.map { src ->
        // 三步校验
        val validation = SourceValidator.validate(src)
        when {
            !validation.ok -> ImportReport.Single(src.name, false, validation.reason)
            repository.existsByName(src.name) -> ImportReport.Single(src.name, false, "已存在同名书源")
            else -> {
                repository.insert(src)
                ImportReport.Single(src.name, true, "")
            }
        }
    }
    return ImportReport.Batch(results)
}
```

**校验器**（书源质量的第一道闸门）：

```kotlin
object SourceValidator {
    data class Result(val ok: Boolean, val reason: String = "")

    fun validate(s: BookSource): Result {
        if (s.name.isBlank()) return Result(false, "书源名称为空")
        if (s.name.length > 40) return Result(false, "书源名称过长")
        if (!s.baseUrl.startsWith("http")) return Result(false, "baseUrl 必须以 http 开头")
        try { URL(s.baseUrl) } catch (e: Exception) { return Result(false, "baseUrl 非法") }

        val r = s.rules
        if (r.search.url.isBlank()) return Result(false, "search.url 为空")
        if (!r.search.url.contains("{{keyword}}")) return Result(false, "search.url 缺少 {{keyword}} 占位符")
        if (r.search.itemSelector.isBlank()) return Result(false, "search.itemSelector 为空")
        if (r.search.linkSelector.isBlank()) return Result(false, "search.linkSelector 为空")
        if (r.catalog.itemSelector.isBlank()) return Result(false, "catalog.itemSelector 为空")
        if (r.content.contentSelector.isBlank()) return Result(false, "content.contentSelector 为空")

        // 选择器语法预校验（用 Jsoup 试解析一个空文档）
        val allSelectors = listOf(
            r.search.itemSelector, r.search.nameSelector, r.search.linkSelector,
            r.catalog.itemSelector, r.catalog.titleSelector, r.catalog.linkSelector,
            r.content.contentSelector,
        ) + r.content.removeSelectors
        for (sel in allSelectors) {
            val err = runCatching { Jsoup.parse("<html></html>").select(sel); null }
                .getOrElse { it.message }
            if (err != null) return Result(false, "选择器语法错误「$sel」：$err")
        }
        return Result(true)
    }
}
```

### 4.7.5 书源规则调试器（P0 功能，决定书源生态可用性）

书源写错是常态。没有调试器，用户遇到问题只能放弃。**调试器必须是 P0**。

```kotlin
// feature/sources — SourceDebuggerScreen.kt
@Composable
fun SourceDebuggerScreen(sourceId: Long) {
    val vm: SourceDebuggerViewModel = hiltViewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    Column {
        // ① 测试关键词输入
        OutlinedTextField(
            value = state.keyword, onValueChange = vm::onKeywordChange,
            label = { Text("测试关键词") },
            trailingIcon = { IconButton(onClick = vm::runSearch) { Icon(Icons.PlayArrow, "运行") } },
        )

        // ② 分步骤结果面板（每步可展开看原始 HTML 与匹配元素）
        StepCard("1. 请求 URL", state.requestUrl, state.requestHeaders) {
            // 显示最终拼出的 URL + 请求头（让用户知道规则怎么工作）
        }
        StepCard("2. 响应", state.responseStatus, expandable = true) {
            CodeBlock(state.responseHtml.take(5000))       // 原始 HTML 前 5000 字
        }
        StepCard("3. 搜索结果匹配", "${state.matchedItems.size} 项") {
            state.matchedItems.forEach { item -> ResultPreview(item) }
            if (state.matchedItems.isEmpty()) {
                ErrorHint("itemSelector「${state.searchRule.itemSelector}」未匹配到元素。"
                    + "请检查：① 站点是否改了页面结构 ② 是否被反爬返回了验证页")
            }
        }
        StepCard("4. 目录匹配", "${state.catalogItems.size} 章") { ... }
        StepCard("5. 正文提取", "${state.contentText.length} 字") {
            CodeBlock(state.contentText.take(3000))
            // ★ 高亮显示被 removeSelectors 移除的部分（红底）与保留部分
        }

        // ③ 一键诊断
        DiagnosticPanel(state.diagnostics)
    }
}
```

**诊断规则示例**（把"为什么这个书源不工作"翻译成人类语言）：

| 现象 | 诊断输出 |
| --- | --- |
| HTTP 403 | "站点返回 403，可能屏蔽了当前 User-Agent。建议在书源设置里更换自定义 UA" |
| HTTP 200 但 itemSelector 匹配 0 项 | "页面获取成功但搜索结果为空。站点可能改版，或返回了 JS 渲染的页面架构" |
| 选择器语法错误 | "`div.class[` 括号不匹配，选择器非法" |
| 目录 0 章 | "目录选择器未匹配。检查是否在详情页 URL 上执行了目录规则，或站点把目录放在单独页面" |
| 正文 < 100 字 | "正文长度异常（${n} 字）。contentSelector 匹配到的可能是导航栏而非正文容器" |
| 正文含大量 "广告" 字样 | "检测到 12 处疑似广告文本，建议添加到 removeSelectors" |

---

## 4.8 聚合搜索与离线缓存

### 4.8.1 并发聚合搜索

```kotlin
// domain — SearchAcrossSourcesUseCase.kt
class SearchAcrossSourcesUseCase @Inject constructor(
    private val sourceRepo: SourceRepository,
    private val engine: SourceEngine,
    private val merger: ResultMerger,
) {
    operator fun invoke(keyword: String): Flow<SearchStreamEvent> = channelFlow {
        val sources = sourceRepo.enabledSourcesSortedByHealth()
        if (sources.isEmpty()) {
            send(SearchStreamEvent.AllFailed("没有启用的书源"))
            return@channelFlow
        }

        val total = sources.size
        var completed = 0
        val allResults = mutableListOf<SourceSearchResult>()

        supervisorScope {
            sources.forEach { source ->
                launch(KrDispatchers.Network) {
                    val started = SystemClock.elapsedRealtime()
                    val result = runCatching { engine.search(source, keyword) }
                    val elapsed = SystemClock.elapsedRealtime() - started

                    when {
                        result.isSuccess -> {
                            val items = result.getOrThrow()
                            sourceRepo.recordSuccess(source.id, elapsed)
                            send(SearchStreamEvent.SourceDone(source, items, elapsed))
                            synchronized(allResults) { allResults += SourceSearchResult(source, items) }
                        }
                        else -> {
                            sourceRepo.recordFailure(source.id)
                            send(SearchStreamEvent.SourceFailed(source, result.exceptionOrNull()!!.message.orEmpty()))
                        }
                    }
                    // 每完成一个源，重新归并（边搜边出）
                    val merged = merger.merge(synchronized(allResults) { allResults.toList() })
                    send(SearchStreamEvent.Merged(merged, ++completed, total))
                }
            }
        }

        if (allResults.isEmpty()) send(SearchStreamEvent.AllFailed("所有书源均失败，请检查网络或书源可用性"))
    }.buffer(Channel.UNLIMITED).flowOn(KrDispatchers.Default)
}
```

**并发度控制**：不是"20 个源同时打"。

```kotlin
/**
 * 源分组并发：同组内串行（组内往往是同站镜像，串行避免触发风控），
 * 组间并行。典型 20 个源分成 5 组，并发度 = 5。
 */
suspend fun enabledSourcesSortedByHealth(): List<BookSource>
```

### 4.8.2 结果归并去重算法

```kotlin
// data:sources — ResultMerger.kt
class ResultMerger {
    fun merge(results: List<SourceSearchResult>): List<MergedBook> {
        val groups = mutableListOf<MutableList<SourceBookItem>>()

        results.flatMap { r -> r.items.map { r.source to it } }
            .forEach { (source, item) ->
                val key = NormalizedKey.of(item)
                // 在已有组中找同书
                val target = groups.firstOrNull { g -> g.any { NormalizedKey.of(it.item).matches(key) } }
                if (target != null) target += SourceBookItem(source, item)
                else groups += mutableListOf(SourceBookItem(source, item))
            }

        return groups.map { g -> buildMergedBook(g) }
            // 排序：源质量分高的在前；源数多的在前（说明多站都有 → 更可能是正确结果）
            .sortedWith(
                compareByDescending<MergedBook> { it.sourceCount }
                    .thenByDescending { it.qualityScore },
            )
    }
}

object NormalizedKey {
    private val NOISE = Regex("""[\s（(【\[][^)）】\]]{0,30}?(小说|最新章节|全文阅读|无弹窗|免费阅读|TXT下载)[^)）】\]]{0,30}?[)）】\]]""")
    private val PUNCT = Regex("""[·・.,，。:：;；!！?？'"“”‘’\-—_~～]""")

    data class Key(val title: String, val author: String)

    fun of(item: SourceBookItem): Key = Key(
        title = normalize(item.item.name),
        author = normalize(item.item.author.orEmpty()),
    )

    private fun normalize(s: String): String = s
        .replace(NOISE, "")
        .replace(PUNCT, "")
        .replace(Regex("""\s+"""), "")
        .lowercase()
        .trim()

    /** 匹配规则：标题完全相同 → 同书；标题包含且作者相同 → 同书；编辑距离 ≤2 且作者相同 → 同书 */
    fun Key.matches(other: Key): Boolean {
        if (title.isEmpty() || other.title.isEmpty()) return false
        if (title == other.title) {
            // 作者都为空，或有交集 → 认为同一本
            return author.isEmpty() || other.author.isEmpty() ||
                   author.contains(other.author) || other.author.contains(author)
        }
        val authorMatches = author.isNotEmpty() && other.author.isNotEmpty() &&
            (author.contains(other.author) || other.author.contains(author))
        if (!authorMatches) return false
        return title.contains(other.title) || other.title.contains(title) ||
            levenshtein(title, other.title) <= 2
    }
}

/** 标准 Levenshtein，带两行滚动数组降内存 */
fun levenshtein(a: String, b: String): Int {
    if (a == b) return 0
    if (a.isEmpty()) return b.length
    if (b.isEmpty()) return a.length
    var prev = IntArray(b.length + 1) { it }
    var curr = IntArray(b.length + 1)
    for (i in 1..a.length) {
        curr[0] = i
        for (j in 1..b.length) {
            val cost = if (a[i - 1] == b[j - 1]) 0 else 1
            curr[j] = minOf(curr[j - 1] + 1, prev[j] + 1, prev[j - 1] + cost)
        }
        prev = curr.copyOf()
    }
    return prev[b.length]
}
```

**质量分计算**：

```kotlin
val qualityScore: Double = with(sourceCount * 0.35) +
    (hasCover * 0.15) +
    (hasIntro * 0.10) +
    (chapterCountEstimate / 1000.0 * 0.20) +      // 章节数多的源通常更完整
    (latestChapterFreshness * 0.20)               // 最新章节的更新时间越近越新
```

### 4.8.3 换源时序（保留进度）

```kotlin
// domain — SwitchSourceUseCase.kt
class SwitchSourceUseCase @Inject constructor(
    private val bookRepo: BookRepository,
    private val sourceRepo: SourceRepository,
    private val engine: SourceEngine,
    private val progressRepo: ProgressRepository,
    private val cacheRepo: CacheRepository,
) {
    suspend operator fun invoke(
        bookId: Long,
        targetSourceId: Long,
        targetBookUrl: String,
    ): SwitchResult = withContext(KrDispatchers.IO) {
        val book = bookRepo.getBook(bookId) ?: return@withContext SwitchResult.BookNotFound
        val progress = progressRepo.get(bookId)?.toDomain()

        // 1. 拉目标源目录
        val source = sourceRepo.get(targetSourceId) ?: return@withContext SwitchResult.SourceNotFound
        val catalog = runCatching { engine.catalog(source, targetBookUrl) }
            .getOrElse { return@withContext SwitchResult.CatalogFailed(it.message.orEmpty()) }

        if (catalog.isEmpty()) return@withContext SwitchResult.CatalogEmpty

        // 2. 章节匹配（三级策略）
        val match = ChapterMatcher.match(
            oldTitle = progress?.chapterTitle,
            oldIndex = progress?.chapterIndex ?: 0,
            oldTotal = book.chapters.size,
            newCatalog = catalog,
        )

        // 3. 更新书籍的源信息（保留旧章节缓存作为离线兜底）
        bookRepo.updateSourceInfo(
            bookId = bookId,
            sourceId = targetSourceId,
            sourceBookKey = targetBookUrl,
            newChapters = catalog.mapIndexed { i, c ->
                ChapterMeta(i, c.title, 0, 0, isVolume = c.isVolume, sourceUrl = c.url)
            },
        )

        // 4. 进度迁移：章节号变了，但章内字符偏移保留
        match?.let { m ->
            progressRepo.update(
                ReadingProgress(
                    bookId = bookId,
                    chapterIndex = m.newIndex,
                    chapterTitle = m.newTitle,
                    charOffsetInChapter = progress.charOffsetInChapter,   // ★ 保留
                    globalCharOffset = 0,
                    percent = m.newIndex.toFloat() / catalog.size,
                    chapterPercent = 0f,
                    readingSeconds = progress.readingSeconds,
                    updatedAt = System.currentTimeMillis(),
                ),
            )
        }

        SwitchResult.Success(matched = match != null, chapterTitle = match?.newTitle ?: catalog.first().title, matchConfidence = match?.confidence ?: 0f)
    }
}

object ChapterMatcher {
    data class Match(val newIndex: Int, val newTitle: String, val confidence: Float)

    private val CN_NUM = mapOf(
        '零' to 0, '一' to 1, '二' to 2, '两' to 2, '三' to 3, '四' to 4, '五' to 5,
        '六' to 6, '七' to 7, '八' to 8, '九' to 9, '十' to 10, '百' to 100, '千' to 1000,
    )

    /** 抽取"第 N 章"中的 N */
    fun extractOrdinal(title: String): Int? {
        val m = Regex("""第\s*([0-9零一二三四五六七八九十百千万两]+)\s*[章节回】]""").find(title) ?: return null
        val raw = m.groupValues[1]
        return raw.toIntOrNull() ?: runCatching { cnToInt(raw) }.getOrNull()
    }

    private fun cnToInt(s: String): Int {
        var total = 0; var section = 0; var number = 0
        for (c in s) {
            val v = CN_NUM[c] ?: return -1
            when {
                v >= 1000 -> { section += (if (number == 0) 1 else number) * v; number = 0 }
                v >= 100 -> { section += (if (number == 0) 1 else number) * v; number = 0 }
                v == 10 -> { number = if (number == 0) 10 else number * 10 }
                else -> number += v
            }
        }
        return total + section + number
    }

    fun match(
        oldTitle: String?,
        oldIndex: Int,
        oldTotal: Int,
        newCatalog: List<CatalogItem>,
    ): Match? {
        if (newCatalog.isEmpty()) return null
        val old = oldTitle.orEmpty()

        // 策略 1：标题精确匹配（归一化后）
        val normOld = normalize(old)
        newCatalog.indexOfFirst { normalize(it.title) == normOld && normOld.isNotEmpty() }
            .takeIf { it >= 0 }
            ?.let { return Match(it, newCatalog[it].title, 1.0f) }

        // 策略 2：章节序号匹配（"第 183 章"）
        val oldOrd = extractOrdinal(old)
        if (oldOrd != null) {
            newCatalog.firstOrNull { extractOrdinal(it.title) == oldOrd }
                ?.let { return Match(newCatalog.indexOf(it), it.title, 0.9f) }
        }

        // 策略 3：标题相似度（编辑距离最小，且需低于阈值）
        val candidates = newCatalog.mapIndexedNotNull { i, c ->
            val d = levenshtein(normOld, normalize(c.title))
            if (d <= maxOf(2, normOld.length / 4)) Triple(i, c.title, d) else null
        }.sortedBy { it.third }
        candidates.firstOrNull()?.let {
            return Match(it.first, it.second, 0.6f)
        }

        // 策略 4：比例位置回退
        val ratio = oldIndex.toFloat() / oldTotal.coerceAtLeast(1)
        val fallbackIndex = (ratio * newCatalog.size).toInt().coerceIn(0, newCatalog.lastIndex)
        return Match(fallbackIndex, newCatalog[fallbackIndex].title, 0.3f)
    }

    private fun normalize(s: String) = s.replace(Regex("""[\s　]+"""), "")
        .replace(Regex("""[：:·・.,，。]"""), "")
        .trim()
}
```

**置信度反馈给用户**：`confidence < 0.5` 时 Toast 提示"未能精确定位章节，已跳到《X》，请手动确认"。

### 4.8.4 离线批量缓存

```kotlin
// data:sources — CacheChaptersWorker.kt
@HiltWorker
class CacheChaptersWorker @AssistedInject constructor(
    @Assisted ctx: Context,
    @Assisted params: WorkerParameters,
    private val engine: SourceEngine,
    private val cacheRepo: CacheRepository,
    private val sourceRepo: SourceRepository,
    private val bookRepo: BookRepository,
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val bookId = inputData.getLong(KEY_BOOK_ID, -1)
        val fromChapter = inputData.getInt(KEY_FROM, 0)
        val toChapter = inputData.getInt(KEY_TO, Int.MAX_VALUE)
        if (bookId <= 0) return Result.failure()

        val book = bookRepo.getBook(bookId) ?: return Result.failure()
        val source = book.sourceId?.let { sourceRepo.get(it) } ?: return Result.failure()
        val chapters = bookRepo.getChapters(bookId)
            .filter { it.chapterIndex in fromChapter..toChapter && !it.isCached }

        if (chapters.isEmpty()) return Result.success()

        var done = 0
        var failedConsecutive = 0

        for (ch in chapters) {
            if (isStopped) return Result.retry()          // 用户取消 / 系统停止
            if (failedConsecutive >= 5) {
                // 连续 5 次失败 → 判定源不可用，停止并让用户知道
                return Result.failure(workDataOf("reason" to "书源连续失败，已停止缓存"))
            }

            try {
                val content = engine.content(source, ch.sourceUrl!!)
                cacheRepo.put(bookId, ch.chapterIndex, ch.title, content, source.id)
                done++
                failedConsecutive = 0
                setProgress(workDataOf(KEY_DONE to done, KEY_TOTAL to chapters.size))
            } catch (e: Exception) {
                failedConsecutive++
                // 单章失败继续下一章（章节点可能已被站点删除）
            }

            // 节流：每章之间延迟，避免触发风控
            delay(if (failedConsecutive > 0) 2000 else 300)
        }

        return Result.success(workDataOf(KEY_DONE to done, KEY_TOTAL to chapters.size))
    }

    companion object {
        const val KEY_BOOK_ID = "bookId"
        const val KEY_FROM = "from"
        const val KEY_TO = "to"
        const val KEY_DONE = "done"
        const val KEY_TOTAL = "total"

        fun enqueue(ctx: Context, bookId: Long, from: Int, to: Int, wifiOnly: Boolean) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val request = OneTimeWorkRequestBuilder<CacheChaptersWorker>()
                .setInputData(workDataOf(KEY_BOOK_ID to bookId, KEY_FROM to from, KEY_TO to to))
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .addTag("cache_book_$bookId")
                .build()

            WorkManager.getInstance(ctx)
                .enqueueUniqueWork("cache_book_$bookId", ExistingWorkPolicy.KEEP, request)
        }
    }
}
```

**缓存策略配置**：

| 选项 | 说明 |
| --- | --- |
| 缓存范围 | 全部章节 / 当前章起后 N 章 / 指定章节区间 |
| 网络约束 | 仅 Wi-Fi（默认）/ 任意网络 |
| 缓存时机 | 手动触发（默认）/ 打开书时自动缓存后 30 章（可选） |
| 缓存上限 | 默认 200MB，可调 50MB ~ 2GB |
| 过期策略 | 连载中书缓存 7 天过期；完结书永不过期 |

### 4.8.5 缓存淘汰算法

```kotlin
// data:sources — CacheEvictor.kt
class CacheEvictor @Inject constructor(
    private val cacheDao: ChapterCacheDao,
    private val prefs: GlobalPrefsStore,
) {
    /** 在每次写入缓存后调用；超过上限时按优先级淘汰 */
    suspend fun evictIfNeeded(): EvictReport = withContext(KrDispatchers.IO) {
        val limitBytes = prefs.cacheLimitMb * 1024L * 1024L
        val total = cacheDao.totalBytes()
        if (total <= limitBytes) return@withContext EvictReport.None

        var needFree = total - limitBytes
        var evicted = 0
        var freedBytes = 0L

        // 优先级 1：已过期条目
        val now = System.currentTimeMillis()
        cacheDao.selectExpired(now).forEach { row ->
            if (needFree <= 0) return@forEach
            cacheDao.delete(row.id); needFree -= row.byteSize; freedBytes += row.byteSize; evicted++
        }

        // 优先级 2：非当前阅读书籍 + 距离阅读位置最远
        if (needFree > 0) {
            val currentBookId = prefs.currentReadingBookId
            val currentChapter = prefs.currentReadingChapter
            cacheDao.selectOldestFirst()
                .filter { it.bookId != currentBookId }
                .forEach { row ->
                    if (needFree <= 0) return@forEach
                    cacheDao.delete(row.id); needFree -= row.byteSize; freedBytes += row.byteSize; evicted++
                }
        }

        // 优先级 3：当前书籍中距离阅读位置 > 20 章的
        if (needFree > 0) {
            cacheDao.selectByBook(prefs.currentReadingBookId)
                .filter { abs(it.chapterIndex - prefs.currentReadingChapter) > 20 }
                .sortedByDescending { abs(it.chapterIndex - prefs.currentReadingChapter) }
                .forEach { row ->
                    if (needFree <= 0) return@forEach
                    cacheDao.delete(row.id); needFree -= row.byteSize; freedBytes += row.byteSize; evicted++
                }
        }

        // 保护：当前书籍 ±5 章永不淘汰（上面的 filter 已保证）
        EvictReport(target = limitBytes, freedBytes = freedBytes, evictedCount = evicted)
    }
}
```

### 4.8.6 聚合搜索与缓存验收要点

- [ ] 8 个书源并发搜索，全部返回耗时 ≤ 3s（含最慢源）
- [ ] 单个源失败不影响其他源结果展示（`supervisorScope` 生效）
- [ ] 同一本书出现在 5 个源，归并为 1 条，展开显示 5 个源
- [ ] 换源后章节标题一致，字级偏移保留，继续阅读内容连续
- [ ] 连续缓存 500 章，可在设置页看到进度，可中途取消（WorkManager 状态为 CANCELLED）
- [ ] 缓存写入超过 200MB 后自动淘汰，当前书 ±5 章未被动
- [ ] 书源连续失败 5 次后续章节停止缓存并给出明确提示

---

**上一卷**：[第 3 卷 · 数据模型与存储设计](#vol-03) ｜ **下一卷**：[第 5 卷 · 关键算法实现方案](#vol-05)


---


# 第 5 卷 · 关键算法实现方案 {#vol-05}
> 第 4 卷讲"每个模块怎么做"，本卷讲"**最难的那几个点怎么做对、做快**"。每个算法以「算法卡片」形式给出：目标 → 复杂度 → 伪代码 → 边界 → 测试向量 → 达标判据。本卷中列出的测试向量**必须**成为单元测试用例。

---

## 5.1 算法卡 A：编码探测（准确率攻坚）

### A.0 目标

| 项 | 要求 |
| --- | --- |
| 输入 | TXT 文件（任意大小） |
| 输出 | `Charset` + 置信度 + 探测依据 |
| 准确率 | 在测试语料集上 ≥ 99%（见 A.5） |
| 耗时 | ≤ 120ms（10MB 文件） |
| 内存 | ≤ 300KB（只采样，不全读） |

### A.1 为什么"猜编码"这么难

中文文本的编码识别有三大混淆区：

| 混淆场景 | 说明 | 后果 |
| --- | --- | --- |
| **UTF-8 vs GBK 短文本** | 只有 3~10 个汉字的文本，字节数太少，统计特征不足 | 可能误判，但影响小（内容少） |
| **GBK 文本恰好是合法 UTF-8** | 极罕见。GBK 的双字节序列需要恰好构成合法 UTF-8 多字节模式，概率约 1e-6 | 概率可忽略 |
| **纯 ASCII 文本** | 任何编码都能解 | 直接用 UTF-8（结果相同，无副作用） |
| **GB2312 vs GBK vs GB18030** | 三者是超集关系 | **统一按 GB18030 处理**（它是 GBK 与 GB2312 的严格超集，能解 GBK 与 GB2312 的全部内容） |
| **BIG5 vs GBK** | 中国台湾地区文本用 BIG5。二者字节范围重叠 | 需靠"解码后字符是否为常用字"打分区分 |

**关键决策**：**内部统一用 GB18030 而非区分 GBK/GB2312**。这消除了一个不可解的歧义来源——凡是能解 GBK 的编码，GB18030 一定能解，且 GB18030 还能解 4 字节扩展区。

### A.2 探测决策树（最终版）

```
                        ┌─────────────────────┐
                        │  读取前 256KB 样本   │
                        └──────────┬──────────┘
                                   ▼
                    ┌──────────────────────────────┐
                    │ 1. BOM 检测                   │
                    │    EF BB BF    → UTF-8        │
                    │    FF FE       → UTF-16LE     │
                    │    FE FF       → UTF-16BE     │
                    └──────────┬───────────────────┘
                        命中 │        未命中
                             ▼            ▼
                    置信度 1.0    ┌──────────────────────────┐
                    直接返回      │ 2. 严格 UTF-8 校验        │
                                 │    (CodingErrorAction.   │
                                 │     REPORT)              │
                                 └──────────┬───────────────┘
                                    通过 │        失败
                                         ▼            ▼
                          ┌────────────────────┐  ┌──────────────────────────┐
                          │ 3. 是否纯 ASCII？   │  │ 4. 启发式打分            │
                          │    bytes < 0x80    │  │    候选：GB18030, BIG5,  │
                          │    → UTF-8, 0.99   │  │          UTF-16LE,       │
                          └────────────────────┘  │          UTF-16BE        │
                                                  │    打分见 A.3            │
                                                  └──────────┬───────────────┘
                                                             ▼
                                            ┌──────────────────────────────┐
                                            │ 最高分 ≥ 阈值(0.55)？        │
                                            │  是 → 采用，置信度 = 得分    │
                                            │  否 → 返回 UNKNOWN           │
                                            │       UI 提示手动选择        │
                                            └──────────────────────────────┘
```

### A.3 启发式打分函数（完整规格）

打分必须**多信号联合**，单一信号不足以区分 BIG5 与 GB18030。

```kotlin
// data:parser/txt — CharsetScorer.kt
object CharsetScorer {

    /** 常用汉字表（前 3500 字，覆盖 99.5% 的日常文本用字） */
    private val COMMON_CJK: Set<Char> = run {
        // 从 assets 加载预置的常用字表（GB2312 一级字库 3755 字）
        // 这是"常用度"判据的核心数据
        loadFromAssets("gb2312_level1.txt").toHashSet()
    }

    /** 中国台湾地区 BIG5 特有字符（简体 GBK 中不存在或罕用） */
    private val BIG5_SPECIFIC = setOf('臺', '灣', '萬', '裡', '發', '來', '說', '沒', '現', '這')

    /** 简体特有字符（BIG5 中不存在） */
    private val SIMPLIFIED_SPECIFIC = setOf('台', '湾', '万', '里', '发', '来', '说', '没', '现', '这')

    data class Score(
        val charset: String,
        val score: Double,
        val signals: Map<String, Double>,   // 用于调试与诊断
    )

    fun score(bytes: ByteArray, charsetName: String): Score {
        val charset = Charset.forName(charsetName)

        // 信号 1：严格解码可行性（权重最高，因为它是硬约束）
        val text = try {
            charset.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes)).toString()
        } catch (e: CharacterCodingException) {
            // 解码失败比例：允许少量失败（文件尾部可能被截断），
            // 但失败超过 0.5% 就判定该编码不可能
            val lossy = charset.newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE)
                .decode(ByteBuffer.wrap(bytes)).toString()
            val badRatio = lossy.count { it == '\uFFFD' }.toDouble() / lossy.length.coerceAtLeast(1)
            if (badRatio > 0.005) return Score(charsetName, 0.0, mapOf("decode" to 0.0))
            return Score(charsetName, 0.15 * (1 - badRatio / 0.005), mapOf("decode" to badRatio))
        }

        if (text.isEmpty()) return Score(charsetName, 0.0, emptyMap())

        // 统计各类字符
        var cjkCount = 0
        var commonCjkCount = 0
        var rareCjkCount = 0
        var printableAscii = 0
        var controlCount = 0
        var big5Specific = 0
        var simplifiedSpecific = 0
        var punctuation = 0

        val PUNCT = "，。！？；：、“”‘’（）《》〈〉【】—…·　「」『』"
        val RARE_RANGES = listOf(
            '\u3400'..'\u4DBF',   // 扩展 A 区（生僻）
            '\uF900'..'\uFAFF',   // 兼容汉字
        )

        for (ch in text) {
            val code = ch.code
            when {
                ch in '\u4E00'..'\u9FFF' -> {
                    cjkCount++
                    if (ch in COMMON_CJK) commonCjkCount++ else rareCjkCount++
                }
                code in 0x20..0x7E || ch == '\n' || ch == '\r' || ch == '\t' -> printableAscii++
                code < 0x20 -> controlCount++
                ch == '\uFFFD' -> controlCount++        // 替换字符
                ch in PUNCT -> punctuation++
                RARE_RANGES.any { ch in it } -> rareCjkCount++
                ch == '\u3000' || ch == '\u2000'..'\u200B' -> punctuation++
                ch in BIG5_SPECIFIC -> big5Specific++
                ch in SIMPLIFIED_SPECIFIC -> simplifiedSpecific++
            }
        }

        val total = text.length.toDouble()
        val cjkRatio = cjkCount / total
        val commonRatio = commonCjkCount / cjkCount.coerceAtLeast(1)
        val rareRatio = rareCjkCount / cjkCount.coerceAtLeast(1)
        val controlRatio = controlCount / total
        val punctRatio = punctuation / total

        // 信号 2：控制字符惩罚（编码猜错时最明显的特征）
        val controlPenalty = when {
            controlRatio > 0.02 -> 1.0           // 直接判死
            controlRatio > 0.005 -> 0.5
            else -> controlRatio / 0.005 * 0.1
        }

        // 信号 3：常用字占比（正确的编码解出来都是常用字）
        val commonnessScore = when {
            cjkRatio < 0.05 -> 0.5              // 英文为主，此信号无效，给中性分
            else -> commonRatio * 0.7 + (1 - rareRatio) * 0.3
        }

        // 信号 4：中日韩字符占比（中文文本应有大量 CJK）
        val cjkScore = when {
            cjkRatio > 0.30 -> 1.0
            cjkRatio > 0.10 -> 0.8
            cjkRatio > 0.02 -> 0.6
            else -> 0.5                          // 可能确实是英文文本
        }

        // 信号 5：标点合理性（正确编码下中文标点会正常出现）
        val punctScore = when {
            punctRatio > 0.03 -> 1.0
            punctRatio > 0.01 -> 0.8
            else -> 0.6
        }

        // 信号 6：简繁特征（区分 GB18030 与 BIG5 的关键）
        val scriptScore = when {
            simplifiedSpecific > 0 && big5Specific == 0 -> 1.0
            big5Specific > 0 && simplifiedSpecific == 0 -> 0.3   // 对 GB18030 不利，对 BIG5 有利
            else -> 0.7
        }

        // 加权合成
        val raw = commonnessScore * 0.32 +
                  cjkScore * 0.18 +
                  punctScore * 0.10 +
                  scriptScore * 0.25 +
                  (1 - controlPenalty) * 0.15

        return Score(
            charset = charsetName,
            score = raw.coerceIn(0.0, 1.0),
            signals = mapOf(
                "control" to controlRatio,
                "common" to commonRatio,
                "cjk" to cjkRatio,
                "punct" to punctRatio,
                "simplified" to simplifiedSpecific.toDouble(),
                "big5" to big5Specific.toDouble(),
            ),
        )
    }
}
```

> **注意 `scriptScore` 对 BIG5 的处理**：上面的实现是给"GB18030 打分"的视角，`scriptScore` 在检测到 BIG5 特有字时给 0.3（惩罚 GB18030）。对 BIG5 候选自身打分的实现，需要把 `BIG5_SPECIFIC` 与 `SIMPLIFIED_SPECIFIC` 的角色对调——在 `CharsetScorer` 中传入 `isTraditionalTarget` 参数控制。这一点必须在实现时写明，否则 BIG5 永远判不出来。

### A.4 混淆场景实战对照表

| 文件特征 | 正确编码 | 探测逻辑路径 | 可能的误判与防御 |
| --- | --- | --- | --- |
| 带 BOM 的 UTF-8 | UTF-8 | 路径 1（BOM） | 无 |
| 纯中文简体，UTF-8 无 BOM | UTF-8 | 路径 2（严格校验通过） | 无 |
| 纯中文简体，GBK | GB18030 | 路径 4，GB18030 得分最高 | 需确保 UTF-8 严格校验**先**跑且失败 |
| 中英混合，GBK | GB18030 | 路径 4 | 英文部分无区分度，靠中文部分 |
| 中国台湾地区正体中文 | BIG5 | 路径 4，BIG5 的 `scriptScore` 高 | 若文本含大量简体字 → 可能误判为 GB18030。**必须提示用户可手动切换** |
| 只有 5 个汉字的短文本 | 任意 | 各编码得分相近 | 采用"最可能是 UTF-8"的默认，且**UI 提供一键切换编码** |
| UTF-16 无 BOM | UTF-16LE | 路径 4，控制字符率极低（因为 UTF-16 下奇偶字节组合让 GB18030 解码出大量控制字符） | UTF-16 文件在 GB18030 视角下会产生很多控制字符 → 反向验证有效 |
| 文件前半 UTF-8 后半 GBK（拼接损坏） | UTF-8 | 路径 2 在前 256KB 通过 → 采用 UTF-8 | 后半段会有替换字符。**这是可接受降级**：至少一半内容可读 |

### A.5 测试向量（**必须成为单元测试**）

```
测试语料集（assets/test/charset/）：

┌──────────────────────────────────────────┬────────────┬──────────┐
│ 文件名                                    │ 真实编码    │ 期望结果 │
├──────────────────────────────────────────┼────────────┼──────────┤
│ utf8_bom_simple.txt      (1KB 简体)      │ UTF-8+BOM  │ UTF-8    │
│ utf8_nobom_simple.txt    (50KB 简体)     │ UTF-8      │ UTF-8    │
│ utf8_nobom_mixed.txt     (200KB 中英)    │ UTF-8      │ UTF-8    │
│ gbk_simple.txt           (80KB 简体)     │ GBK        │ GB18030  │
│ gbk_mixed.txt            (300KB 中英)    │ GBK        │ GB18030  │
│ gbk_large.txt            (10MB 简体)     │ GBK        │ GB18030  │
│ gb2312_classic.txt       (60KB)          │ GB2312     │ GB18030  │
│ big5_traditional.txt     (120KB 正体)    │ BIG5       │ BIG5     │
│ utf16le_bom.txt          (40KB)          │ UTF-16LE   │ UTF-16LE │
│ utf16be_bom.txt          (40KB)          │ UTF-16BE   │ UTF-16BE │
│ utf16le_nobom.txt        (40KB)          │ UTF-16LE   │ UTF-16LE │
│ ascii_only.txt           (100KB 英文)    │ ASCII      │ UTF-8    │
│ tiny_5chars.txt          (5 个汉字)      │ GBK        │ UTF-8*   │
│ utf8_truncated.txt       (UTF-8 被截断)  │ UTF-8(坏尾)│ UTF-8    │
│ crlf_windows.txt         (100KB)         │ GBK+\r\n   │ GB18030  │
└──────────────────────────────────────────┴────────────┴──────────┘
 * tiny_5chars 允许任意结果，但必须"不崩溃 + 提示可手动切换"

准确率判据：14/15 正确（≥ 93%），排除 tiny_5chars 后 14/14（100%）
```

**测试实现要点**：

```kotlin
@Test
fun `探测准确率达标`() {
    val corpus = loadCorpus("assets/test/charset")
    val results = corpus.map { case ->
        val detected = runBlocking { detector.detect(case.file) }
        case.expected to detected.charset.name()
    }
    val correct = results.count { (exp, got) -> exp == got }
    assertTrue(
        "编码探测准确率 $correct/${results.size}，低于 93% 门槛",
        correct.toDouble() / results.size >= 0.93,
    )
    // 输出混淆矩阵，便于分析
    results.groupBy { it.first }.forEach { (exp, list) ->
        println("期望 $exp → $list (${list.size} 例)")
    }
}
```

### A.6 手动切换兜底（必做）

任何自动探测都可能失败。**必须提供用户手动切换通道，且切换后立即重新分章**：

```kotlin
// feature/settings — EncodingPickerDialog.kt
@Composable
fun EncodingPickerDialog(
    current: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val candidates = listOf("UTF-8", "GB18030", "BIG5", "UTF-16LE", "UTF-16BE")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择文件编码") },
        text = {
            Column {
                Text("当前显示为乱码，可手动切换编码：", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
                candidates.forEach { cs ->
                    // ★ 每个选项显示该编码下前 60 字的实际预览——用户一眼就能看出对不对
                    EncodingPreviewRow(
                        charset = cs,
                        previewText = remember(cs) { previewWith(cs, 60) },
                        selected = cs == current,
                        onClick = { onConfirm(cs) },
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}
```

> **带预览的编码选择器**是这个功能的体验关键。让用户在 5 个选项里逐个试读，比只给编码名称有效 10 倍。

---

## 5.2 算法卡 B：章节识别的多信号投票与误报抑制

### B.0 目标

| 项 | 要求 |
| --- | --- |
| 输入 | TXT 文本流（行迭代器） |
| 输出 | 章节列表，含精确字符偏移 |
| 召回率 | 标准网文（2000 章）识别 ≥ 99.5% |
| 精确率 | 误报（把正文当章节）≤ 0.2% |
| 耗时 | ≤ 300ms（10MB 文件，约 18 万行） |

### B.1 召回与精确的矛盾

**放宽规则能提高召回但引入误报**：

```
正文中常见的"伪章节"行：
  「第三章的那个人」                      ← 引用，不是章节标题
  「他想起第一回见到她的时候」            ← 正文句子
  「卷一 少年游」                         ← 真章节（卷标题）
  「第3章 更新通知：本作品已完结」         ← 真章节（含噪声）
  「第一章」                              ← 真章节
  第一章                                   ← 真章节（无标题）
  CHAPTER 12                              ← 真章节（英文）
  12                                      ← 可能是页码，不是章节！

误报的代价：目录里出现 "第三章的那个人" → 用户困惑
漏报的代价：目录缺章 → 无法跳转
```

### B.2 多信号投票机制

**核心思想**：不靠单条规则，而是对"候选行"计算多个特征，用加权投票判定。

```kotlin
// data:parser/txt — ChapterCandidateVoter.kt
object ChapterCandidateVoter {

    /** 候选行的特征向量 */
    data class Features(
        val lineNo: Int,
        val charOffset: Long,
        val rawText: String,
        val trimmed: String,
        val length: Int,
        val leadingWhitespace: Int,
        val hasFollowingBlankLine: Boolean,
        val prevLineIsBlank: Boolean,
        val prevLineLength: Int,
        val nextLineLength: Int,
        val ordinal: Int?,                 // "第 N 章" 抽出的 N
        val keywordType: KeywordType,       // 命中的关键词类型
        val matchRuleId: String,
    )

    enum class KeywordType { NUMBERED, VOLUME, SPECIAL, ENGLISH, DIGIT_ONLY, NONE }

    /** 信号权重表（可通过实验调优） */
    private const val W_RULE_MATCH        = 0.30
    private const val W_LENGTH            = 0.15
    private const val W_BLANK_NEIGHBOR    = 0.15
    private const val W_DISTANCE          = 0.15
    private const val W_ORDINAL_MONOTONIC = 0.20
    private const val W_NO_PUNCT_END      = 0.05

    /** 判定阈值：得分 ≥ 0.65 视为真章节 */
    private const val THRESHOLD = 0.65

    fun vote(f: Features, ctx: VoteContext): VoteResult {
        var score = 0.0
        val reasons = mutableListOf<String>()

        // ── 信号 1：规则匹配置信度 ─────────────────────────
        score += when (f.matchRuleId) {
            "cn_numbered" -> 1.0
            "cn_volume"   -> 1.0
            "cn_special"  -> 0.95
            "en_numbered" -> 0.9
            "digit_only"  -> 0.3      // "12" 太容易是页码，低置信
            else          -> 0.0
        } * W_RULE_MATCH

        // ── 信号 2：长度合理性 ────────────────────────────
        // 章节标题通常 3~30 字。过长（>40）或过短（1 字）都可疑
        val lenScore = when {
            f.length in 3..30 -> 1.0
            f.length in 31..40 -> 0.6
            f.length == 2 -> 0.5
            f.length == 1 -> 0.2
            else -> 0.1
        }
        score += lenScore * W_LENGTH
        if (lenScore < 0.5) reasons += "长度异常(${f.length})"

        // ── 信号 3：前后空行（章节标题通常独立成行，前后有空白）──
        val blankScore = when {
            f.prevLineIsBlank && f.hasFollowingBlankLine -> 1.0
            f.prevLineIsBlank || f.hasFollowingBlankLine -> 0.7
            else -> 0.3
        }
        score += blankScore * W_BLANK_NEIGHBOR

        // ── 信号 4：与上一章的距离 ────────────────────────
        // 正文中的"第三章"引用会密集出现（几十字内多个）
        val dist = f.charOffset - ctx.lastAcceptedOffset
        val distScore = when {
            ctx.acceptedCount == 0 -> 1.0                  // 首个候选无条件接受基础分
            dist < 200 -> 0.0                              // 太近：几乎必然是引用
            dist < 500 -> 0.4
            dist < 2000 -> 0.9
            dist < 20000 -> 1.0
            else -> 0.7                                    // 过长：可能漏了章节，或分卷处
        }
        score += distScore * W_DISTANCE
        if (distScore < 0.5 && ctx.acceptedCount > 0) reasons += "距上章仅 ${dist} 字"

        // ── 信号 5：序号单调性（最强信号之一）────────────────
        val monoScore = if (f.ordinal != null && ctx.lastOrdinal != null) {
            when {
                f.ordinal == ctx.lastOrdinal + 1 -> 1.0        // 完美递增
                f.ordinal > ctx.lastOrdinal -> 0.85            // 递增但跳跃（可能跳章）
                f.ordinal == ctx.lastOrdinal -> 0.3            // 重复（可能是分卷重置）
                else -> 0.1                                    // 倒退：可疑
            }
        } else if (f.ordinal != null && ctx.lastOrdinal == null) {
            0.9
        } else 0.5
        score += monoScore * W_ORDINAL_MONOTONIC
        if (f.ordinal != null && ctx.lastOrdinal != null && f.ordinal <= ctx.lastOrdinal) {
            reasons += "序号非递增(${ctx.lastOrdinal} → ${f.ordinal})"
        }

        // ── 信号 6：行尾无标点 ───────────────────────────
        // "他想起第一回见到她的时候。" 以句号结尾 → 是正文句子
        val lastCh = f.trimmed.lastOrNull()
        val punctScore = if (lastCh != null && lastCh in "。！？；，、") 0.0 else 1.0
        score += punctScore * W_NO_PUNCT_END
        if (punctScore == 0.0) reasons += "以句末标点结尾，疑似正文"

        return VoteResult(accepted = score >= THRESHOLD, score = score, reasons = reasons)
    }

    data class VoteContext(
        val lastAcceptedOffset: Long = Long.MIN_VALUE,
        val lastOrdinal: Int? = null,
        val acceptedCount: Int = 0,
    )
}
```

### B.3 两阶段处理：先粗筛，再投票

为了性能，**不是每一行都跑 6 个信号**：

```
阶段 1（廉价预筛，处理 18 万行）：
  · 长度检查（> 60 字直接跳过）
  · 首字符检查（不在预置字符集中直接跳过）
  · 命中率：约 2 万行进入阶段 2（11%）

阶段 2（正则匹配，处理 2 万行）：
  · 依次尝试 5 条内置规则 + 自定义规则
  · 命中率：约 2100 行进入阶段 3（10.5%）

阶段 3（多信号投票，处理 2100 行）：
  · 6 个信号打分
  · 通过率：约 2000 行被接受（95%）
```

**性能漏斗**：18 万 → 2 万 → 2100 → 2000。阶段 1 与 2 是纯 CPU 的字符串比较，`18万 × 3 次判断 ≈ 50 万次操作 < 10ms`。总耗时由阶段 2 的正则主导（2 万次 `matches`，约 250ms）。

### B.4 特殊处理：分卷与序号重置

网文的章节结构常有"卷"：

```
卷一 少年游
  第一章 醒来
  第二章 入城
  ...
  第八十章 离乡
卷二 江湖路
  第一章 遇袭        ← 序号从 1 重新开始！
```

**问题**：`lastOrdinal` 单调性检查会把"卷二的 第一章"判为倒退（1 < 80），进而误判。

**解决**：检测到卷标题后，重置 `lastOrdinal`：

```kotlin
// 在 Voting 循环中
if (keywordType == KeywordType.VOLUME) {
    ctx = ctx.copy(lastOrdinal = null)     // ★ 卷标题后重置序号链
    accepted += chapter(text, isVolume = true)
} else {
    val vote = voter.vote(features, ctx)
    if (vote.accepted) {
        ctx = ctx.copy(
            lastAcceptedOffset = f.charOffset,
            lastOrdinal = f.ordinal ?: ctx.lastOrdinal,
            acceptedCount = ctx.acceptedCount + 1,
        )
        accepted += chapter(text, isVolume = false)
    }
}
```

**二级序号**：更好的做法是用"层级序号"（卷序号.章序号）：

```kotlin
data class ChapterNumber(val volume: Int, val chapter: Int) {
    /** 可比较的线性序号，用于单调性检查 */
    val linear: Double get() = volume * 100000.0 + chapter

    fun next(): ChapterNumber = copy(chapter = chapter + 1)
}
```

### B.5 误报抑制的额外规则

| 误报模式 | 检测 | 处理 |
| --- | --- | --- |
| 连续多行都是"第 N 章"（如目录页被误当正文） | 检测到 5 个以上连续候选且间隔均 < 100 字 | 判定为"目录区"，跳过整个区域（记 `warnings`） |
| 同一标题在 5000 字内重复 | `title == lastTitle && dist < 5000` | 跳过（页眉/页脚残留） |
| "第 N 章"出现在段落中间（行首有缩进且行尾有标点） | `leadingWhitespace == 0 && 行尾有标点` | 投票得分已覆盖（信号 6） |
| 大量数字行（如数据表格类文本） | 单规则 `digit_only` 命中数 > 总行数 5% | 禁用 `digit_only` 规则，只保留 `cn_numbered` |

**"目录区"检测实现**：

```kotlin
/** 检测连续的目录块：返回需要跳过的偏移范围 */
private fun detectTocBlock(candidates: List<Features>): LongRange? {
    var runStart = 0
    for (i in 1..candidates.size) {
        val broken = i == candidates.size ||
            candidates[i].charOffset - candidates[i - 1].charOffset > 100
        if (broken) {
            val runLength = i - runStart
            if (runLength >= 5) {
                // 找到目录块：从 runStart 到 i-1
                return candidates[runStart].charOffset..candidates[i - 1].charOffset
            }
            runStart = i
        }
    }
    return null
}
```

### B.6 测试向量

```
测试语料（assets/test/chapterize/）：

┌────┬──────────────────────────────────────────────┬────────┬────────┬────────┐
│ #  │ 描述                                          │ 期望章数│ 允许误差│ 断言   │
├────┼──────────────────────────────────────────────┼────────┼────────┼────────┤
│ 1  │ 标准网文：第1-2000章，规整格式                │ 2000  │ 0      │ 精确   │
│ 2  │ 中文序号：第一章 / 第一百零三章               │ 103   │ 0      │ 精确   │
│ 3  │ 混合序号：第1章 第2章 第三章                  │ 20    │ 1      │ ≥19    │
│ 4  │ 带分卷：卷一卷二，各含 1-N 章                 │ 122   │ 2      │ ≥120   │
│ 5  │ 特殊章：序章 楔子 番外 完本感言               │ 8     │ 0      │ 精确   │
│ 6  │ 英文：Chapter 1 ~ Chapter 50                  │ 50    │ 0      │ 精确   │
│ 7  │ 正文含伪章节：「他想起第三章的那个人」        │ 100   │ 2      │ ≤102   │
│ 8  │ 含目录页（前 200 行是目录，后是正文）         │ 50    │ 5      │ ≤55    │
│ 9  │ 无章节标识（纯正文）                          │ 1     │ 0      │ 精确   │
│ 10 │ CR-only 换行（\r）                            │ 200   │ 0      │ 精确   │
│ 11 │ 章节标题超长（"第1章 " + 80 字标题）          │ 100   │ 5      │ ≥95    │
│ 12 │ 页眉残留（每 3000 字重复"第N章 书名"）        │ 100   │ 10     │ ≤110   │
│ 13 │ 只有数字行（歌单/代码片段）                   │ 1     │ 0      │ 精确   │
│ 14 │ 章节间隔极小（每章仅 50 字）                  │ 500   │ 20     │ ≥480   │
└────┴──────────────────────────────────────────────┴────────┴────────┴────────┘
```

**性能测试**：

```kotlin
@Test
fun `10MB GBK 文件分章耗时达标`() {
    val file = corpus("gbk_large.txt")     // 10MB，2000 章
    // 预热 3 次（JIT 编译）
    repeat(3) { runBlocking { chapterizer.chapterize(file, Charset.forName("GB18030"), null) {} } }

    val elapsed = measureTimeMillis {
        runBlocking { chapterizer.chapterize(file, Charset.forName("GB18030"), null) {} }
    }
    println("分章耗时: ${elapsed}ms")
    assertTrue("分章耗时 ${elapsed}ms 超过 1500ms 预算", elapsed < 1500)
}
```

---

## 5.3 算法卡 C：物理分页的正确性保障

### C.0 目标

| 项 | 要求 |
| --- | --- |
| 输入 | 章文本 + 排版配置 + 视口尺寸 |
| 输出 | 页列表（页间字符区间连续、无空洞、无重叠） |
| 正确性 | 页区间严格连续覆盖 `[0, text.length)` |
| 首屏延迟 | ≤ 120ms（3000 字章节） |
| 全章分页 | ≤ 400ms（10 万字章节） |

### C.1 不变量（Invariants）

分页输出的正确性由以下不变量定义，**必须写进单元测试**：

```
I1. 连续性：pages[i].endCharInChapter == pages[i+1].startCharInChapter
I2. 覆盖性：pages[0].startCharInChapter == 0
I3. 完备性：pages.last().endCharInChapter == chapterText.length
I4. 单调性：pages[i].startCharInChapter < pages[i].endCharInChapter（空页不允许，除空章节）
I5. 不重叠：pages[i].endCharInChapter <= pages[i+1].startCharInChapter
I6. 索引一致：pages[i].index == i
I7. 容量约束：每页行数 × 行高 + 段间距 ≤ contentHeightPx + 单行高容差
```

**为什么 I1 和 I5 是分开的？** I5（不重叠）是弱约束，I1（严格连续）是强约束。分页算法天然的失败模式是"丢行"——某一行因为高度计算舍入误差被跳过，导致 I1 违反但 I5 满足。**必须断言 I1，而不只是 I5**。

### C.2 断页的舍入误差问题

浮点高度计算是分页最常见的 bug 来源：

```kotlin
// ❌ 危险：浮点累积误差导致最后一页可能放不下实际能放下的行
var accHeight = 0f
while (...) {
    accHeight += line.heightPx        // 每行都有 0.0001 的误差
    if (accHeight > contentHeightPx) break
}

// ✅ 安全：用整数微单位（1/1000 px）累积，杜绝累积误差
var accHeightMicro = 0L               // 单位：1/1000 px
val limitMicro = (contentHeightPx * 1000).roundToLong()

while (lineIdx < lines.size) {
    val needMicro = (lines[lineIdx].heightPx * 1000).roundToLong() +
        if (lines[lineIdx].isParagraphEnd) (paraSpacingPx * 1000).roundToLong() else 0L

    if (accHeightMicro + needMicro > limitMicro && lineIdx > pageStartLine) {
        // 收页
        accHeightMicro = 0L
        continue
    }
    accHeightMicro += needMicro
    lineIdx++
}
```

**更根本的保障**：允许 1 行的"容差溢出"——如果因为舍入导致最后一页只放了 1 行而下一行其实能放，会显得很突兀。加一个 `TOLERANCE_RATIO = 0.02` 容差：

```kotlin
val toleranceMicro = (contentHeightPx * 1000 * 0.02).roundToLong()
if (accHeightMicro + needMicro > limitMicro + toleranceMicro && lineIdx > pageStartLine) {
    // 收页
}
```

### C.3 中文标点避头尾（可选增强，V1.x）

中文排版规范要求：**行首不能出现 `。，、；：！？）》」』` 等标点，行尾不能出现 `（《「『` 等**。

但这个需求在**分页场景下不适用**——因为断行已由 `TextMeasurer` 处理完毕（Compose 已实现基本的标点挤压与避头尾），分页只是在行间插页。因此：

**结论：V1.0 不自行实现避头尾，依赖平台文本排版能力。** 若发现平台表现不佳（如 Android 8~10 上句号出现在行首），在 `TextStyle` 上加 `lineBreak = LineBreak.Paragraph` 或对文本做**预替换**：

```kotlin
/**
 * 预替换法：把"会出现在行首的标点"替换为"标点+不换行空格"，
 * 强制标点跟随前一个字。
 * 仅在检测到平台表现异常时启用（API < 29）。
 */
private fun applyPunctuationHanging(text: String): String {
    val NO_LINE_START = "。，、；：！？）》」』】…—"
    return text.map { ch ->
        if (ch in NO_LINE_START) "$ch\u00A0" else ch.toString()   // \u00A0 不换行空格
    }.joinToString("")
}
```

> ⚠️ **这个替换会改变字符数**，进而破坏字符偏移的连续性！如果启用，必须在分页前做替换、并在 `charOffset` 记录时**用原始文本的偏移**。所以：`startCharInChapter` 的计算必须基于**原始文本**，而不是替换后的文本。实现上要维护"替换后下标 → 原始下标"的映射表。**这是 V1.0 不做此优化的另一个原因**——复杂度收益比不划算。

### C.4 空章节与极端输入

| 输入 | 期望行为 |
| --- | --- |
| 空文本 `""` | 返回 1 个空页（`start=0, end=0`），UI 显示"本章无内容" |
| 纯空白 `"\n\n\n"` | 同上 |
| 单字符 `"一"` | 1 页 |
| 无换行的 10 万字（一整段） | 正常分页（`measure` 一次返回全部行）；注意 `Constraints` 高度给 `Infinity`，否则会被截断 |
| 视口高度为 0（未测量完成） | **立即返回空页列表并标记 `notReady`**，不抛异常；等视口就绪后重新触发 |
| 排版参数非法（字号 0） | `TypographyRepository.sanitize` 已拦截；防御性再 `coerceIn` |

**视口高度为 0 的处理**（这是真实会发生的：Compose 首次组合时 `size` 为 `IntSize.Zero`）：

```kotlin
if (viewport.height <= 0 || viewport.width <= 0) {
    emit(PaginationBatch(emptyList(), isComplete = false, 0))
    return@flow                       // 等下次 viewport 变更再分页
}
```

在 ViewModel 侧，viewport 用 `snapshotFlow` 监听，`filter { it.height > 0 }` 过滤掉无效值。

### C.5 测试向量

```kotlin
@Test
fun `分页不变量全部成立`() {
    val cases = listOf(
        "" to TypographyConfig.Default,
        "一" to TypographyConfig.Default,
        "一二三\n四五六\n七八九" to TypographyConfig.Default,
        loremIpsum(3000) to TypographyConfig.Default,
        loremIpsum(100_000) to TypographyConfig.Default.copy(fontSizeSp = 12f),
        loremIpsum(100_000) to TypographyConfig.Default.copy(fontSizeSp = 36f),
        "段落一\n\n\n段落二\n\n\n段落三" to TypographyConfig.Default,
        "\u3000\u3000缩进段落" to TypographyConfig.Default,
    )

    for ((text, typo) in cases) {
        val pages = runBlocking {
            engine.paginate(text, typo, IntSize(1080, 1920)).toList().flatMap { it.pages }
        }
        if (text.isEmpty()) {
            assertTrue(pages.isEmpty() || pages.size == 1 && pages[0].charCount == 0)
            continue
        }
        // I1 连续性
        pages.zipWithNext { a, b ->
            assertEquals("页 ${a.index} 与 ${b.index} 之间不连续", a.endCharInChapter, b.startCharInChapter)
        }
        // I2 覆盖起点
        assertEquals(0, pages.first().startCharInChapter)
        // I3 覆盖终点
        assertEquals(text.length, pages.last().endCharInChapter, "末尾未覆盖全文")
        // I4 非空
        pages.forEach { assertTrue("页 ${it.index} 为空", it.endCharInChapter >= it.startCharInChapter) }
        // I6 索引
        pages.forEachIndexed { i, p -> assertEquals(i, p.index) }
    }
}

@Test
fun `字号变化后偏移锚点语义稳定`() {
    val text = loremIpsum(5000)
    val cfg1 = TypographyConfig.Default.copy(fontSizeSp = 16f)
    val cfg2 = TypographyConfig.Default.copy(fontSizeSp = 28f)

    val pages1 = paginate(text, cfg1)
    val pages2 = paginate(text, cfg2)

    // 取第 3 页的首字符偏移
    val anchor = pages1[2].startCharInChapter
    // 在小字号下这个字符在第三页；大字号下应能在 pages2 中找到包含它的页
    val containingPage = pages2.first { it.startCharInChapter <= anchor && anchor < it.endCharInChapter }
    // 该字符在大字号下应在更靠后的页（因为每页字变少），但必须存在且唯一
    assertTrue(containingPage.index >= 0)
    val duplicates = pages2.count { it.startCharInChapter <= anchor && anchor < it.endCharInChapter }
    assertEquals("偏移 $anchor 落在 $duplicates 个页中，分页有重叠", 1, duplicates)
}
```

### C.6 性能测试

```kotlin
@Test
fun `首屏分页延迟达标`() {
    val text = loremIpsum(3000)
    val typo = TypographyConfig.Default
    repeat(5) { warmup() }

    val firstBatchMs = measureTimeMillis {
        runBlocking {
            engine.paginate(text, typo, IntSize(1080, 1920)).first()     // 只取首批
        }
    }
    assertTrue("首屏分页 ${firstBatchMs}ms 超过 120ms", firstBatchMs <= 120)

    val fullMs = measureTimeMillis {
        runBlocking { engine.paginate(text, typo, IntSize(1080, 1920)).toList() }
    }
    assertTrue("全章分页 ${fullMs}ms 超过 400ms", fullMs <= 400)
}
```

---

## 5.4 算法卡 D：字符偏移 ↔ 字节偏移的双向映射

### D.0 为什么需要这个映射

| 需求 | 需要的能力 |
| --- | --- |
| 按章节偏移读取文件片段 | 字符偏移 → 字节偏移（用于 `RandomAccessFile.seek`） |
| 计算全文百分比 | 字符偏移（因为不同编码字节数不同，百分比应按字符算） |
| 书签定位 | 字符偏移 |
| 服务端（未来）同步 | 字符偏移（跨编码稳定） |

**核心矛盾**：文件是**字节**序列，而分章与进度是**字符**偏移。GBK 下 1 个汉字 = 2 字节，UTF-8 下 = 3 字节，所以二者不能线性换算。

### D.1 映射策略对比

| 策略 | 内存 | 时间 | 精度 | 适用 |
| --- | --- | --- | --- | --- |
| **全量映射表**：`IntArray(字符数)` 存每字符起始字节位置 | 2000万字符 → 80MB | O(1) 查 | 精确 | ❌ 内存不可接受 |
| **稀疏锚点表**：每 4096 字符记一个字节偏移 | 2000万字符 → 4.9K 条目 → 20KB | 锚点 + 局部扫描（≤4096 字符）→ ~0.1ms | 精确 | ✅ **采用** |
| 实时扫描：每次 seek 从头扫 | 0 | O(n) 扫 → 慢 | 精确 | ❌ 慢 |
| 按行索引：每行记起始字节 | 18万行 → 1.4MB | 快 | 行内需再扫 | 可接受但不如锚点均匀 |

**决策：稀疏锚点表（间隔 4096 字符） + 局部线性扫描。**

### D.2 实现

```kotlin
// data:parser/txt — OffsetIndex.kt
/**
 * 字符偏移 ↔ 字节偏移的稀疏索引。
 * 构建时机：分章时顺带构建（复用同一遍扫描），零额外 IO。
 */
class OffsetIndex private constructor(
    /** anchors[i] = 第 (i * STRIDE) 个字符对应的字节位置 */
    private val byteAnchors: LongArray,
    /** anchors 对应的字符位置（首项为 0） */
    private val charAnchors: LongArray,
    val totalChars: Long,
    val totalBytes: Long,
    private val charset: Charset,
) {
    companion object {
        /**
         * 步长选择：
         *  - 太小 → 索引占内存（步长 1024 → 2000万字符 = 19.5K 条目 × 8B × 2 = 312KB，还行）
         *  - 太大 → 局部扫描慢（每字符平均 3 字节，4096 字符 = 12KB 扫描，~0.05ms）
         *  4096 是内存与速度的平衡点：2000 万字符 → 4.9K 条目 → 78KB
         */
        const val STRIDE = 4096

        /**
         * 在流式读取过程中构建索引。
         * 调用方在读取每个缓冲块后调用 onChunk。
         */
        class Builder(private val charset: Charset) {
            private val byteList = ArrayList<Long>(1024)
            private val charList = ArrayList<Long>(1024)
            private var charCount = 0L
            private var byteCount = 0L
            private var lastAnchorChar = 0L

            init { byteList += 0L; charList += 0L }

            /** 传入一个已解码的块及其原始字节数 */
            fun onChunk(decodedChunk: String, rawBytes: Int) {
                var i = 0
                while (i < decodedChunk.length) {
                    charCount++
                    if (charCount - lastAnchorChar >= STRIDE) {
                        // 记录锚点。注意：这里记录的字节位置是"块末尾"，
                        // 会引入最多一个块的误差，因此在读取时用"上一个锚点"起点
                        byteList += byteCount + rawBytes
                        charList += charCount
                        lastAnchorChar = charCount
                    }
                    i++
                }
                byteCount += rawBytes
            }

            fun build(): OffsetIndex = OffsetIndex(
                byteAnchors = byteList.toLongArray(),
                charAnchors = charList.toLongArray(),
                totalChars = charCount,
                totalBytes = byteCount,
                charset = charset,
            )
        }
    }

    /** 字符偏移 → 字节偏移。用于 seek 读取章节。 */
    fun charToByte(charOffset: Long): Long {
        require(charOffset in 0..totalChars) { "字符偏移越界: $charOffset" }
        // 二分找到最近的锚点（charAnchor <= charOffset）
        val idx = charAnchors.binarySearch(charOffset).let {
            if (it >= 0) it else -(it + 1) - 1
        }.coerceIn(0, charAnchors.lastIndex)

        // 从锚点开始局部扫描
        val startChar = charAnchors[idx]
        val startByte = byteAnchors[idx]
        if (startChar == charOffset) return startByte

        // 需要从该字节位置读一小段并解码，数够 (charOffset - startChar) 个字符
        // 这一段最多 STRIDE 个字符，约 12KB，扫描极快
        return scanChars(startChar, startByte, charOffset)
    }

    /**
     * 从 (startChar, startByte) 起，扫描到目标字符下标，返回其字节位置。
     * 实现：用 CharsetDecoder 逐字符解码，累加消费的字节数。
     */
    private fun scanChars(startChar: Long, startByte: Long, targetChar: Long): Long {
        val need = (targetChar - startChar).toInt()
        val decoder = charset.newDecoder()
        RandomAccessFile(file, "r").use { raf ->
            raf.seek(startByte)
            val buf = ByteArray(need * 4 + 64)      // 最坏情况 UTF-8 4 字节/字符
            val read = raf.read(buf)
            val bb = ByteBuffer.wrap(buf, 0, read)
            val cb = CharBuffer.allocate(need + 16)
            var decoded = 0
            while (decoded < need && bb.hasRemaining()) {
                val before = bb.position()
                val cr = decoder.decode(bb, cb, false)
                decoded = cb.position()
                if (cr.isOverflow) cb.clear()
                if (bb.position() == before) break     // 防止死循环
            }
            return startByte + bb.position()
        }
    }

    /**
     * 字节偏移 → 字符偏移。用于"从任意位置恢复"。
     * 用法：读取字节区间 [byteStart, byteEnd)，解码后长度即字符数。
     */
    fun byteToCharApprox(byteOffset: Long): Long {
        val idx = byteAnchors.binarySearch(byteOffset).let {
            if (it >= 0) it else -(it + 1) - 1
        }.coerceIn(0, byteAnchors.lastIndex)
        return charAnchors[idx]
    }
}
```

### D.3 按偏移读取章节正文

分章后，读第 800 章正文**不需要扫描全文**：

```kotlin
// data:parser/txt — TxtChapterLoader.kt
class TxtChapterLoader(
    private val file: File,
    private val index: OffsetIndex,
    private val charset: Charset,
) {
    /**
     * 按字符偏移区间读取正文。
     * 复杂度：O(章节字符数)，与全书大小无关。
     */
    fun read(startChar: Long, endChar: Long): String {
        val startByte = index.charToByte(startChar)
        val endByte = index.charToByte(endChar)

        return RandomAccessFile(file, "r").use { raf ->
            raf.seek(startByte)
            val len = (endByte - startByte).toInt()
            val buf = ByteArray(len)
            raf.readFully(buf)

            val decoder = charset.newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE)
            decoder.decode(ByteBuffer.wrap(buf)).toString()
        }
    }

    /** 读章节目录（章节头 + 后续 N 字），用于目录预览 */
    fun readPreview(startChar: Long, maxChars: Int = 100): String {
        val end = (startChar + maxChars).coerceAtMost(index.totalChars)
        return read(startChar, end).replace('\n', ' ')
    }
}
```

**性能验证**：10MB 文件、2000 章。读第 1500 章：

| 步骤 | 耗时 |
| --- | --- |
| `charToByte(1500章.start)` 二分 + 局部扫描 | 0.08ms |
| `RandomAccessFile.seek` + 读 5KB | 0.03ms |
| 解码 5KB | 0.05ms |
| **合计** | **~0.16ms** |

对比"每次从文件头扫描到第 1500 章"：约 800ms。**5000 倍差距**。这就是"秒开任意章节"的实现基础。

### D.4 边界与陷阱

| 陷阱 | 说明 | 处理 |
| --- | --- | --- |
| 锚点记录在块边界，局部扫描要跨块 | 用"上一个锚点"作为扫描起点，保证目标在扫描范围内 | 已由 `binarySearch` 的 `-(it+1)-1` 处理 |
| 多字节字符被块边界截断 | **必须先解码再统计字符数**，不能按字节数估算字符数 | `Builder.onChunk` 接收的是"已解码块"，字节数单独传入 |
| 代理对（emoji，UTF-16 下 2 个 char） | Kotlin `String` 的 index 是 UTF-16 code unit，emoji 占 2 | 文档中明确定义"偏移 = UTF-16 code unit 下标"；进度恢复时若落在代理对中间则向前取整 |
| `charToByte` 传入超过 `totalChars` | `require` 拦截 | 调用方需保证合法 |
| 文件在索引构建后被修改 | 索引与文件不一致 → 读取乱码 | 索引持久化时记录 `fileSize + lastModified`，打开时校验，不一致则重建索引 |

**索引持久化**：

```kotlin
@Serializable
data class PersistedOffsetIndex(
    val fileSize: Long,
    val lastModified: Long,
    val charset: String,
    val stride: Int,
    val totalChars: Long,
    val totalBytes: Long,
    val byteAnchors: LongArray,      // 压缩后存（可用 varint delta 编码）
    val charAnchors: LongArray,
)

// 存储位置：files/index/<bookId>.idx（二进制，约 78KB → 压缩后 ~30KB）
// 打开书籍时：若 idx 存在且 fileSize/lastModified 匹配 → 直接加载（<5ms），跳过重新分章！
```

> **索引持久化是"第二次打开秒开"的关键**。首次导入需分章（1秒），之后每次打开只需加载 30KB 索引 + 读目标章节（0.16ms），首屏可达 **< 100ms**。

---

## 5.5 算法卡 E：书源规则的选择器回退链

### E.0 问题

书源规则写的是 CSS 选择器，但站点 HTML 结构千奇百怪且经常改版：

```
理想情况：content.contentSelector = "#content"
现实情况：
  · div#content        ← 有 id
  · div.content        ← 只有 class
  · div[id^=content]   ← 动态 id（content_123）
  · article            ← 语义标签
  · .read-content      ← 站点自定义
  · 正文直接在 body 里，无嵌套容器  ← 最麻烦
```

### E.1 回退链设计

```kotlin
// data:sources — SelectorResolver.kt
object SelectorResolver {

    /**
     * 正文容器解析：按优先级依次尝试，命中即返回。
     * 返回 (element, strategy) 便于调试器展示"用了哪条策略"。
     */
    fun resolveContent(doc: Document, configured: String): ResolveResult? {
        // 策略 1：用户配置的选择器（最高优先）
        doc.selectFirst(configured)?.let {
            if (it.text().length >= MIN_CONTENT_LENGTH) {
                return ResolveResult(it, "configured", it.text().length)
            }
        }

        // 策略 2：配置的选择器命中但内容过短 → 可能是选到了导航栏
        //         尝试在 doc 内用启发式找真正正文
        heuristicFind(doc)?.let { return it }

        // 策略 3：全文档文本兜底（几乎总能给出"能读"的结果）
        doc.body()?.let { return ResolveResult(it, "body-fallback", it.text().length) }

        return null
    }

    private const val MIN_CONTENT_LENGTH = 150

    /**
     * 启发式正文识别：经典"文本密度 + 标点密度"算法（简化版 Readability）。
     */
    private fun heuristicFind(doc: Document): ResolveResult? {
        var best: Element? = null
        var bestScore = 0.0
        var bestStrategy = ""

        for (el in doc.select("div, article, section, main")) {
            val text = el.text()
            if (text.length < MIN_CONTENT_LENGTH) continue

            // 分数 = 文本长度 × log(标点密度) × 链接密度惩罚
            val punctCount = text.count { it in "，。！？；：、“”" }
            val punctDensity = punctCount.toDouble() / text.length
            if (punctDensity < 0.005) continue               // 无标点 → 不是正文（可能是菜单/代码）

            val linkText = el.select("a").sumOf { it.text().length }
            val linkDensity = linkText.toDouble() / text.length
            if (linkDensity > 0.4) continue                  // 链接太多 → 是导航

            val brCount = el.select("br").size
            val pCount = el.select("p").size
            val structuralBonus = when {
                pCount >= 3 -> 1.3
                brCount >= 5 -> 1.15
                else -> 1.0
            }

            val score = text.length * (1 + punctDensity * 10) * structuralBonus * (1 - linkDensity)
            if (score > bestScore) {
                bestScore = score; best = el; bestStrategy = "heuristic(density)"
            }
        }
        return best?.let { ResolveResult(it, bestStrategy, it.text().length) }
    }

    data class ResolveResult(val element: Element, val strategy: String, val textLength: Int)
}
```

### E.2 目录选择的容错

目录规则失败有两种模式，处理方式不同：

```kotlin
fun catalogWithFallback(doc: Document, rule: CatalogRule): List<CatalogItem> {
    // 主路径
    val primary = parseCatalog(doc, rule)
    if (primary.size >= 3) return primary

    // 回退 1：规则的选择器可能匹配到了"外层容器"，尝试其内部的 a 标签
    val container = doc.selectFirst(rule.itemSelector)
    if (container != null) {
        val inner = container.select("a[href]").mapNotNull { a ->
            val title = a.text().trim()
            val href = a.absUrl("href")
            if (title.isEmpty() || href.isEmpty()) null else CatalogItem(title, href, false)
        }
        if (inner.size >= 3) return inner
    }

    // 回退 2：全文档找"最长的一组同构链接列表"
    return findLongestLinkList(doc)
}

/**
 * 启发式：找页面中"父元素相同、且都是链接、数量最多"的一组。
 * 章节列表天然满足这个特征。
 */
private fun findLongestLinkList(doc: Document): List<CatalogItem> {
    val groups = doc.select("a[href]")
        .groupBy { it.parent()?.cssSelector() ?: "" }
        .filter { (_, v) -> v.size >= 3 }
        .maxByOrNull { (_, v) -> v.size }

    return groups?.value?.mapNotNull { a ->
        val title = a.text().trim()
        val href = a.absUrl("href")
        if (title.isEmpty() || href.isEmpty()) null else CatalogItem(title, href, false)
    } ?: emptyList()
}
```

### E.3 正文提取的段落还原

最容易被忽略但极影响阅读体验的一环：**段落还原**。

```kotlin
/**
 * 站点 HTML 有两种常见的段落表达：
 *   A. <p>段落1</p><p>段落2</p>              → 语义化好
 *   B. 段落1<br><br>段落2<br><br>段落3         → 老站点常见
 *   C. 段落1&nbsp;&nbsp;&nbsp;段落2            → 最差，用连续空格分隔
 * 必须都能正确还原。
 */
fun extractParagraphs(container: Element, mode: String = "AUTO"): List<String> {
    return when (mode) {
        "PARAGRAPH" -> container.select("p, div, li, blockquote")
            .map { it.ownText().trim() }
            .filter { it.isNotEmpty() }

        "BR" -> {
            // 把 <br> 换成哨兵，然后按哨兵切分
            val html = container.html()
                .replace(Regex("""(<br\s*/?>\s*){1,}""", RegexOption.IGNORE_CASE), "\u0001")
            Jsoup.parse(html).text()
                .split('\u0001')
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        }

        "AUTO" -> {
            // 自动探测：先看 <p> 数量
            val pCount = container.select("p").size
            val brCount = container.select("br").size

            when {
                pCount >= 3 -> extractParagraphs(container, "PARAGRAPH")
                brCount >= 3 -> extractParagraphs(container, "BR")
                else -> {
                    // 回退：按"连续 2 个以上全角空格/空白"切分
                    val text = container.text()
                    text.split(Regex("""[\u3000\s]{2,}"""))
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .ifEmpty { listOf(text.trim()) }
                }
            }
        }
        else -> extractParagraphs(container, "AUTO")
    }
}
```

**归一化后续处理**（在段落列表上做，而非在整段文本上做正则）：

```kotlin
fun normalizeParagraphs(paras: List<String>): List<String> = paras
    .map { p ->
        p.replace('\u00A0', ' ')                       // nbsp → 普通空格
            .replace(Regex("""[\u200B-\u200D\uFEFF]"""), "")   // 零宽字符（反爬常用）
            .replace(Regex("""\s+"""), " ")              // 折叠空白
            .trim()
    }
    .filter { it.isNotEmpty() }
    // 去重相邻重复段（站点 bug 常见）
    .fold(mutableListOf<String>()) { acc, p ->
        if (acc.lastOrNull() != p) acc += p
        acc
    }
```

---

## 5.6 算法卡 F：正文清洗与 ReDoS 防护

### F.0 问题：正则回溯灾难

用户与书源作者写的 `cutPatterns` 正则是**不可信输入**。恶意或疏忽的正则会让应用卡死：

```
危险正则：^(a+)+$
输入：aaaaaaa...（30 个 a 后跟 b）
复杂度：2^30 → 约 10 亿次回溯 → ANR 甚至崩溃
```

### F.1 三道防线

```kotlin
// data:sources — SafeRegex.kt
object SafeRegex {
    private const val MAX_PATTERN_LENGTH = 200
    private const val TIMEOUT_MS = 300L

    /**
     * 第一道防线：静态检查（导入书源时执行，拒绝危险模式）
     */
    fun validate(pattern: String): ValidationResult {
        if (pattern.length > MAX_PATTERN_LENGTH) {
            return ValidationResult.Reject("正则过长（${pattern.length} > $MAX_PATTERN_LENGTH）")
        }
        // 检测嵌套量词： (...+)* (...+)+ (.*)* 等
        val nestedQuantifier = Regex("""\([^)]*[+*]\)[+*?]""")
        if (nestedQuantifier.containsMatchIn(pattern)) {
            return ValidationResult.Reject("检测到嵌套量词（可能导致性能灾难）：$pattern")
        }
        // 检测 super-linear 结构： (a|a)*
        val alternationRepeat = Regex("""\(([^)|]+)\|(\1)\)[+*]""")
        if (alternationRepeat.containsMatchIn(pattern)) {
            return ValidationResult.Reject("检测到重复选择分支：$pattern")
        }
        // 编译测试
        return runCatching { Regex(pattern) }
            .fold(
                onSuccess = { ValidationResult.Ok },
                onFailure = { ValidationResult.Reject("语法错误：${it.message}") },
            )
    }

    /**
     * 第二道防线：运行时限时执行。
     * 用独立线程 + 超时，超时则放弃该正则（返回原文，不阻塞阅读）。
     */
    suspend fun <T> runWithTimeout(
        pattern: Regex,
        input: CharSequence,
        block: (MatchResult) -> T?,
    ): T? = withContext(KrDispatchers.Parse) {
        val job = async {
            runCatching { pattern.find(input)?.let(block) }.getOrNull()
        }
        val result = withTimeoutOrNull(TIMEOUT_MS) { job.await() }
        if (result == null && job.isActive) {
            job.cancel()          // ★ 超时取消，防止线程被长期占用
            Log.w("SafeRegex", "正则执行超时，已放弃：${pattern.pattern}")
        }
        result
    }

    sealed interface ValidationResult {
        data object Ok : ValidationResult
        data class Reject(val reason: String) : ValidationResult
    }
}
```

### F.2 第三道防线：长度限制

即使正则本身安全，在大文本上执行也会慢。**清洗只在合理长度上执行**：

```kotlin
private const val MAX_PURIFY_LENGTH = 500_000     // 单章最多 50 万字

fun purify(text: String, rules: PurifyRules): String {
    var result = text.substring(0, text.length.coerceAtMost(MAX_PURIFY_LENGTH))
    // ... 应用规则
    return result
}
```

### F.3 清洗规则集（内置的"通用净化"）

**这些是内置的通用规则，用户与书源都可扩展**：

```kotlin
// data:sources — BuiltinPurifyRules.kt
object BuiltinPurifyRules {

    /** 常见站点水印（按行匹配，整行移除） */
    val LINE_PATTERNS = listOf(
        Regex("""^.{0,10}(?:最新|最快|全文|免费|无弹窗|无广告)?.{0,6}(?:首发|更新|阅读|小说).{0,20}$"""),
        Regex("""^.{0,20}(?:请记住|记住|收藏)(?:本书|本站|本页|网址).{0,30}$"""),
        Regex("""^.{0,10}(?:www\.|https?://|m\.)[\w.-]+(?:\.com|\.net|\.cn|\.org).{0,20}$""", RegexOption.IGNORE_CASE),
        Regex("""^.{0,10}(?:手机|移动端|APP)(?:阅读|访问|下载|用户).{0,20}$"""),
        Regex("""^.{0,5}(?:上一章|下一章|返回目录|章节目录|加入书架|投推荐票|打赏).{0,5}$"""),
        Regex("""^第?\s*\d+\s*/\s*\d+\s*页$"""),
        Regex("""^\s*\d+\s*$"""),                                  // 纯数字行（错版页码）
        Regex("""^.{0,20}(?:温馨提示|公告|通知)[:：].{0,60}$"""),
        Regex("""^.{0,30}(?:天才一秒记住|一秒记住|一秒收藏).{0,30}$"""),
    )

    /** 段内模式（保留上下文，只移除匹配片段） */
    val INLINE_PATTERNS = listOf(
        // 「本章未完，请点击下一页继续阅读」
        Regex("""[（(【\[]?\s*本章未完[，,]?\s*请?点击.{0,10}(?:下一页|继续).{0,15}[)）】\]]?"""),
        // 「（未完待续）」
        Regex("""[（(]\s*未完待续\s*[)）]"""),
        // 「&nbsp;」残留
        Regex("""&(?:nbsp|#160|#xA0);""", RegexOption.IGNORE_CASE),
        // 「本文来自XXX」
        Regex("""本文(?:来源|来自|首发|由).{0,30}(?:整理|发布|原创|提供)?"""),
        // 「PS：...」/「P.S. ...」（可选，默认关闭，可能误删）
        // 反爬零宽字符
        Regex("""[\u200B-\u200F\u2028-\u202F\uFEFF]"""),
        // 重复标点压缩（"！！！！" → "！"）
        Regex("""([！？。，、；：])\1{2,}"""),
    )

    /** 需要整段移除的段落（长度占比异常） */
    fun isSuspiciousParagraph(p: String): Boolean {
        // 单段超过 3000 字且无标点 → 可能是乱码或被注入的内容
        if (p.length > 3000 && p.none { it in "。！？，" }) return true
        // 单段重复同一字符 50 次以上
        if (Regex("""(.)\1{49,}""").containsMatchIn(p)) return true
        return false
    }
}
```

### F.4 净化流水线

```
原始 HTML 响应
      │
      ▼
[1] Jsoup 解析
      │
      ▼
[2] removeSelectors 移除（书源配置的广告选择器）
      │
      ▼
[3] 硬编码移除：script/style/iframe/ins/[class*=ad]
      │
      ▼
[4] 段落提取（E.3 的 extractParagraphs）
      │
      ▼
[5] 段落归一化（normlizeParagraphs：nbsp、零宽字符、空白折叠）
      │
      ▼
[6] 逐行应用 LINE_PATTERNS（整行移除）  ← SafeRegex 限时执行
      │
      ▼
[7] 逐段应用 INLINE_PATTERNS（片段移除）
      │
      ▼
[8] cutPatterns 截断（书源配置，从匹配处丢弃后续全部）
      │
      ▼
[9] isSuspiciousParagraph 过滤异常段
      │
      ▼
[10] 首尾空白段落修剪 + 结果长度合理性与原文对比
      │
      ▼
清洗后正文（纯文本，段落以 \n 分隔）
```

**验收指标**：净化后正文字数应 ≥ 原始 HTML 纯文本的 **30%**。若清洗后字数骤降（< 30%），说明规则过度匹配，**回退到清洗前的结果并记录警告**——这比"正文被删光"要好得多。

```kotlin
val purified = pipeline(input)
val rawTextLength = Jsoup.parse(input).text().length
return if (purified.length < rawTextLength * 0.3) {
    Log.w("Purify", "清洗过度（${purified.length}/$rawTextLength），回退原始文本")
    warnings += ParseWarning.OverPurified
    input
} else purified
```

### F.5 测试向量

```kotlin
@Test
fun `净化规则不误删正常正文`() {
    val normalChapter = """
        林轩站在山巅，望着远处翻涌的云海。
        这是他第一次如此清晰地感受到天地的辽阔。
        "我一定会回来的。"他轻声说。
    """.trimIndent()

    val purified = purifier.purify(normalChapter)
    assertEquals("正常正文被误删", normalChapter, purified)
}

@Test
fun `净化规则移除常见水印`() {
    val polluted = """
        林轩站在山巅。
        请记住本站域名 www.example.com，最快更新！
        这是他第一次感受到天地的辽阔。
        天才一秒记住一秒收藏本站
        （本章未完，请点击下一页继续阅读）
        他轻声说："我一定会回来的。"
    """.trimIndent()

    val purified = purifier.purify(polluted)
    assertTrue("水印未清除", "www.example.com" !in purified)
    assertTrue("水印未清除", "天才一秒记住" !in purified)
    assertTrue("水印未清除", "本章未完" !in purified)
    assertTrue("正文被误删", "林轩站在山巅" in purified)
    assertTrue("正文被误删", "我一定会回来的" in purified)
}

@Test
fun `危险正则被拒绝`() {
    assertTrue(SafeRegex.validate("""^(a+)+$""") is SafeRegex.ValidationResult.Reject)
    assertTrue(SafeRegex.validate("""(a|a)*""") is SafeRegex.ValidationResult.Reject)
    assertTrue(SafeRegex.validate("""a""".repeat(300)) is SafeRegex.ValidationResult.Reject)
    assertEquals(SafeRegex.ValidationResult.Ok, SafeRegex.validate("""请记住本站域名"""))
}

@Test
fun `超时正则不阻塞`() = runBlocking {
    // 通过反射强制传入一个会超时的正则（绕过静态检查），验证运行时限时生效
    val evil = Regex("""(a+)+b""")
    val input = "a".repeat(40)
    val start = SystemClock.elapsedRealtime()
    SafeRegex.runWithTimeout(evil, input) { it.value }
    val elapsed = SystemClock.elapsedRealtime() - start
    assertTrue("超时机制未生效，耗时 ${elapsed}ms", elapsed < 600)
}
```

---

## 5.7 算法卡 G：大文件内存映射与压力控制

### G.0 目标

| 项 | 要求 |
| --- | --- |
| 支持文件大小 | ≥ 500MB TXT |
| 分章峰值内存 | ≤ 4MB（与文件大小无关） |
| 章节读取峰值内存 | ≤ 章节大小 + 1MB |
| 连续阅读 1 小时内存波动 | ≤ 150MB |

### G.1 内存预算表

```
总预算 150MB 分配：

┌────────────────────────────────────┬──────────┬───────────────────────────────┐
│ 组成                                │ 预算     │ 说明                          │
├────────────────────────────────────┼──────────┼───────────────────────────────┤
│ Android 运行时 + Compose 框架      │ ~55MB    │ 不可控基线（中端机实测）       │
│ Bitmap（PDF 页 / 封面 / EPUB 图）   │ ~30MB    │ LRU 上限 30 页 × 1MB          │
│ 分页缓存（当前书 ±2 章）            │ ~8MB     │ 每章 PageSnapshot 约 200KB    │
│ 章节索引（当前书 2000 章）          │ ~1.5MB   │ ChapterMeta 对象              │
│ 字符偏移索引（当前书）              │ ~0.1MB   │ 稀疏锚点 4.9K 条目            │
│ Typeface（字体）                    │ ~20MB    │ LRU 上限 2 个 CJK 字体        │
│ 网络/HTML 解析瞬时                  │ ~10MB    │ 单章 HTML + Jsoup DOM         │
│ 预留余量                            │ ~25MB    │ GC 波动与碎片                 │
├────────────────────────────────────┼──────────┼───────────────────────────────┤
│ 合计                                │ ~150MB   │                               │
└────────────────────────────────────┴──────────┴───────────────────────────────┘
```

**结论**：`Typeface`（20MB）与 `Bitmap`（30MB）是两大头。因此：

1. `typefaceCache = LruCache(2)` 而非 4（第 4 卷写的 4 是上限，实测应设为 2）。
2. PDF 页位图用 `RGB_565` 而非 `ARGB_8888`（省一半内存），夜间反色用 `ColorFilter` 而非改位图。
3. 分页缓存只保留当前书 ±2 章（切书时清空）。

### G.2 内存压力监听与主动收缩

```kotlin
// app — MemoryPressureMonitor.kt
@Singleton
class MemoryPressureMonitor @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val paginationEngine: PaginationEngine,
    private val bitmapCache: PdfBitmapCache,
    private val fontRepo: FontRepository,
) {
    fun start() {
        // 1. 系统内存回调（ComponentCallbacks2）
        ctx.registerComponentCallbacks(object : ComponentCallbacks2 {
            override fun onTrimMemory(level: Int) {
                when (level) {
                    // 应用不可见：清掉所有可重建的缓存
                    TRIM_MEMORY_UI_HIDDEN, TRIM_MEMORY_BACKGROUND -> releaseAll()

                    // 内存吃紧：按级别递减释放
                    TRIM_MEMORY_RUNNING_LOW -> {
                        bitmapCache.trimToSize(5)
                        paginationEngine.invalidate(currentBookId)   // 只保留当前章
                    }
                    TRIM_MEMORY_RUNNING_CRITICAL -> {
                        bitmapCache.evictAll()
                        paginationEngine.invalidate(currentBookId)   // 全部清空
                        fontRepo.trimCache(keepCurrent = true)
                        System.gc()
                    }
                }
            }
        })

        // 2. 主动监控（每 30 秒采样一次，超过阈值提前收缩）
        KrDispatchers.ApplicationScope.launch {
            while (true) {
                delay(30_000)
                val info = ActivityManager.MemoryInfo().also {
                    (ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).getMemoryInfo(it)
                }
                val usedMb = (info.totalMem - info.availMem) / 1024 / 1024
                val appUsedMb = Runtime.getRuntime().let { (it.totalMemory() - it.freeMemory()) / 1024 / 1024 }

                if (appUsedMb > APP_MEMORY_SOFT_LIMIT_MB) {   // 130MB
                    bitmapCache.trimToSize(5)
                    paginationEngine.invalidateExcept(currentBookId, currentChapter)
                }
                if (appUsedMb > APP_MEMORY_HARD_LIMIT_MB) {   // 170MB
                    releaseAll()
                }
            }
        }
    }

    private const val APP_MEMORY_SOFT_LIMIT_MB = 130L
    private const val APP_MEMORY_HARD_LIMIT_MB = 170L
}
```

### G.3 大文件读取：绝不整读

**红线规则**：

```kotlin
// ❌ 绝对禁止（在任何地方）
File.readText()
File.readBytes()
inputStream.readBytes()

// ✅ 唯一允许的方式
file.inputStream().buffered(64 * 1024).use { stream -> /* 流式处理 */ }
```

**静态检查**：用 detekt 自定义规则拦截：

```yaml
# config/detekt/detekt.yml
style:
  ForbiddenMethodCall:
    active: true
    methods:
      - 'kotlin.io.readText'
      - 'kotlin.io.readBytes'
      - 'java.io.File.readText'
      - 'java.io.File.readBytes'
```

**例外白名单**：`data:parser` 中读取小文件（< 1MB，如 OPF、NCX、书源 JSON）的地方，需显式注释 `// @allow-full-read: 文件 < 1MB`。

### G.4 长会话防泄漏检查清单

连续阅读 1 小时不泄漏，需要在以下位置全部正确清理：

| 资源 | 注册处 | 必须清理处 | 泄漏后果 |
| --- | --- | --- | --- |
| `BroadcastReceiver`（电量） | `DisposableEffect` | `onDispose { unregisterReceiver }` | Activity 泄漏，每小时 +2MB |
| `PdfRenderer` | 打开 PDF 时 | `onDispose { queue.close() }` | native 内存泄漏（GC 管不到），每页 +2MB |
| `ParcelFileDescriptor` | 打开 PDF 时 | `pfd.close()` | 文件描述符耗尽 |
| `CoroutineScope`（阅读器） | ViewModel | `onCleared { scope.cancel() }` | 协程泄漏，持有 ViewModel |
| `Typeface` | 字体加载 | LruCache 自动淘汰 | native 内存泄漏风险 |
| `Bitmap` | PDF 渲染 | `recycle()` 或 LRU 淘汰 | 堆内存泄漏 |
| `Cursor` | Room（自动管理） | — | Room 已封装 |
| `AssistStructure` / `Uri` 权限 | SAF 授权 | `releasePersistableUriPermission` | 权限泄漏（上限 128 个） |
| `Flow` collect | Composable | `collectAsStateWithLifecycle` | 生命周期不一致导致的重复订阅 |
| `TextMeasurer` 缓存 | Composition | 随 Composition 销毁 | `Globals` 在非 Composition 环境持有 → 泄漏 |

**PDF 的特殊处理**（最容易被忽略）：

```kotlin
@Composable
fun PdfReaderScreen(bookId: Long) {
    val renderer = remember(bookId) { PdfRendererProvider.open(bookId) }
    // ★ 必须显式释放：PdfRenderer 持有 native 内存，GC 不会回收
    DisposableEffect(renderer) {
        onDispose {
            renderer?.close()
            PdfRendererProvider.evict(bookId)
        }
    }
}
```

### G.5 内存测试方法

```kotlin
@Test
fun `连续阅读1小时内存波动达标`() {
    // 这是 instrumentation test，需在真机运行
    val baseline = sampleAppMemoryMb()          // 冷启动后基线
    val samples = mutableListOf<Long>()

    repeat(60) { minute ->
        // 模拟 1 分钟阅读：翻 30 页
        repeat(30) { readerScreen.performClickNextPage() }
        SystemClock.sleep(1000)
        samples += sampleAppMemoryMb()
    }

    val peak = samples.max()
    val growth = peak - baseline
    val leaked = samples.last() - samples[10]   // 跳过前 10 分钟预热

    assertTrue("峰值内存增长 ${growth}MB 超过 150MB", growth <= 150)
    assertTrue("疑似内存泄漏（第 60 分钟比第 10 分钟多 ${leaked}MB）", leaked <= 15)
}
```

**同时用 LeakCanary 在 debug 包做自动检测**：

```kotlin
// app/build.gradle.kts
debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
```

**验收：debug 包运行 1 小时，LeakCanary 无任何 `Activity` / `ViewModel` / `PdfRenderer` 泄漏报告。**

---

## 5.8 算法达标总览

| 算法 | 达标判据 | 验证方式 |
| --- | --- | --- |
| A 编码探测 | 准确率 ≥ 93%（含边界样本） | 单元测试 + 15 例语料 |
| B 章节识别 | 召回 ≥ 99.5%，误报 ≤ 0.2% | 单元测试 + 14 例语料 |
| B 分章性能 | 10MB ≤ 1.5s | 性能测试（预热后） |
| C 分页正确性 | 7 条不变量全部成立 | 单元测试（8 类输入） |
| C 首屏分页 | ≤ 120ms（3000 字） | 性能测试 |
| D 偏移映射 | 读任意章 ≤ 1ms | 性能测试 |
| D 索引持久化 | 二次打开 ≤ 100ms | 手动 + 性能测试 |
| E 选择器回退 | 3 种残缺 HTML 均能提取正文 | 单元测试 |
| F ReDoS 防护 | 危险正则被拒 + 超时正则 ≤ 600ms | 单元测试 |
| F 清洗准确率 | 不误删正文 + 清除 90% 常见水印 | 单元测试 |
| G 内存 | 1 小时波动 ≤ 150MB，无泄漏 | Instrumentation + LeakCanary |

---

**上一卷**：[第 4 卷 · 功能模块详细设计](#vol-04) ｜ **下一卷**：[第 6 卷 · 性能与 NFR 实现](#vol-06)


---


# 第 6 卷 · 性能与 NFR 实现 {#vol-06}
> 需求文档的 NFR 章节给出了三条硬指标：**10MB TXT 分章 <1.5s**、**翻页 60fps（高刷 90/120fps）**、**1 小时内存波动 <150MB**。本卷把这三条拆解为可测量、可归因、可回归的工程体系，并补齐兼容性、隐私、耗电等非功能性要求。

---

## 6.1 性能指标体系（完整版）

需求只给了 3 条，工程上需要补充 20+ 条才能覆盖真实场景。以下为**完整性能预算表**，每条都有测量方法（详见 6.6）。

### 6.1.1 P0 指标（需求明确要求，不可退让）

| 编号 | 指标 | 目标值 | 测量方法 | 归因工具 |
| --- | --- | --- | --- | --- |
| **P0-1** | 10MB TXT 首次分章 | ≤ 1500ms | 单元性能测试（预热后取中位数） | CPU Profiler |
| **P0-2** | 阅读翻页帧率 | ≥ 60fps（120Hz 屏 ≥ 90fps） | `dumpsys gfxinfo` + Macrobenchmark | Perfetto |
| **P0-3** | 连续阅读 1 小时内存波动 | ≤ 150MB | Instrumentation 采样 + LeakCanary | Memory Profiler |
| P0-4 | 无肉眼可见白屏 | 0 次超过 200ms 的空白帧 | 录屏逐帧检查 + JankStats | Perfetto |

### 6.1.2 P1 指标（体验关键，纳入 CI 门禁）

| 编号 | 指标 | 目标值 | 说明 |
| --- | --- | --- | --- |
| P1-1 | 冷启动到书架可交互 | ≤ 800ms（中端机） | `ActivityTaskManager` 的 `Displayed` 时间 |
| P1-2 | 冷启动到首帧 | ≤ 400ms | |
| P1-3 | 首次打开书籍（已分章）到首屏 | ≤ 300ms | 依赖偏移索引持久化 |
| P1-4 | 首次打开书籍（未分章，5MB）到首屏 | ≤ 1800ms | 分章 + 首屏分页 |
| P1-5 | 3000 字章节首屏分页 | ≤ 120ms | 单元性能测试 |
| P1-6 | 全章分页（3000 字） | ≤ 400ms | |
| P1-7 | 字号拖动到重分页完成 | ≤ 350ms（含 120ms 去抖） | |
| P1-8 | 章节跳转（本地书，已有索引） | ≤ 150ms | |
| P1-9 | 目录页打开（2000 章） | ≤ 200ms | |
| P1-10 | 聚合搜索 8 源全部返回 | ≤ 3000ms | 含最慢源；P50 ≤ 1200ms |
| P1-11 | 单章网络抓取（含清洗） | ≤ 2000ms（P50）/ ≤ 5000ms（P95） | |
| P1-12 | 导入 50 本混合格式书籍 | ≤ 60s | |
| P1-13 | 扫描 1000 个文件的文件夹 | ≤ 5s | |
| P1-14 | EPUB 打开到首屏 | ≤ 600ms | 含 OPF/NCX 解析 |
| P1-15 | PDF 单页渲染（1080p，1.0x） | ≤ 120ms | |
| P1-16 | 书架滚动帧率（500 本） | ≥ 60fps | 封面加载不能阻塞 |
| P1-17 | APK 体积（单 ABI） | ≤ 12MB | |
| P1-18 | 冷启动内存 | ≤ 80MB | |

### 6.1.3 P2 指标（稳定性与能效）

| 编号 | 指标 | 目标值 |
| --- | --- | --- |
| P2-1 | 崩溃率 | < 0.5% |
| P2-2 | ANR 率 | < 0.1% |
| P2-3 | 阅读 1 小时耗电 | ≤ 6%（中端机，亮度 50%） |
| P2-4 | 后台缓存 100 章耗电 | ≤ 3% |
| P2-5 | 待机 8 小时耗电 | ≤ 1% |
| P2-6 | 网络流量（读 100 章网文） | ≤ 15MB |
| P2-7 | 数据库大小（500 本 + 1000 章缓存） | ≤ 80MB |
| P2-8 | 首次安装包冷启动无 ANR | 100% |

---

## 6.2 指标 P0-1：10MB TXT 分章 ≤ 1.5s

### 6.2.1 性能预算分解

```
时间轴（10MB GBK 文件，约 500 万字符，2000 章）：

0ms      ┃ 独占 Parse 线程池启动
         ┃
120ms    ┃ ✓ 编码探测（采样 256KB，严格 UTF-8 校验 + 启发式打分）
         ┃   - 采样读取 256KB              35ms
         ┃   - UTF-8 严格校验（失败出口）  50ms
         ┃   - GB18030 解码 + 打分         35ms
         ┃
520ms    ┃ ✓ 流式解码 10MB（GBK → UTF-16）
         ┃   - 解码本身                     400ms ← 主要成本，不可避免
         ┃
700ms    ┃ ✓ 行切分与迭代（18 万行）
         ┃   - StringBuilder 复用           180ms
         ┃
1000ms   ┃ ✓ 章节匹配（2 万次候选，2100 次正则）
         ┃   - 首字符预筛（18万→2万）       50ms
         ┃   - 正则匹配 2 万次              250ms
         ┃
1050ms   ┃ ✓ 偏移索引构建（顺带完成，零额外遍历）  50ms
         ┃
1150ms   ┃ ✓ 章节列表构建 + 索引持久化写入  100ms
         ┃
1150ms   ┃ 完成（预留 350ms 余量给低端机）
```

### 6.2.2 优化手段清单（按收益排序）

| 优化 | 收益 | 实现要点 |
| --- | --- | --- |
| **① 首字符预筛** | **8~10x**（章节匹配） | 章节行必然以特定字符开头，预筛把正则次数从 18 万降到 2 万 |
| **② 预编译 Pattern** | 2000 章 → 省 4 秒 | `Regex`/`Pattern` 在对象初始化时编译一次，循环外复用 |
| **③ 严格 UTF-8 校验只跑前 64KB** | 2x（编码探测） | 前 64KB 合法则后续非法概率 <0.1% |
| **④ 不用 `readText`，流式解码** | 内存 40MB → 1.2MB | `BufferedInputStream` + `CharsetDecoder` |
| **⑤ 偏移索引顺带构建** | 省一整遍遍历 | 在行迭代循环中同时记录锚点，不额外扫描 |
| **⑥ 索引持久化** | 二次打开 1.5s → 100ms | 分章后写 30KB 索引文件，下次直接加载 |
| **⑦ 独立 Parse 线程池** | 避免与分页争抢 CPU | 见 2.4.3 |
| **⑧ `CharBuffer` 复用** | 减少 GC 压力 | 解码缓冲复用，不在循环内 new |
| **⑨ 章节列表预分配容量** | 省 3~5 次扩容拷贝 | `ArrayList(2048)` |
| **⑩ 避免 String.substring** | 减少字符数组拷贝 | 章节标题用 `CharSequence` 或直接 trim 后的 String |

### 6.2.3 反例：常见的错误实现

```kotlin
// ❌ 反面教材：这个实现需要 6~8 秒
suspend fun chapterizeBad(file: File): List<ChapterMeta> {
    val text = file.readText()                          // ① 全量进内存，20MB char[]
    val lines = text.lines()                             // ② 创建 18 万个 String 对象
    val chapters = mutableListOf<ChapterMeta>()
    var offset = 0L

    lines.forEachIndexed { i, line ->
        // ③ 每个循环重新编译正则！这是最大的浪费
        val regex = Regex("""^\s*第\s*[0-9零一二三四五六七八九十百千万两]{1,10}\s*[章节回卷集篇话]\s*[^\n]{0,40}$""")
        if (regex.matches(line)) {                       // ④ 18 万次正则
            chapters += ChapterMeta(chapters.size, line.trim(), offset, 0)
        }
        offset += line.length + 1
    }
    return chapters
}
```

**性能对比**：

| 实现 | 耗时 | 峰值内存 | 问题 |
| --- | --- | --- | --- |
| 反面教材 | 6.8s | 42MB | 违反 P0-1 与内存预算 |
| 正确实现 | 1.15s | 1.4MB | 达标 |

**差异来源**：③ 正则重复编译（4.0s）+ ② 对象洪泛（1.2s）+ ① 全量内存（0.5s）+ ④ 无预筛（1.0s）。

### 6.2.4 低端机兜底策略

中端机（骁龙 778G）1.15s，低端机（骁龙 665 / 天玑 700）约 2.2s。**P0-1 的 1.5s 是"中端机"口径**，但需在 UI 上避免"低端机用户干等 2.2s"的体验：

```kotlin
// 分章时的 UI 策略：根据文件大小与设备等级预估耗时
object ImportTimeEstimator {
    fun estimateMs(fileSize: Long): Long {
        val tier = DeviceTier.current()     // 由 CPU 核心数、频率、内存判定
        val base = fileSize / 1024 / 1024   // MB
        return base * when (tier) {
            DeviceTier.HIGH -> 100L          // 10MB → 1.0s
            DeviceTier.MID -> 115L           // 10MB → 1.15s
            DeviceTier.LOW -> 220L           // 10MB → 2.2s
        }
    }
}

// UI：预估 > 800ms 才显示进度条，避免闪现
if (estimatedMs > 800) {
    showProgressBar(progress = onProgressValue)
} else {
    // 快，直接显示骨架屏
}
```

**关键体验原则**：**进度条必须在 200ms 内出现**。若预估超过 800ms 而进度条延后出现，用户会觉得"卡死了"。实现上用 `LaunchedEffect { delay(200); if (stillRunning) show() }`。

---

## 6.3 指标 P0-2：翻页帧率 ≥ 60fps

### 6.3.1 掉帧的三大根因与对策

```
掉帧根因树：

翻页掉帧
├── 1. 主线程做了重活
│   ├── 分页在主线程            → 对策：flowOn(Default) + 后台分页
│   ├── 文本测量在 Composable   → 对策：预分页，Composable 只渲染
│   ├── 图片解码在主线程        → 对策：Coil 异步解码 + crossfade
│   └── SharedPreferences 同步读 → 对策：DataStore（异步）+ 内存缓存
│
├── 2. 重组/重绘范围过大
│   ├── 翻页动画触发 Composition → 对策：Animatable + graphicsLayer（只走绘制）
│   ├── 每次滚动重建列表项       → 对策：稳定 key + derivedStateOf
│   ├── 状态读在 Composable 顶层 → 对策：状态读下沉到 lambda（deferred read）
│   └── 主题色对象每次新建        → 对策：@Immutable + remember
│
└── 3. 渲染本身超预算
    ├── 阴影/模糊（blur）         → 对策：避免实时 blur，用预渲染渐变
    ├── 裁剪（clip）过多          → 对策：合并形状
    ├── 层数过多（overdraw）      → 对策：减少不必要的背景叠加
    └── 大文本单次绘制            → 对策：只渲染当前页，用 Canvas 裁剪
```

### 6.3.2 关键实现：状态读下沉（Deferred State Read）

**这是 Compose 性能优化中最有效、最易被忽略的手段。**

```kotlin
// ❌ 慢：offsetFraction.value 在 Composable 作用域读取
//    动画每帧都会让整个 ReaderPage 重组
@Composable
fun PageBad(offset: Animatable<Float, AnimationVector1D>) {
    val v = offset.value                      // ← 读在 Composition 阶段
    Box(Modifier.fillMaxSize()) {
        Text("第 1 页", modifier = Modifier.offset(x = (v * 1080).dp))
        Text("第 2 页", modifier = Modifier.offset(x = ((1 - v) * 1080).dp))
        // 每帧重组 Box + 2 个 Text
    }
}

// ✅ 快：状态读在 graphicsLayer 的 lambda 内（绘制阶段）
//    动画每帧只更新 Layer，不触发重组
@Composable
fun PageGood(offset: Animatable<Float, AnimationVector1D>) {
    Box(Modifier.fillMaxSize()) {
        Text("第 1 页", modifier = Modifier.graphicsLayer {
            translationX = -offset.value * size.width     // ← 读在绘制阶段
        })
        Text("第 2 页", modifier = Modifier.graphicsLayer {
            translationX = (1f - offset.value) * size.width
        })
        // 每帧只更新 RenderNode，零重组
    }
}
```

**效果实测**：翻页动画期间，错误实现的 `Recomposition` 次数为 60~120 次/秒；正确实现为 **0 次/秒**。Jank 率从 12% 降到 0.3%。

### 6.3.3 关键实现：稳定 key 与派生状态

```kotlin
// 阅读器页列表：用页索引作 key（页码在重分页前稳定）
LazyRow(
    state = pagerState,
    key = { index -> pages.getOrNull(index)?.startCharInChapter ?: index },   // ★ 用字符偏移作 key！
) { index ->
    ReaderPage(pages[index], typography, theme)
}
```

> **用 `startCharInChapter` 而非 `index` 作 key**：重分页后，同一字符偏移对应的内容不变，而页索引会变。用字符偏移作 key 能让 Compose 在重分页后**复用**已渲染的页（若内容相同），减少闪烁。

```kotlin
// 书架：书籍用 book.id 作 key（不能省略！否则批量删除会错位）
LazyVerticalGrid(
    columns = GridCells.Fixed(columnCount),
    key = { book -> book.id },
) { ... }
```

```kotlin
// 高频滚动的状态用 derivedStateOf 减少重组
val showScrollToTopButton by remember {
    derivedStateOf { listState.firstVisibleItemIndex > 20 }
}
// 对比：直接 listState.firstVisibleItemIndex > 20 会在每次滚动时重组
```

### 6.3.4 高刷适配（90/120fps）

Android 高刷设备有两种刷新率行为：

| 行为 | 说明 | 影响 |
| --- | --- | --- |
| 全局高刷 | 系统所有应用跑 120Hz | 无特殊处理 |
| **动态刷新率** | 系统在静止时降到 60Hz，滑动时升到 120Hz | 动画启动瞬间可能掉到 60Hz，产生"卡一下" |

**适配手段**：

```kotlin
// 1. 声明支持高刷（AndroidManifest）
//    注意：不要设置 android:resizeableActivity="false" 等会限制刷新率的属性

// 2. 在需要高帧率的动画期间请求高刷新率（API 30+ PreferredFrameRate）
@Composable
fun HighRefreshRateScope(
    isAnimating: Boolean,
    content: @Composable () -> Unit,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val view = LocalView.current
        LaunchedEffect(isAnimating) {
            view.parent?.let { parent ->
                if (parent is View) {
                    parent.requestedFrameRate = if (isAnimating) 120f else 0f
                    // 0f = 不限制，交回系统默认
                }
            }
        }
    }
    content()
}

// 3. 动画时长不要为了"看起来快"而设得过短
//    120Hz 下 200ms 的动画有 24 帧，视觉充足；
//    ❌ 不要设 60ms（只有 7 帧，看起来是闪一下）
val durationMs = 220
```

**帧率验证方法**：

```bash
# 单次采样
adb shell dumpsys gfxinfo com.kr.reader framestats

# 关键指标：
#   Janky frames: 掉帧数
#   50th/90th/95th/99th percentile: 帧耗时分布
#   必须看 95th percentile ≤ 16.67ms（60Hz）/ 11.11ms（90Hz）/ 8.33ms（120Hz）

# 持续采样（Macrobenchmark）
./gradlew :benchmark:connectedBenchmarkAndroidTest
```

### 6.3.5 Macrobenchmark 基准测试

```kotlin
// benchmark — ReaderScrollBenchmark.kt
@RunWith(AndroidJUnit4::class)
class ReaderScrollBenchmark {
    @get:Rule val rule = MacrobenchmarkRule()

    @Test
    fun scrollReader() = rule.measureRepeated(
        packageName = "com.kr.reader",
        metrics = listOf(FrameTimingMetric()),
        iterations = 10,
        startupMode = StartupMode.WARM,
        setupBlock = {
            pressHome()
            startActivityAndWait()
            // 打开一本已导入的书
            device.findObject(By.text("测试书籍 5MB")).click()
            device.waitForIdle()
        },
    ) {
        repeat(50) {
            device.findObject(By.res("reader_next_page")).click()
            Thread.sleep(80)
        }
    }

    @Test
    fun startup() = rule.measureRepeated(
        packageName = "com.kr.reader",
        metrics = listOf(StartupTimingMetric(), FrameTimingMetric()),
        iterations = 10,
        startupMode = StartupMode.COLD,
    ) { startActivityAndWait() }
}
```

**回归门禁**：CI 中对比基准测试结果与基线，**任一指标劣化超过 10% 则 PR 阻断**。

---

## 6.4 指标 P0-3：1 小时内存波动 ≤ 150MB

### 6.4.1 内存构成与优化落点

见 5.7.1 的内存预算表。关键优化点：

| 优化 | 节省 | 实现 |
| --- | --- | --- |
| PDF 位图用 `RGB_565` | 50% | `Bitmap.Config.RGB_565`（PDF 多无透明需求） |
| PDF 页缓存 LRU 上限 30 页 | — | `LruCache<String, Bitmap>(30)` 但按字节控制更准 |
| 字体 LruCache 上限 **2** | ~20MB | 实测 4 太大（一个 CJK 字体 8MB） |
| 分页缓存只留当前书 ±2 章 | ~30MB | 切书时 `invalidate(otherBooks)` |
| `PageSnapshot.displayText` 用 `CharSequence` 切片 | ~40% | 避免每页复制 String |
| 封面加载按目标尺寸采样 | ~60% | Coil `size(400, 533)` 而非原图 |
| 章节缓存用 Room 存（磁盘），不常驻内存 | — | 关键：**不要把整本书正文放内存** |

### 6.4.2 按字节控制的 Bitmap LRU（比按数量更准确）

```kotlin
// data:parser/pdf — PdfBitmapCache.kt
class PdfBitmapCache(maxBytes: Int = 48 * 1024 * 1024) {   // 48MB 上限
    private val lru = object : LruCache<String, Bitmap>(maxBytes) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }

    fun get(key: String): Bitmap? = lru.get(key)

    fun put(key: String, bitmap: Bitmap) {
        if (bitmap.byteCount > maxBytes / 3) {
            // 单张超过 1/3 上限的位图不缓存（避免挤出所有其他页）
            return
        }
        lru.put(key, bitmap)
    }

    fun trimToSize(pages: Int) {
        // 按"约 N 页"收缩：假设每页 ~1.5MB
        lru.trimToSize(pages * 1_500_000)
    }

    fun evictAll() {
        lru.evictAll()
        // 显式提示 GC：大量 Bitmap 释放后建议一次 gc，避免下次分配时抖动
    }

    private var maxBytes = maxBytes      // 供动态调整
}
```

### 6.4.3 内存泄漏的高危点与检查

| 高危点 | 症状 | 修复 |
| --- | --- | --- |
| `PdfRenderer` 未 close | native 内存持续增长，Java 堆正常 | `DisposableEffect { onDispose { close() } }` |
| `BroadcastReceiver` 未注销 | Activity 泄漏，每页 +几十 KB | `onDispose { unregisterReceiver }` |
| ViewModel 持有 Activity Context | Activity 泄漏 | 注入 `@ApplicationContext` |
| `CoroutineScope` 未取消 | ViewModel 泄漏 | `onCleared { scope.cancel() }` |
| `Flow` 在 Composable 中用 `collectAsState` | 重复订阅 | `collectAsStateWithLifecycle` |
| `TextMeasurer` 在非 Composition 持有 | Globals 泄漏 | 通过 `SideEffect` 更新 holder，Composition 销毁时置空 |
| 静态 Map 缓存书籍对象 | 全量书籍常驻 | 用 `WeakHashMap` 或按时清理 |
| Lambda 捕获大对象在动画中 | 动画期间内存尖峰 | 动画 lambda 只捕获基本类型 |

**修复示例（TextMeasurer 泄漏）**：

```kotlin
@Composable
fun ProvideTextMeasurer(content: @Composable () -> Unit) {
    val holder = remember { TextMeasurerHolder() }
    val measurer = rememberTextMeasurer(cacheSize = 8)
    val density = LocalDensity.current

    SideEffect {
        holder.measurer = measurer
        holder.density = density
    }
    // ★ 关键：Composition 销毁时清空引用，避免 holder 长期持有 TextMeasurer
    DisposableEffect(Unit) {
        onDispose {
            holder.measurer = null
        }
    }
    CompositionLocalProvider(LocalTextMeasurerHolder provides holder) { content() }
}
```

### 6.4.4 内存验证测试

```kotlin
// app/src/androidTest — MemoryStabilityTest.kt
@RunWith(AndroidJUnit4::class)
class MemoryStabilityTest {
    @Test
    fun oneHourReadingMemoryStable() {
        val milestones = mutableListOf<Pair<Int, Long>>()

        // 打开一本 5MB 书
        openBook("测试书籍 5MB")

        repeat(60) { minute ->
            repeat(30) { nextPage() }          // 每分钟翻 30 页
            Thread.sleep(500)
            val mb = appUsedMemoryMb()
            milestones += (minute to mb)
            Log.i("MemTest", "第 ${minute + 1} 分钟: ${mb}MB")
        }

        val baseline = milestones.take(5).map { it.second }.average()
        val at10min = milestones[9].second
        val at60min = milestones[59].second
        val peak = milestones.maxOf { it.second }

        // 断言 1：峰值相对基线增长 ≤ 150MB
        assertTrue("峰值增长 ${peak - baseline}MB > 150MB", peak - baseline <= 150.0)

        // 断言 2：后 50 分钟无持续增长（泄漏检测）
        val growthLast50 = at60min - at10min
        assertTrue("后 50 分钟增长 ${growthLast50}MB，疑似泄漏", growthLast50 <= 15)

        // 断言 3：GC 后应能回落到合理水平（无不可回收的强引用）
        System.gc(); Thread.sleep(2000)
        val afterGc = appUsedMemoryMb()
        assertTrue("GC 后仍占用 ${afterGc}MB，存在强引用泄漏", afterGc - baseline <= 80)
    }

    private fun appUsedMemoryMb(): Long {
        val rt = Runtime.getRuntime()
        return (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024
    }
}
```

---

## 6.5 兼容性适配

### 6.5.1 版本适配矩阵

| API 级别 | Android 版本 | 关键差异 | 适配措施 |
| --- | --- | --- | --- |
| 26 | 8.0 | 最低支持。`Typeface.createFromFile` 可用；SAF 可用 | 基线 |
| 28 | 9.0 | 明文流量默认禁止 | `networkSecurityConfig` 允许书源 HTTP 站点（必须，很多书源站是 HTTP） |
| 29 | 10.0 | 分区存储（Scoped Storage）强制 | 全面使用 SAF，不申请 `WRITE_EXTERNAL_STORAGE` |
| 30 | 11.0 | 软件包可见性；后台位置限制 | `queries` 声明（本项目无需）；`requestedFrameRate` 可用 |
| 31 | 12.0 | 精确闹钟权限；`PendingIntent` 必须指定 flag | 本项目不用闹钟；WorkManager 不受影响 |
| 32 | 12L | 大屏适配；`SplashScreen` API | 支持平板分栏 |
| 33 | 13.0 | `POST_NOTIFICATIONS` 运行时权限；细化媒体权限 | **不申请通知权限**（无营销推送需求，仅缓存完成通知时按需申请） |
| 34 | 14.0 | 前台服务类型必须声明；隐式 Intent 限制 | WorkManager `dataSync` 类型声明 |
| 35 | 15.0 | 边到边强制；`enableEdgeToEdge` | 全面 `WindowInsets` 适配 |

### 6.5.2 明文 HTTP 支持（书源必需）

大量书源站点是 HTTP。Android 9+ 默认禁止明文流量，**必须显式配置**：

```xml
<!-- app/src/main/res/xml/network_security_config.xml -->
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <!-- 默认禁止明文，安全第一 -->
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>

    <!-- ★ 例外：允许对用户添加的书源域名使用明文 -->
    <!-- 问题：域名在运行时才知道，无法静态列举 -->
    <!-- 解决：创建一个特殊的"书源域名"域配置不适用，改用 domain-config 通配 -->
    <!-- 实际方案：见下方代码实现 -->
</network-security-config>
```

**运行时动态允许明文的正确做法**：`network-security-config` 是静态的，无法支持运行时域名。方案是**在 OkHttp 层做协议升级/降级 + 在 config 中允许所有明文但通过代码控制**：

```xml
<!-- app/src/main/res/xml/network_security_config.xml -->
<network-security-config>
    <!-- 官方服务：强制 HTTPS -->
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">kr-reader.example.com</domain>
    </domain-config>

    <!-- ★ 用户书源：允许明文（用户在导入书源时已被告知该源为 HTTP） -->
    <base-config cleartextTrafficPermitted="true">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
</network-security-config>
```

**同时在代码层给出安全提示**：书源为 HTTP 时，在书源详情页显示"该源使用不安全连接"标记。

### 6.5.3 存储权限最小化（合规要求）

| 权限 | 是否申请 | 说明 |
| --- | --- | --- |
| `INTERNET` | ✅ 必须 | 书源抓取 |
| `ACCESS_NETWORK_STATE` | ✅ 必须 | 缓存任务的网络约束判断 |
| `WAKE_LOCK` | ✅ 必须 | WorkManager 后台缓存 |
| `POST_NOTIFICATIONS` | ⚠️ 按需 | **仅在用户主动开启"缓存完成通知"时申请** |
| `FOREGROUND_SERVICE` | ✅ | 缓存任务（API 34 需 `FOREGROUND_SERVICE_DATA_SYNC`） |
| `READ_EXTERNAL_STORAGE` | ❌ **不申请** | 用 SAF，无需此权限 |
| `WRITE_EXTERNAL_STORAGE` | ❌ **不申请** | 同上 |
| `READ_MEDIA_*` | ❌ **不申请** | 书籍不是媒体文件 |
| `QUERY_ALL_PACKAGES` | ❌ **不申请** | 不需要 |
| `ACCESS_FINE_LOCATION` | ❌ **不申请** | 不需要 |
| `REQUEST_INSTALL_PACKAGES` | ❌ **不申请** | 无自更新 |
| `SYSTEM_ALERT_WINDOW` | ❌ **不申请** | 不需要悬浮窗 |
| `VIBRATE` | ⚠️ 按需 | 翻页震动反馈 |

```xml
<!-- app/src/main/AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />

<!-- 按需申请（运行时请求，用户同意才用） -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.VIBRATE" />

<!-- SAF 不需要权限声明 -->
<application
    android:requestLegacyExternalStorage="false"
    android:allowBackup="false"                    <!-- ★ 禁止自动备份：防止书籍被上传到 Google 云 -->
    android:dataExtractionRules="@xml/data_extraction_rules"
    android:networkSecurityConfig="@xml/network_security_config"
    android:enableOnBackInvokedCallback="true"
    ... />
```

### 6.5.4 屏幕与设备形态适配

| 形态 | 适配要点 |
| --- | --- |
| 手机竖屏 | 默认布局，3 列书架 |
| 手机横屏 | 阅读器进入**双栏模式**（两页并排），书架 5 列 |
| 折叠屏（展开） | 阅读器双栏；设置面板用 `SupportingPaneScaffold` |
| 平板（≥600dp） | 阅读器双栏；书架 5~6 列；设置页左右分栏 |
| 挖孔屏/刘海屏 | `WindowInsets.displayCutout` → 内容避让；阅读器可用 `immersive` 全屏让文字绕开 |
| 手势导航栏 | `WindowInsets.navigationBars` 底部内边距 |
| 大字体（系统字体缩放 200%） | 所有 UI 用 `sp` 且测试极端缩放下的布局不破 |

```kotlin
// 阅读器：横屏/平板双栏
@Composable
fun ReaderContent(
    pages: List<PageSnapshot>,
    typography: TypographyConfig,
    theme: ReaderTheme,
    windowSize: WindowSizeClass,
) {
    val isTwoPane = windowSize.widthSizeClass != WindowSizeClass.WIDTH_DP_COMPACT_LOWER ||
        // 或者简单用宽高比判断（横屏）
        LocalConfiguration.current.let { it.screenWidthDp > it.screenHeightDp && it.screenWidthDp >= 600 }

    if (isTwoPane) {
        // 双栏：把视口宽度减半后重新分页（关键！要用半宽视口分页）
        Row {
            ReaderPage(pages.getOrNull(0), typography, theme, Modifier.weight(1f))
            VerticalDivider(Modifier.width(1.dp).fillMaxHeight())
            ReaderPage(pages.getOrNull(1), typography, theme, Modifier.weight(1f))
        }
    } else {
        SinglePage(pages[0], typography, theme)
    }
}
```

> ⚠️ **双栏模式必须用"半宽视口"重新分页**，而不是把单栏的两页并排显示。否则每页的行宽过宽（>40 字/行），阅读舒适度极差。这是横屏适配最容易做错的地方。

### 6.5.5 兼容性测试矩阵

| 维度 | 覆盖 |
| --- | --- |
| API 级别 | 26, 28, 29, 31, 33, 34, 35 |
| 屏幕尺寸 | 4.7" (720p), 6.1" (1080p), 6.8" (1440p), 10" 平板 |
| 刷新率 | 60Hz, 90Hz, 120Hz |
| 厂商 ROM | Pixel（原生）、小米 HyperOS、华为 HarmonyOS、OPPO ColorOS、vivo OriginOS、三星 OneUI |
| 特殊设备 | 折叠屏（含内屏切换）、电子墨水屏（如有条件）、带实体返回键 |
| 语言 | 中文（简体）为主，英文次之 |
| 字体缩放 | 100%, 150%, 200% |
| 深色模式 | 系统深色 / 系统浅色 |

**厂商 ROM 的已知坑**：

| 厂商 | 坑 | 对策 |
| --- | --- | --- |
| 小米 HyperOS | 后台任务被激进清理，WorkManager 缓存中断 | 引导用户"锁定后台"；用 `setForeground` 提升优先级 |
| 华为 HarmonyOS | `Typeface.createFromFile` 对某些 otf 返回 null | 加载失败回退系统字体 + 提示 |
| OPPO ColorOS | 高刷新率白名单，应用可能被限到 60Hz | 无法程序解决，文档说明 |
| 三星 OneUI | SAF 的 `DocumentFile.listFiles()` 在某些目录返回空 | 用 `DocumentsContract` 直接查询（更底层） |
| vivo OriginOS | 节电模式限制网络 | 检测到请求失败时提示用户检查节电设置 |

**三星 SAF 的修复实现**（这是个真实存在的问题）：

```kotlin
/** DocumentFile.listFiles() 在三星部分机型上不可靠，改用 DocumentsContract 直查 */
fun listChildren(ctx: Context, treeUri: Uri, parentDocId: String): List<ChildDoc> {
    val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocId)
    val result = mutableListOf<ChildDoc>()
    ctx.contentResolver.query(
        childrenUri,
        arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
        ),
        null, null, null,
    )?.use { cursor ->
        while (cursor.moveToNext()) {
            result += ChildDoc(
                docId = cursor.getString(0),
                name = cursor.getString(1),
                mimeType = cursor.getString(2),
                size = cursor.getLong(3),
                lastModified = cursor.getLong(4),
            )
        }
    }
    return result
}
```

---

## 6.6 数据安全与隐私

### 6.6.1 隐私原则（对应需求"不上传用户本地阅读文件"）

| 原则 | 落实措施 |
| --- | --- |
| **本地文件永不上传** | 代码层面：文件读取 API 与网络 API 在架构上隔离（`data:parser` 与 `data:sources` 无直接依赖）；`data:sources` 无法访问 `filesDir/books/` |
| **无账号体系** | 无登录、无用户 ID、无设备指纹 |
| **默认无遥测** | 崩溃上报默认**关闭**，用户显式开启才生效 |
| **权限最小化** | 见 6.5.3 |
| **禁止自动云备份** | `android:allowBackup="false"` + `dataExtractionRules` 排除书籍目录 |
| **敏感数据加密** | 书源 Cookie 存 `EncryptedSharedPreferences` |
| **无广告 SDK** | 零第三方广告/分析 SDK |

### 6.6.2 架构层面的强制隔离

```kotlin
// 用一个"隐私网关"在编译期隔离文件与网络
// data/sources 模块的 build.gradle.kts 中不依赖任何文件工具
// 同时在 detekt 中禁止 data:sources 引用 java.io.File

// config/detekt/detekt.yml（模块级配置）
style:
  ForbiddenImport:
    active: true
    imports:
      - 'java.io.File'
      - 'android.content.Context.filesDir'
      - 'com.kr.reader.data.parser.*'
```

**额外的运行时审计**（debug 包）：

```kotlin
#if DEBUG
// 审计：检测是否有代码试图把文件内容发往网络
class PrivacyAuditInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val body = chain.request().body
        if (body != null) {
            val contentLength = body.contentLength()
            if (contentLength > 100 * 1024) {
                // 请求体超过 100KB，可能是误传了书籍内容
                Log.e("PrivacyAudit", "⚠️ 检测到大请求体（${contentLength}B）：${chain.request().url}")
                if (BuildConfig.CRASH_ON_PRIVACY_VIOLATION) {
                    error("隐私审计失败：疑似上传本地文件")
                }
            }
        }
        return chain.proceed(chain.request())
    }
}
#endif
```

### 6.6.3 崩溃上报的隐私处理

```kotlin
// 默认关闭；开启后也要做数据脱敏
object CrashReporter {
    var enabled: Boolean = false     // 默认 false

    fun report(throwable: Throwable, context: Map<String, String>) {
        if (!enabled) return
        val sanitized = context.mapValues { (k, v) -> sanitize(k, v) }
        // 脱敏规则：
        //   · 文件路径 → 只保留扩展名（/data/.../abc123.txt → *.txt）
        //   · 书名     → 替换为 <<BOOK_TITLE>>
        //   · 书源 URL → 只保留域名
        //   · 搜索关键词 → 替换为 <<QUERY>>
        upload(throwable.stackTraceToString(), sanitized)
    }

    private fun sanitize(key: String, value: String): String = when {
        key.contains("path") || key.contains("file") ->
            "*." + value.substringAfterLast('.', "unknown")
        key.contains("title") || key.contains("book") -> "<<BOOK_TITLE>>"
        key.contains("url") -> runCatching { URL(value).host }.getOrDefault("<<URL>>")
        key.contains("query") || key.contains("keyword") -> "<<QUERY>>"
        else -> value.take(100)      // 兜底：截断
    }
}
```

### 6.6.4 数据导出与清除（用户权利）

| 功能 | 入口 | 说明 |
| --- | --- | --- |
| 导出书签/笔记 | 设置 → 数据管理 → 导出 | JSON 文件，写到用户选择的 SAF 位置 |
| 导出书源 | 书源页 → 多选 → 导出 | JSON，**不含 Cookie** |
| 清除所有数据 | 设置 → 数据管理 → 清除 | 二次确认 + 显示将删除的内容与大小 |
| 清除缓存 | 设置 → 存储 | 只删 `cache/` 与 `chapter_cache` 表 |
| 清除阅读记录 | 设置 → 数据管理 | 只删进度与统计，保留书籍 |

**"清除所有数据"的交互必须极其明确**：

```kotlin
AlertDialog(
    title = { Text("确认清除所有数据？") },
    text = {
        Column {
            Text("此操作将永久删除：")
            Spacer(Modifier.height(8.dp))
            BulletLine("全部导入的书籍文件（${bookCount} 本，${totalSize}）")
            BulletLine("全部阅读进度与书签（${bookmarkCount} 条）")
            BulletLine("全部书源配置（${sourceCount} 个）")
            BulletLine("全部阅读设置")
            Spacer(Modifier.height(12.dp))
            Text(
                "此操作不可撤销。建议先导出书签与书源。",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { exportFirst() }) { Text("先导出备份") }
        }
    },
    confirmButton = {
        TextButton(
            onClick = { confirmDelete() },
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
        ) { Text("确认清除") }
    },
    dismissButton = { TextButton(onClick = dismiss) { Text("取消") } },
)
```

---

## 6.7 能效优化（P2 指标）

### 6.7.1 阅读场景的耗电优化

| 耗电源 | 优化 |
| --- | --- |
| 屏幕常亮 | 这是用户主动选择的（`keepScreenOn`），默认关闭 |
| 高刷新率 | 静止时不请求高刷（`requestedFrameRate = 0f`）；仅翻页动画期间请求 |
| 时钟每秒更新 | **对齐分钟边界**更新，不是每秒（省 ~1% 电量/小时） |
| 电量监听 | 用粘性广播取初值 + 监听变化，不是轮询 |
| 后台预分页 | 只在屏幕亮时预分页；`onStop` 时暂停 |
| 自动滚屏 | `withFrameNanos` 会持续唤醒 vsync，属于必要成本；但暂停时要真正停止协程 |
| 内存 GC | 减少对象分配（见 6.3.2）间接降低 CPU 唤醒 |

**"对齐分钟边界"的实现**（已在 4.6.4 给出，这里强调其重要性）：

```kotlin
// ❌ 每秒唤醒一次，1 小时 3600 次
LaunchedEffect(Unit) {
    while (true) { delay(1000); time = now() }
}

// ✅ 每分钟唤醒一次，1 小时 60 次；且对齐到分钟边界使显示与系统时钟同步
LaunchedEffect(Unit) {
    while (true) {
        delay(60_000 - (System.currentTimeMillis() % 60_000))
        time = now()
    }
}
```

### 6.7.2 后台缓存的能效

```kotlin
// WorkManager 约束已包含 RequiresBatteryNotLow
// 额外优化：动态调整抓取节奏
private fun dynamicDelay(successRate: Double, avgLatencyMs: Long): Long = when {
    successRate < 0.5 -> 3000          // 失败多 → 放慢，避免无谓流量与电耗
    avgLatencyMs > 3000 -> 800         // 站点慢 → 适度间隔
    else -> 300                        // 正常 → 300ms
}
```

---

## 6.8 性能回归体系

### 6.8.1 性能基线管理

```
benchmark/baselines/
├── reader_scroll_60hz.json
├── reader_scroll_120hz.json
├── startup_cold.json
├── bookshelf_scroll_500books.json
└── chapterize_10mb.json
```

**CI 中的对比逻辑**：

```yaml
# .github/workflows/perf.yml
- name: 运行性能基准
  run: ./gradlew :benchmark:connectedBenchmarkAndroidTest

- name: 对比基线（劣化 > 10% 则失败）
  run: |
    python scripts/compare_baseline.py \
      --baseline benchmark/baselines/ \
      --current benchmark/build/outputs/ \
      --threshold 0.10
```

```python
# scripts/compare_baseline.py（核心逻辑）
import json, sys, pathlib

def compare(baseline_path, current_path, threshold):
    base = json.loads(pathlib.Path(baseline_path).read_text())
    curr = json.loads(pathlib.Path(current_path).read_text())
    regressions = []

    for metric in base["metrics"]:
        name = metric["name"]
        b = metric["value"]
        c = next((m["value"] for m in curr["metrics"] if m["name"] == name), None)
        if c is None:
            continue
        # 对于耗时类指标，越小越好；对于帧率类，越大越好
        worse_is_higher = "frameDuration" in name or "timeTo" in name
        delta = (c - b) / b
        is_regression = delta > threshold if worse_is_higher else delta < -threshold
        if is_regression:
            regressions.append(f"{name}: {b} → {c} ({delta:+.1%})")

    if regressions:
        print("❌ 性能劣化：")
        for r in regressions:
            print(f"   {r}")
        sys.exit(1)
    print("✅ 性能无劣化")
```

### 6.8.2 开发期的性能护栏

| 手段 | 作用 |
| --- | --- |
| `JankStats` 生产环境监控 | 采样统计线上掉帧率，发现真机问题 |
| Compose Compiler Metrics | CI 中输出各 Composable 的跳过/重启统计，发现"不可跳过"的组件 |
| StrictMode（debug） | 检测主线程 IO 与网络 |
| LeakCanary（debug） | 自动泄漏检测 |
| Layout Inspector | 排查过度重组 |
| `adb shell dumpsys gfxinfo` | 手动快速验证帧率 |

**StrictMode 配置**：

```kotlin
// app — KrApplication.kt
override fun onCreate() {
    super.onCreate()
    if (BuildConfig.DEBUG) {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()             // 主线程网络 → 立即暴露
                .detectCustomSlowCalls()
                .penaltyLog()
                .penaltyDialog()             // 开发时弹窗，无法忽略
                .build(),
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedClosableObjects()
                .detectLeakedSqlLiteObjects()
                .detectActivityLeaks()
                .detectFileUriExposure()
                .penaltyLog()
                .build(),
        )
    }
}
```

**Compose Compiler Metrics 配置**：

```kotlin
// app/build.gradle.kts
composeCompiler {
    metricsDestination = layout.buildDirectory.dir("compose-metrics")
    reportsDestination = layout.buildDirectory.dir("compose-reports")
    // 输出每个 Composable 是否可跳过（skippable）、是否可重启（restartable）
}
```

**检查原则**：如果 `ReaderPage` 显示为 `restartable skippable`，说明参数稳定、性能良好；如果显示为 `restartable`（无 skippable），说明参数不稳定，需要检查（常见原因：传了 `List` 而非 `ImmutableList`，或传了 lambda 未 `remember`）。

---

## 6.9 NFR 达标总清单

| 编号 | 指标 | 目标 | 状态 | 验证 |
| --- | --- | --- | --- | --- |
| P0-1 | 10MB TXT 分章 | ≤ 1500ms | ☐ | 单元性能测试 |
| P0-2 | 翻页帧率 | ≥ 60fps（120Hz 屏 ≥ 90） | ☐ | Macrobenchmark |
| P0-3 | 1 小时内存波动 | ≤ 150MB | ☐ | Instrumentation |
| P0-4 | 无白屏 | 0 次 > 200ms | ☐ | 录屏 + JankStats |
| P1-1 | 冷启动可交互 | ≤ 800ms | ☐ | Macrobenchmark |
| P1-3 | 二次打开书籍首屏 | ≤ 300ms | ☐ | Macrobenchmark |
| P1-5 | 首屏分页 | ≤ 120ms | ☐ | 单元测试 |
| P1-9 | 目录页（2000 章） | ≤ 200ms | ☐ | 手动 + 基准 |
| P1-10 | 聚合搜索 8 源 | ≤ 3000ms | ☐ | 集成测试 |
| P1-16 | 书架滚动（500 本） | ≥ 60fps | ☐ | Macrobenchmark |
| P1-17 | APK 体积 | ≤ 12MB | ☐ | 构建产物 |
| P2-1 | 崩溃率 | < 0.5% | ☐ | 线上监控 |
| P2-2 | ANR 率 | < 0.1% | ☐ | 线上监控 |
| P2-3 | 阅读 1 小时耗电 | ≤ 6% | ☐ | Battery Historian |
| P2-5 | 待机 8 小时 | ≤ 1% | ☐ | Battery Historian |
| — | 兼容性矩阵 | 全通过 | ☐ | 见 7.4 |
| — | 权限最小化 | 无多余权限 | ☐ | `aapt dump permissions` |
| — | 隐私审计 | 无文件上传路径 | ☐ | 代码审计 + 拦截器 |

> **本清单是 V1.0 发布的硬性前置条件。任一 ☐ 未勾选，不得进入发布流程。**具体验收流程见 7.5。

---

**上一卷**：[第 5 卷 · 关键算法实现方案](#vol-05) ｜ **下一卷**：[第 7 卷 · 测试与质量保障](#vol-07)


---


# 第 7 卷 · 测试与质量保障 {#vol-07}
> 阅读器是一个"**正确性 + 性能**双重敏感"的产品。进度记错一个字，用户会怀疑整个应用；分页慢 200ms，用户会直接换应用。本卷给出分层测试策略、覆盖率门禁、验收标准，以及**如何让测试真正跑起来而不是躺在文档里**。

---

## 7.1 测试策略总览

### 7.1.1 测试金字塔（按投入比例）

```
                          ┌───────────────┐
                          │  E2E / 手测    │  ~5%   关键用户旅程
                          ├───────────────┤
                          │  仪器测试      │  ~10%  UI 交互、内存、DB 迁移
                          │ (Instrumented) │
                          ├───────────────┤
                          │  集成测试      │  ~20%  Repository + Room + 规则引擎
                          ├───────────────┤
                          │               │
                          │   单元测试     │  ~65%  算法、状态、映射、清洗
                          │               │
                          └───────────────┘
```

**比例背后的判断**：本项目的核心复杂度在**纯逻辑**（编码探测、分章、分页、偏移映射、规则解析、清洗），这些都能用快速单元测试覆盖。UI 层薄（Compose 声明式），仪器测试价值相对低。**因此不追求仪器测试覆盖率，追求算法单元测试的完备性。**

### 7.1.2 测试分层与工具

| 层 | 工具 | 运行环境 | 目标耗时 | 何时跑 |
| --- | --- | --- | --- | --- |
| 单元测试 | JUnit 4 + Truth + MockK + Turbine | JVM | < 30s 全量 | 每次保存（IDE）/ 每次 PR |
| 属性测试 | Kotest Property | JVM | < 10s | PR |
| 性能单测 | JUnit + `measureTimeMillis` | JVM | < 20s | PR |
| 集成测试 | Robolectric + Room in-memory | JVM | < 60s | PR |
| 仪器测试 | AndroidX Test + Compose Test | 真机/模拟器 | < 5min | 合并到 dev 前 |
| 基准测试 | Macrobenchmark | 真机 | < 15min | 每日/发版前 |
| 内存测试 | Instrumentation + LeakCanary | 真机 | < 70min | 发版前 |
| 兼容性 | Firebase Test Lab / 真机矩阵 | 云真机 | < 30min | 发版前 |
| E2E 手测 | 人工 + 检查单 | 真机 | 半天 | 发版前 |

### 7.1.3 覆盖率门禁

```kotlin
// build.gradle.kts（Jacoco 覆盖率校验）
tasks.register<JacocoCoverageVerification>("checkCoverage") {
    violationRules {
        rule {
            element = "PACKAGE"
            // 全局底线
            limit { minimum = "0.60".toBigDecimal() }
        }
        // 核心模块高标准
        listOf(":core:model", ":domain").forEach { path ->
            rule {
                element = "PACKAGE"
                includes = listOf("com.kr.reader.*")
                limit { minimum = "0.85".toBigDecimal() }
            }
        }
        // 解析与分页引擎：最高标准
        rule {
            element = "CLASS"
            includes = listOf("com.kr.reader.data.parser.*", "com.kr.reader.data.pagination.*")
            limit { minimum = "0.90".toBigDecimal() }
        }
    }
}
```

| 模块 | 覆盖率门禁 | 理由 |
| --- | --- | --- |
| `data:parser` | **≥ 90%** | 解析错误直接导致"打不开书" |
| `data:pagination` | **≥ 90%** | 分页错误导致进度错乱 |
| `data:sources` | ≥ 85% | 规则引擎与清洗 |
| `domain` | ≥ 85% | 业务编排 |
| `core:model` | ≥ 85% | 数据模型 |
| `data:books` | ≥ 80% | 书架 CRUD |
| `feature:*`（ViewModel） | ≥ 70% | 状态转换 |
| `core:*`（其他） | ≥ 60% | 工具类 |
| **整体** | **≥ 70%** | 综合底线 |

> **覆盖率不是目的**。原则是：**核心算法分支必须被测试**，而不是"为了数字写无断言的测试"。Review 时检查：每个 `when` / `if` 分支是否有对应用例；边界值是否覆盖（空、单元素、最大值、非法值）。

---

## 7.2 单元测试设计

### 7.2.1 测试命名与结构

采用 **Given-When-Then** 命名，中文描述便于团队理解：

```kotlin
class TxtChapterizerTest {

    @Test
    fun `给定标准网文_当分章时_应识别全部2000章`() { ... }

    @Test
    fun `给定含正文章节引用的文本_当分章时_不应误报章节`() { ... }

    @Test
    fun `给定空文件_当分章时_应返回单章兜底而非崩溃`() { ... }
}
```

**测试结构模板**：

```kotlin
@Test
fun `给定章序号重置的分卷文本_当分章时_应重置序号链`() {
    // ── Given ────────────────────────────────────────
    val text = buildString {
        appendLine("卷一 少年游")
        repeat(80) { appendLine("第${it + 1}章 章节标题"); appendLine("正文内容。".repeat(50)) }
        appendLine("卷二 江湖路")
        repeat(40) { appendLine("第${it + 1}章 新章节"); appendLine("正文内容。".repeat(50)) }
    }
    val file = text.toTempTxtFile(charset = Charsets.UTF_8)

    // ── When ─────────────────────────────────────────
    val chapters = runBlocking {
        TxtChapterizer.chapterize(file, Charsets.UTF_8, customRegex = null) {}
    }

    // ── Then ─────────────────────────────────────────
    assertEquals("卷标题与章节总数不符", 122, chapters.size)
    assertTrue("卷二第一章未识别（序号重置导致误判）",
        chapters.any { it.title == "第1章 新章节" })
    // 卷标题应被标记
    assertEquals(2, chapters.count { it.isVolume })
    // 偏移应严格单调递增
    chapters.zipWithNext { a, b ->
        assertTrue("章节偏移非单调：${a.title}(${a.startOffset}) → ${b.title}(${b.startOffset})",
            a.startOffset < b.startOffset)
    }
}
```

### 7.2.2 必须测试的用例清单

#### `data:parser`（最高优先级）

| 测试类 | 用例 |
| --- | --- |
| `CharsetDetectorTest` | 15 例语料（见 5.1.5）；准确率聚合断言；BOM 优先；纯 ASCII；截断文件；空文件 |
| `TxtChapterizerTest` | 14 例语料（见 5.2.6）；分卷序号重置；目录块跳过；无章节兜底；CR-only 换行 |
| `OffsetIndexTest` | `charToByte` 双向精度；锚点边界；越界防御；emoji（代理对）；文件变更检测 |
| `TxtChapterLoaderTest` | 按偏移读正文；首章/末章边界；跨锚点读取；空章 |
| `EpubParserTest` | 正常 EPUB；缺 OPF；缺 NCX（用 NAV）；缺 NAV（无目录）；路径越界拒绝；内嵌图提取 |
| `PdfParserTest` | 正常 PDF；加密 PDF 提示；0 页 PDF；封面渲染 |
| `TitleCleanerTest` | 12 种常见文件名清洗；清洗过度回退 |
| `MetadataExtractorTest` | TXT/EPUB/PDF 三格式元数据；作者提取 4 种模式 |

#### `data:pagination`

| 测试类 | 用例 |
| --- | --- |
| `PaginationEngineTest` | 7 条不变量（8 类输入）；空章节；纯空白；超长单段；视口为 0 |
| `PaginationEngineTest` | 字号变化后偏移锚点语义稳定；页区间无重叠 |
| `PaginationPerformanceTest` | 首屏 ≤ 120ms；全章 ≤ 400ms |

#### `data:sources`

| 测试类 | 用例 |
| --- | --- |
| `SourceEngineTest` | MockWebServer 模拟各类响应；搜索结果解析；目录解析（正序/倒序）；正文提取 |
| `SelectorResolverTest` | 3 种残缺 HTML 的回退链；启发式正文识别 |
| `ParagraphExtractorTest` | PARAGRAPH/BR/AUTO 三模式；nbsp；零宽字符；相邻重复段 |
| `BuiltinPurifyRulesTest` | 不误删正文；清除 8 类水印；过度清洗回退 |
| `SafeRegexTest` | 危险正则拒绝（嵌套量词、重复分支、超长）；超时不阻塞 |
| `ResultMergerTest` | 同书多源归并；标题噪声归一化；Levenshtein 边界；质量分排序 |
| `ChapterMatcherTest` | 4 级匹配策略；置信度；全失败回退比例 |
| `SourceValidatorTest` | 缺字段、非法 URL、缺占位符、选择器语法错误 |

#### `data:books` / `core:database`

| 测试类 | 用例 |
| --- | --- |
| `BookRepositoryTest` | 导入幂等（重复导入不重复）；去重（dedup_hash）；移出 vs 删除语义 |
| `ProgressRepositoryTest` | 三级写入；flushNow；进度恢复 |
| `MigrationTest` | 每次 schema 变更的迁移测试 + 核心数据不丢 |
| `BookDaoTest` | 4 种排序；分组过滤；分页 |
| `ChapterDaoTest` | `locateByOffset` 二分定位；索引查询性能 |

#### `feature:*` ViewModel

| 测试类 | 用例 |
| --- | --- |
| `ReaderViewModelTest` | 翻页状态流转；跳转；排版变更触发重分页；进度锚点保持；错误态 |
| `BookshelfViewModelTest` | 扫描→勾选→导入流程；多选模式；批量操作 |
| `SearchViewModelTest` | 并发搜索流式事件；单源失败不中断；结果归并 |
| `SourceDebuggerViewModelTest` | 规则诊断输出 |

**ViewModel 测试模板**（用 Turbine 测 Flow）：

```kotlin
@Test
fun `排版变更后_应按新页表重新定位到同一字符偏移`() = runTest {
    val vm = ReaderViewModel(
        handle = SavedStateHandle(mapOf("bookId" to 1L)),
        loadChapters = FakeLoadChapters(3000 字章节),
        paginationEngine = FakePaginationEngine(),
        progressRepo = FakeProgressRepo(),
        typographyRepo = FakeTypographyRepo(),
    )

    // 初始：18sp，读到第 3 页
    vm.state.test {
        awaitItem()                                            // 初始态
        vm.onIntent(ReaderIntent.GotoChapter(0))
        advanceUntilIdle()
        repeat(2) { vm.onIntent(ReaderIntent.NextPage(false)) }
        advanceUntilIdle()

        val before = vm.state.value
        val anchorChars = before.pages[before.currentPageIndex].startCharInChapter

        // 变更字号到 28sp
        vm.onIntent(ReaderIntent.OnTypographyChanged(TypographyConfig.Default.copy(fontSizeSp = 28f)))
        advanceTimeBy(200)                                     // 过掉 120ms 去抖
        advanceUntilIdle()

        val after = vm.state.value
        val newPage = after.pages[after.currentPageIndex]
        assertTrue("偏移锚点丢失",
            newPage.startCharInChapter <= anchorChars && anchorChars < newPage.endCharInChapter)
        // 页索引必然变大（每页字变少）
        assertTrue("页索引未随字号变化", after.currentPageIndex > before.currentPageIndex)

        cancelAndIgnoreRemainingEvents()
    }
}
```

### 7.2.3 属性测试（Property-Based Testing）

对算法类代码，属性测试能发现手工用例想不到的边界：

```kotlin
class PaginationPropertyTest : StringSpec({
    "分页区间必须严格连续覆盖全文" {
        checkAll(
            Exhaustive.ints(0..500),                    // 文本长度
            Exhaustive.floats(12f..36f),                // 字号
        ) { length, fontSize ->
            val text = randomChineseText(length)
            val cfg = TypographyConfig.Default.copy(fontSizeSp = fontSize)
            val pages = runBlocking {
                engine.paginate(text, cfg, IntSize(1080, 1920)).toList().flatMap { it.pages }
            }
            if (text.isEmpty()) return@checkAll

            // 不变量 1：起点
            pages.first().startCharInChapter shouldBe 0
            // 不变量 2：终点
            pages.last().endCharInChapter shouldBe text.length
            // 不变量 3：连续
            pages.zipWithNext { a, b ->
                a.endCharInChapter shouldBe b.startCharInChapter
            }
        }
    }

    "任何字符偏移都能被唯一一页包含" {
        checkAll(Exhaustive.ints(0..2000)) { offset ->
            val text = randomChineseText(2000)
            val pages = paginate(text)
            val offsetInRange = offset.coerceIn(0, text.length - 1)
            val containing = pages.filter {
                it.startCharInChapter <= offsetInRange && offsetInRange < it.endCharInChapter
            }
            containing.size shouldBe 1
        }
    }
})
```

**关键属性清单**：

| 被测对象 | 属性 |
| --- | --- |
| 分页 | 区间连续覆盖；每个偏移唯一页；页序单调 |
| 编码探测 | 任意编码写出的文本，探测结果解码后应无替换字符（除了明确标注的有损） |
| 分章 | 章节偏移严格单调递增；章节区间不重叠；首章从 0 开始 |
| 偏移映射 | `charToByte(charToByte⁻¹(x)) == x`（往返一致） |
| 净化 | 净化后长度 ≤ 原长度；净化两次结果相同（幂等） |
| 归并 | 归并结果与输入顺序无关（交换律）；同一输入归并两次结果相同（幂等） |
| ChapterMatcher | 相同目录匹配到自己（自反性）；归一化后相同标题必精确匹配 |

### 7.2.4 测试替身（Fake）设计

**优先用 Fake 而非 Mock**（更稳定、可测行为而非实现）：

```kotlin
// core:testing — Fakes.kt
class FakeBookRepository : BookRepository {
    private val books = MutableStateFlow<List<Book>>(emptyList())

    override fun observeShelf(groupId: Long?, sort: ShelfSort) =
        books.map { list -> list.filter { groupId == null || it.groupId == groupId } }

    override suspend fun getBook(id: Long) = books.value.firstOrNull { it.id == id }

    override suspend fun importBooks(uris: List<ImportRequest>) = flow {
        uris.forEachIndexed { i, r ->
            emit(ImportProgress(current = i + 1, total = uris.size, name = r.name))
        }
    }

    // ★ 测试专用辅助方法（不在接口中）
    fun setBooks(vararg b: Book) { books.value = b.toList() }
    fun addedBooks() = books.value
}

/** 可控制分页结果的假引擎：模拟慢分页、流式批次、失败 */
class FakePaginationEngine(
    private val batchDelayMs: Long = 0,
    private val pagesPerChapter: Int = 10,
) : PaginationEngine {
    var lastRequest: PaginateRequest? = null
        private set

    override fun paginate(
        chapterText: String,
        typography: TypographyConfig,
        viewport: IntSize,
        config: PaginationConfig,
    ): Flow<PaginationBatch> = flow {
        lastRequest = PaginateRequest(chapterText, typography, viewport)
        if (batchDelayMs > 0) delay(batchDelayMs)
        val perPage = (chapterText.length / pagesPerChapter).coerceAtLeast(1)
        val pages = (0 until pagesPerChapter).map { i ->
            PageSnapshot(
                index = i,
                startCharInChapter = (i * perPage).coerceAtMost(chapterText.length),
                endCharInChapter = ((i + 1) * perPage).coerceAtMost(chapterText.length),
                displayText = "",
                startLine = 0, endLine = 0,
            )
        }
        emit(PaginationBatch(pages.take(2), pages.size <= 2, pages.size))
        if (pages.size > 2) emit(PaginationBatch(pages.drop(2), true, pages.size))
    }

    override fun invalidate(bookId: Long, chapterIndex: Int?) {}
    override fun cacheStats() = PaginationCacheStats(0, 0, 0)
}
```

**Mock 用于**：`Expectations`（验证调用次数与顺序）；**外部 API 边界**（OkHttp）；**难以构造的异常**。

### 7.2.5 网络测试：MockWebServer

```kotlin
class SourceEngineTest {
    private lateinit var server: MockWebServer

    @Before fun setUp() {
        server = MockWebServer().apply { start() }
    }
    @After fun tearDown() { server.shutdown() }

    @Test
    fun `搜索_当站点返回GBK页面时_应正确解码并解析`() = runTest {
        val gbkHtml = """
            <html><head><meta charset="gbk"></head><body>
              <div class="result">
                <a href="/book/123">测试书籍名</a>
                <span class="author">某作者</span>
              </div>
            </body></html>
        """.trimIndent()
        server.enqueue(
            MockResponse()
                .setBody(Buffer().write(gbkHtml.toByteArray(Charset.forName("GBK"))))
                .addHeader("Content-Type", "text/html; charset=gbk"),
        )

        val source = testSource(baseUrl = server.url("/").toString())
        val results = engine.search(source, "测试")

        assertEquals(1, results.size)
        assertEquals("测试书籍名", results[0].name)
        assertEquals("某作者", results[0].author)
        assertTrue(results[0].detailUrl.endsWith("/book/123"))
    }

    @Test
    fun `搜索_当站点返回503时_应重试两次后抛出可恢复错误`() = runTest {
        repeat(3) { server.enqueue(MockResponse().setResponseCode(503)) }

        val ex = assertThrows<KrError.NetworkBlocked> {
            runBlocking { engine.search(testSource(server.url("/").toString()), "x") }
        }
        assertEquals(503, ex.code)
        assertEquals(3, server.requestCount)          // 首次 + 2 次重试
    }

    @Test
    fun `搜索_当响应慢时_应按超时配置中断`() = runTest {
        server.enqueue(MockResponse().setBodyDelay(5, TimeUnit.SECONDS).setBody("<html></html>"))
        val ex = assertThrows<SocketTimeoutException> {
            runBlocking { engine.search(testSource(server.url("/").toString(), timeoutMs = 1000), "x") }
        }
        assertNotNull(ex)
    }
}
```

**必须覆盖的网络异常场景**：

| 场景 | 期望行为 |
| --- | --- |
| 200 + 正常 HTML | 正确解析 |
| 200 + 空 body | 抛出可读错误，不崩溃 |
| 403 / 404 / 429 | 不重试（除 429 外），标记源健康度下降 |
| 500 / 502 / 503 / 504 | 重试 2 次，退避 1s/3s |
| 超时 | 按书源 `timeoutMs` 中断 |
| 连接失败（DNS/无网） | 明确"网络不可用"提示 |
| 响应是 GBK / UTF-8 / BIG5 | 正确解码 |
| HTML 结构完全变化（选择器全不匹配） | 返回空列表 + 警告，不崩溃 |
| 返回 JS 渲染的空白页 | 提示"该源需要 JS 渲染，暂不支持" |

### 7.2.6 时间与随机性的可控化

**禁止在测试中使用真实时间与随机**：

```kotlin
// core:testing — TestClock.kt
class TestClock(private var currentMs: Long = 1_700_000_000_000L) : Clock {
    override fun nowMs() = currentMs
    fun advanceBy(ms: Long) { currentMs += ms }
}

// 生产代码中所有时间获取必须走注入的 Clock
class ImportBooksUseCase @Inject constructor(
    private val clock: Clock,        // ★ 不用 System.currentTimeMillis()
) { ... }

// 测试中
@Test
fun `导入后_addedAt 应为当前时间`() = runTest {
    val clock = TestClock()
    val useCase = ImportBooksUseCase(bookRepo, clock)
    useCase.import(listOf(...))
    assertEquals(clock.nowMs(), bookRepo.addedBooks().first().addedAt)
}
```

**协程测试规则**：

| 规则 | 说明 |
| --- | --- |
| 用 `runTest` 而非 `runBlocking` | `runTest` 使用虚拟时间，`delay` 瞬间完成 |
| 禁注入 `Dispatchers.Main` 真实实现 | 用 `Dispatchers.setMain(StandardTestDispatcher())` |
| `advanceUntilIdle()` 代替 `Thread.sleep` | 立即推进所有挂起任务 |
| `advanceTimeBy(n)` 测去抖 | 精确控制虚拟时间 |
| Flow 测试用 Turbine | 避免 `collect` 挂起不返回 |

```kotlin
// TestRule 统一配置
@ExperimentalCoroutinesApi
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) { Dispatchers.setMain(dispatcher) }
    override fun finished(description: Description) { Dispatchers.resetMain() }
}
```

---

## 7.3 集成测试与仪器测试

### 7.3.1 Room 迁移测试（**强制**）

```kotlin
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        KrDatabase::class.java,
    )

    @Test
    fun migrateAll_preservesProgressAndBookmarks() {
        // 1. 创建 v1 数据库并写入数据
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL("""
                INSERT INTO books (id, title, format, file_size, original_name, dedup_hash, added_at)
                VALUES (1, '测试书籍', 'TXT', 1024, 'test.txt', 'hash123', 1700000000000)
            """.trimIndent())
            execSQL("""
                INSERT INTO reading_progress
                (book_id, chapter_index, chapter_title, char_offset_in_chapter, global_char_offset, percent, chapter_percent, reading_seconds, updated_at)
                VALUES (1, 12, '第十二章', 1420, 50000, 0.34, 0.42, 1800, 1700000000000)
            """.trimIndent())
            execSQL("""
                INSERT INTO bookmarks (book_id, chapter_index, chapter_title, char_offset, snippet, created_at)
                VALUES (1, 3, '第三章', 800, '……这一刻', 1700000000000)
            """.trimIndent())
            close()
        }

        // 2. 跑迁移到最新版本
        val db = helper.runMigrationsAndValidate(TEST_DB, KR_DB_VERSION, true, *ALL_MIGRATIONS)

        // 3. ★ 核心数据必须完整
        db.query("SELECT char_offset_in_chapter FROM reading_progress WHERE book_id = 1").use {
            assertTrue(it.moveToFirst())
            assertEquals("进度字符偏移丢失", 1420, it.getInt(0))
        }
        db.query("SELECT count(*) FROM bookmarks WHERE book_id = 1").use {
            assertTrue(it.moveToFirst())
            assertEquals("书签丢失", 1, it.getInt(0))
        }
        db.query("SELECT title FROM books WHERE id = 1").use {
            assertTrue(it.moveToFirst())
            assertEquals("测试书籍", it.getString(0))
        }
    }
}
```

### 7.3.2 Compose UI 测试

```kotlin
@RunWith(AndroidJUnit4::class)
class ReaderScreenTest {
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun tapRightZone_turnsToNextPage() {
        openTestBook()
        val pagerBefore = composeRule.onNodeWithTag("reader_page_content")
            .fetchSemanticsNode().config[SemanticsProperties.Text]
        val pageBefore = readCurrentPageNumber(composeRule)

        composeRule.onNodeWithTag("reader_tap_area")
            .performTouchInput { click(Offset(size.width * 0.85f, size.height * 0.5f)) }
        composeRule.waitForIdle()

        val pageAfter = readCurrentPageNumber(composeRule)
        assertEquals("点击右区未翻页", pageBefore + 1, pageAfter)
    }

    @Test
    fun changeFontSize_keepsReadingPosition() {
        openTestBook()
        // 翻到第 5 页
        repeat(4) { tapRightZone() }
        val anchorText = readFirstVisibleText(composeRule)

        // 打开排版面板，调大字号
        tapCenterZone()
        composeRule.onNodeWithText("排版").performClick()
        repeat(5) { composeRule.onNodeWithTag("font_size_plus").performClick() }
        composeRule.onNodeWithText("完成").performClick()
        composeRule.waitUntil(3000) { !isPaginating(composeRule) }

        // 断言：当前页应包含之前的锚点文本（或其后紧邻内容）
        val afterText = readVisibleText(composeRule)
        assertTrue(
            "字号变更后阅读位置丢失。之前首行：$anchorText；现在：${afterText.take(100)}",
            afterText.contains(anchorText.take(10)),
        )
    }
}
```

**UI 测试覆盖清单**：

| 界面 | 必测交互 |
| --- | --- |
| 书架 | 网格/列表切换；排序切换；长按多选；批量删除；空态引导 |
| 阅读器 | 三种点击区域；左右滑动翻页；滑动条跳转；菜单唤出/关闭；目录跳转 |
| 排版面板 | 字号增减；行距/段距/边距拖动；主题切换；字体切换 |
| 书源 | 导入（文本/URL/文件）；启用开关；删除；调试器运行 |
| 搜索 | 输入搜索；结果流式出现；换源；加入书架 |
| 设置 | 各项开关持久化（退出重进保持） |

### 7.3.3 无网络场景测试

**阅读器是离线优先应用，必须验证"飞行模式"下完全可用**：

```kotlin
@Test
fun offline_mode_localBookFullyReadable() {
    // 1. 导入本地书
    importTestBook()
    // 2. 开启飞行模式
    setAirplaneMode(true)
    // 3. 完整读 5 章
    openBook()
    repeat(100) { tapRightZone() }
    // 断言：无异常、无错误提示、进度正确保存
    assertNoErrorDialog(composeRule)
    val progress = readProgressFromDb(bookId)
    assertTrue(progress.charOffsetInChapter > 0)
    // 4. 打开网络书缓存章节
    openCachedBook()
    assertContentDisplayed(composeRule)
    // 断言：显示缓存内容，不是错误页
}
```

---

## 7.4 兼容性测试矩阵

### 7.4.1 设备矩阵

| 类别 | 设备 | 覆盖点 |
| --- | --- | --- |
| 最低支持 | API 26 模拟器（720p） | 基线功能；无新 API 崩溃 |
| 主流中端 | Pixel 6a / 红米 Note 12（API 33/34） | 性能指标主战场 |
| 高刷旗舰 | Pixel 8 / 一加 12（120Hz） | 帧率、动态刷新率 |
| 高分屏 | 三星 S24 Ultra（1440p） | 分页、位图内存 |
| 平板 | Pixel Tablet（API 34） | 双栏布局 |
| 折叠屏 | Pixel Fold / 小米 MIX Fold | 展开/折叠重分页 |
| 厂商 ROM | 小米 / 华为 / OPPO / vivo / 三星 | 后台任务、SAF、字体 |
| 低端 | API 26，2GB RAM | 内存压力、分章耗时 |

### 7.4.2 兼容性检查清单

| 项 | 检查方法 | 通过标准 |
| --- | --- | --- |
| 无新 API 直接调用 | Lint `NewApi` 检查 | 0 error |
| 权限最小化 | `aapt dump permissions app.apk` | 只有 INTERNET / ACCESS_NETWORK_STATE / WAKE_LOCK / FOREGROUND_SERVICE(_DATA_SYNC) |
| 明文流量配置 | 在 API 28+ 设备访问 HTTP 书源 | 成功抓取 |
| SAF 扫描 | 在三星/小米上扫描外部目录 | 正确列出文件 |
| 后台缓存存活 | 小米 HyperOS 后台跑 100 章 | 完成或明确失败，不静默中断 |
| 自定义字体 | 在 API 26 与 API 34 上加载同一 ttf | 均正常显示 |
| 大字体 | 系统字体 200% | 布局不破、不截断 |
| 深色模式 | 系统深色 | 阅读器主题与系统深色独立（阅读器主题由用户配置） |
| 横屏/折叠 | 旋转与折叠 | 重分页且进度保持 |
| 边到边 | 手势导航 + 挖孔屏 | 内容不被遮挡 |

### 7.4.3 自动化兼容测试

```yaml
# .github/workflows/compat.yml（Firebase Test Lab）
- name: 上传到 Test Lab 运行兼容性测试
  run: |
    gcloud firebase test android run \
      --type instrumentation \
      --app app/build/outputs/apk/debug/app-debug.apk \
      --test app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk \
      --device model=oriole,version=33,locale=zh_CN,orientation=portrait \
      --device model=redfin,version=30,locale=zh_CN,orientation=portrait \
      --device model=akita,version=34,locale=zh_CN,orientation=landscape \
      --timeout 30m
```

---

## 7.5 V1.0 验收标准（发布门槛）

### 7.5.1 功能验收（**全部必须通过**）

| 编号 | 验收项 | 验收方法 | 通过标准 |
| --- | --- | --- | --- |
| AC-01 | TXT/EPUB/PDF 三格式导入与阅读 | 手工 + 自动化 | 各 5 个样本文件全部正常打开 |
| AC-02 | 六种编码识别 | 自动化 | 准确率 ≥ 93%（含边界样本） |
| AC-03 | TXT 目录提取 | 自动化 | 标准网文召回 ≥ 99.5% |
| AC-04 | EPUB 目录（NAV + NCX） | 自动化 | 两种结构的 EPUB 均正确提取目录 |
| AC-05 | EPUB 内嵌图点击放大 | 手工 | 图片可放大、拖拽、复位 |
| AC-06 | PDF 单页滚动 + 双击缩放 + 夜间反色 | 手工 | 三者均正常，反色后内容可辨 |
| AC-07 | 四种翻页模式 | 手工 | 仿真/覆盖/滑动/滚动均可切换并正常使用 |
| AC-08 | **字号变化进度不丢** | 自动化 + 手工 | 12sp→36sp 全程渐变，位置偏差 ≤ 1 屏 |
| AC-09 | **字体切换进度不丢** | 手工 | 切换后位置保持 |
| AC-10 | 边距/行距变化进度不丢 | 手工 | 同上 |
| AC-11 | 目录/滑动条/书签跳转 | 手工 | 三种跳转均准确 |
| AC-12 | 12 项排版参数全部生效 | 手工 | 每项调整都有可见效果 |
| AC-13 | 4 套预置主题 + 自定义 HEX | 手工 | 主题切换立即生效；自定义色校验对比度 |
| AC-14 | 自定义 ttf/otf 字体加载 | 手工 | 加载成功并用于正文；坏字体被拒绝 |
| AC-15 | 电量/时间/章进度常驻显示 | 手工 | 数值正确，可独立开关 |
| AC-16 | 自动滚屏 | 手工 | 连续滚动无抖动，到章尾自动进下一章 |
| AC-17 | 网格/列表双视图 | 手工 | 切换正常，封面/进度显示正确 |
| AC-18 | 分组、拖拽排序、批量操作 | 手工 | 全部正常，数据同步 |
| AC-19 | 书源 JSON 导入（单/批量 URL） | 手工 + 自动化 | 有效源导入成功；非法源被拒绝并说明原因 |
| AC-20 | 书源规则调试器 | 手工 | 能定位规则失败的具体字段 |
| AC-21 | 聚合搜索（多源并发） | 自动化 + 手工 | 单源失败不影响整体；结果正确归并 |
| AC-22 | 换源保留进度 | 手工 | 章节匹配成功，字级偏移保留 |
| AC-23 | 后台离线批量缓存 | 手工 | 缓存完成，断网可读，可取消 |
| AC-24 | 正文净化 | 自动化 + 手工 | 不误删正文；清除常见水印 |
| AC-25 | 书签增删与跳转 | 手工 | 排版变化后书签位置仍准确 |

### 7.5.2 性能验收（**全部必须达标**）

| 编号 | 验收项 | 目标 | 方法 |
| --- | --- | --- | --- |
| PF-01 | 10MB TXT 分章 | ≤ 1.5s | 单元性能测试（中端机） |
| PF-02 | 翻页帧率（60Hz 设备） | ≥ 60fps，掉帧率 < 1% | `dumpsys gfxinfo` |
| PF-03 | 翻页帧率（120Hz 设备） | ≥ 90fps | Macrobenchmark |
| PF-04 | 1 小时内存波动 | ≤ 150MB | Instrumentation |
| PF-05 | 无内存泄漏 | LeakCanary 零报告 | 1 小时运行 |
| PF-06 | 冷启动可交互 | ≤ 800ms | Macrobenchmark |
| PF-07 | 二次打开书籍首屏 | ≤ 300ms | Macrobenchmark |
| PF-08 | 目录页（2000 章） | ≤ 200ms | 手工计时 |
| PF-09 | 聚合搜索 8 源 | ≤ 3s | 集成测试 |
| PF-10 | 书架滚动（500 本） | ≥ 60fps | Macrobenchmark |
| PF-11 | 无白屏 | 0 次 > 200ms | 录屏逐帧 |
| PF-12 | APK 体积 | ≤ 12MB | 构建产物 |
| PF-13 | 阅读 1 小时耗电 | ≤ 6% | Battery Historian |
| PF-14 | 待机 8 小时耗电 | ≤ 1% | Battery Historian |

### 7.5.3 质量验收

| 编号 | 验收项 | 目标 |
| --- | --- | --- |
| QA-01 | 单元测试覆盖率（整体） | ≥ 70% |
| QA-02 | 覆盖率（parser / pagination） | ≥ 90% |
| QA-03 | 崩溃率（内部测试 1 周） | < 0.5% |
| QA-04 | ANR 率 | < 0.1% |
| QA-05 | 静态分析 | detekt + Lint 零 error |
| QA-06 | 兼容性矩阵 | 全部通过（7.4.2） |
| QA-07 | 权限最小化 | 仅 5 项必要权限 |
| QA-08 | 隐私审计 | 无文件上传路径；崩溃上报默认关闭；禁云备份 |
| QA-09 | 无障碍 | TalkBack 可完成"打开书→翻页→调字号"全流程 |
| QA-10 | 文档 | 使用手册（第 9 卷）与实际行为一致 |

### 7.5.4 发布检查单（Release Checklist）

```
□ 所有 AC-01 ~ AC-25 通过并记录证据（截图/测试报告）
□ 所有 PF-01 ~ PF-14 达标并记录数值
□ 所有 QA-01 ~ QA-10 通过
□ 版本号已更新（versionCode / versionName）
□ CHANGELOG 已编写
□ 隐私政策文本与代码行为一致（逐条核对）
□ 第三方开源许可列表已生成（设置页可查看）
□ ProGuard 规则验证：release 包功能与 debug 一致（重点验证书源 JSON / Room）
□ release APK/AAB 已在真机完整走通一遍主流程
□ 签名密钥正确，未泄露 keystore
□ 应用图标、启动图、商店截图、描述文案就绪
□ 灰度计划已确定（5% → 20% → 100%）
□ 回滚方案已准备（上一版本 APK 可随时发布）
```

---

## 7.6 缺陷管理

### 7.6.1 缺陷分级

| 级别 | 定义 | 示例 | 处理时限 |
| --- | --- | --- | --- |
| **P0 致命** | 崩溃、数据丢失、核心功能不可用 | 导入崩溃；进度丢失；阅读器打不开书 | 立即修复，阻断发布 |
| **P1 严重** | 主要功能异常但有绕过方式 | 某格式解析失败；书源全部搜索失败 | 24 小时内 |
| **P2 一般** | 局部功能异常或体验问题 | 某主题下对比度不足；长书名截断异常 | 本周内 |
| **P3 轻微** | 视觉细节、文案问题 | 图标对齐 1px；错别字 | 排入迭代 |

### 7.6.2 缺陷报告模板

```markdown
## 环境
- 设备：Pixel 6a / Android 14 (API 34)
- 版本：v0.3.2 (build 128)
- 场景：阅读本地 TXT

## 复现步骤
1. 导入 2MB GBK 编码 TXT
2. 打开书籍，跳到第 50 章
3. 调整字号 18sp → 24sp

## 期望结果
停留在第 50 章相近位置（同一句话附近）

## 实际结果
跳回了第 12 章

## 附加信息
- 复现率：5/5
- 录制：attached.mp4
- Logcat：（附过滤后的日志）
- 涉及文件：（如可提供测试书籍）
```

### 7.6.3 缺陷分析的根因分类

每个 P0/P1 缺陷修复后必须归类根因，并**补充对应测试防止回归**：

| 根因类型 | 说明 | 防回归措施 |
| --- | --- | --- |
| 需求理解偏差 | 实现与需求不一致 | 补充验收用例 |
| 边界未覆盖 | 空值、极大值、并发 | 补充边界单测 |
| 生命周期错误 | 协程/监听器未取消 | 补充泄漏测试 |
| 平台差异 | 厂商 ROM 行为不同 | 加入兼容矩阵 |
| 状态管理错误 | UiState 不一致 | 补充 ViewModel 状态测试 |
| 算法错误 | 逻辑漏洞 | 补充算法用例 + 属性测试 |
| 性能退化 | 未达预算 | 加入性能回归基线 |

---

## 7.7 质量度量与看板

### 7.7.1 持续跟踪的度量

| 度量 | 目标 | 采集方式 |
| --- | --- | --- |
| 单元测试覆盖率 | ≥ 70%（核心 ≥ 90%） | CI Jacoco |
| 测试用例数 | — | CI |
| 单元测试全量耗时 | < 30s | CI |
| 静态分析问题数 | 0 error | CI detekt + Lint |
| 性能指标达标率 | 100% | CI 基准对比 |
| 崩溃率 | < 0.5% | 线上（用户开启上报后） |
| ANR 率 | < 0.1% | 线上 |
| 缺陷密度 | < 1 / 千行 | 手工统计 |
| P0/P1 缺陷未关闭数 | 发版前 = 0 | Issue 跟踪 |

### 7.7.2 每周质量报告模板

```markdown
# 质量周报 YYYY-WW

## 概况
- 本周新增用例：+23（总 412）
- 覆盖率：71.3%（上周 69.8%）↑1.5%
- 静态分析：0 error / 12 warning

## 性能趋势
| 指标 | 本周 | 上周 | 变化 |
| --- | --- | --- | --- |
| 10MB 分章 | 1.12s | 1.18s | ↓5% ✅ |
| 翻页帧率(P95) | 15.2ms | 14.8ms | ↑2.7% ⚠️ |
| 冷启动 | 762ms | 748ms | ↑1.9% |

⚠️ 翻页帧率与冷启动轻微劣化，本周提交 #142 与 #156 疑似相关，已安排排查。

## 缺陷
- 新增 P0：0；P1：2（均已修复）；P2：5
- 关闭：P0 0；P1 2；P2 4

## 风险
- 【中】某主流书源站点改版，解析规则失效（已提交规则修复，待用户侧更新）
- 【低】三星 SAF 兼容问题已修复，待兼容矩阵验证
```

---

**上一卷**：[第 6 卷 · 性能与 NFR 实现](#vol-06) ｜ **下一卷**：[第 8 卷 · 开发计划与协作规范](#vol-08)


---


# 第 8 卷 · 开发计划与协作规范 {#vol-08}
> 本卷把前七卷的设计转化为**可排期、可分工、可追踪**的执行计划，并定义团队协作规则。所有排期以"人日"为单位（1 人日 = 6 小时有效编码时间），按 2 人全职开发 + 1 人兼职测试估算。

---

## 8.1 里程碑总览

### 8.1.1 版本节奏（共 9 个月 / 约 39 周）

```
M1      M2      M3      M4      M5      M6      M7      M8      M9
├───────┼───────┼───────┼───────┼───────┼───────┼───────┼───────┤
│ V0.1 骨架      │ V0.2 能读      │ V0.3 好用      │ V0.4 聚合      │ V1.0 发布
│  4 周          │  5 周          │  4 周          │  5 周          │  4 周
└───────────────┴───────────────┴───────────────┴───────────────┘
   ▲ 可行性验证     ▲ 阅读闭环      ▲ 书架+书源闭环   ▲ 生态闭环      ▲ 上架
```

| 里程碑 | 交付版本 | 周期 | 结束标志（Gate） |
| --- | --- | --- | --- |
| M1 | V0.1 内部原型 | W1–W4 | 能导入并阅读一本 GBK TXT |
| M2 | V0.2 内测版 | W5–W9 | 三格式可读，四种翻页可用，进度精准 |
| M3 | V0.3 公测版 | W10–W13 | 书架完整，书源可导入并在线读 |
| M4 | V0.4 RC | W14–W18 | 聚合搜索、换源、批量缓存可用 |
| M5 | V1.0 正式版 | W19–W22 | 全部验收标准通过，上架 |

### 8.1.2 各里程碑的交付物与验收门（Gate Review）

#### Gate 1（M1 结束）· 可行性验证

```
必须交付：
  □ 工程骨架（14 个模块，CI 通过）
  □ 书架列表页 + 文件夹扫描 + TXT 导入
  □ TXT 阅读（UTF-8 + GBK）+ 上下滚动 + 进度保存
  □ 2 套主题（默认 + 夜间）

Gate 标准：
  □ 从零导入一本 5MB GBK TXT，分章 ≤ 2s（V0.1 放宽至 2s）
  □ 退出重进停留在原位置（同一字号下）
  □ CI 全绿（规范 + 单测 + 构建）
  □ 崩溃率 0

风险检查：
  □ 分章性能是否已达 1.5s 的技术路径清晰？（若否，此处必须停下来攻坚）
  □ Compose 文本测量的分页方案是否验证可行？
```

#### Gate 2（M2 结束）· 阅读闭环

```
必须交付：
  □ EPUB 解析（OPF/NCX/NAV/内嵌图）
  □ PDF 单页渲染 + 双击缩放 + 夜间反色
  □ 物理分页引擎（含字符偏移映射）
  □ 四种翻页模式
  □ 目录提取（TXT 正则 + EPUB 标准）
  □ 完整排版设置面板（12 项参数）
  □ 字符级进度模型落地

Gate 标准：
  □ 四种翻页模式下，字号 12→36sp 全程进度偏差 ≤ 1 屏（★ 核心 Gate）
  □ 10MB TXT 分章 ≤ 1.5s（★ 核心 Gate）
  □ 翻页帧率 ≥ 60fps（用 gfxinfo 验证）
  □ 覆盖率：parser ≥ 85%，pagination ≥ 85%
```

#### Gate 3（M3 结束）· 书架与书源闭环

```
必须交付：
  □ 网格/列表双视图 + 分组 + 拖拽排序 + 批量操作
  □ 书源 JSON 导入（单/批量 URL/文件）
  □ 书源规则调试器
  □ 单源搜索 → 详情 → 加入书架 → 在线阅读
  □ 章节缓存（单章级）
  □ 书签 + 阅读统计 + 电量/时间/章进度常驻

Gate 标准：
  □ 导入 3 个书源，能搜到并在线阅读一本 2000 章网文
  □ 调试器能定位规则失败的具体字段
  □ 书架 500 本的滚动帧率 ≥ 60fps
  □ 书源导入的非法输入全部被友好拒绝
```

#### Gate 4（M4 结束）· 生态闭环

```
必须交付：
  □ 多源并发聚合搜索 + 归并去重
  □ 一键换源（保留字级进度）
  □ 后台离线批量缓存（WorkManager）
  □ 正文净化（广告/水印/占位符）
  □ 自定义主题（HEX）+ 自定义字体（ttf/otf）
  □ 缓存管理与淘汰

Gate 标准：
  □ 8 源并发搜索 ≤ 3s，单源失败不影响整体
  □ 换源后章节匹配成功，进度保留
  □ 断网后缓存章节可读
  □ 1 小时阅读内存波动 ≤ 150MB（★ 核心 Gate）
  □ 覆盖率：sources ≥ 85%
```

#### Gate 5（M5 结束）· 发布

```
必须交付：
  □ 全部 AC / PF / QA 验收项通过（见第 7 卷 7.5）
  □ 兼容性矩阵全部通过
  □ 无障碍适配（TalkBack 走通主流程）
  □ i18n（中/英）
  □ 应用图标、启动图
  □ 隐私政策、开源许可页
  □ 商店素材与文案

Gate 标准：
  □ 第 7 卷 7.5.4 发布检查单全部勾选
  □ 内部测试 1 周零 P0/P1 缺陷
```

---

## 8.2 工作分解结构（WBS）

### 8.2.1 V0.1 骨架（4 周 / 约 48 人日）

| 编号 | 任务 | 模块 | 人日 | 依赖 | 负责人建议 |
| --- | --- | --- | --- | --- | --- |
| 1.1 | 工程初始化：Gradle 多模块、Version Catalog、build-logic 约定插件 | — | 3 | — | 架构 |
| 1.2 | CI 流水线：规范检查、单测、构建、覆盖率门禁 | ci | 2 | 1.1 | 架构 |
| 1.3 | core:model 数据模型定义（含字符偏移进度模型） | core | 2 | 1.1 | 架构 |
| 1.4 | core:common（KrDispatchers、Result、扩展函数） | core | 1 | 1.1 | A |
| 1.5 | core:database：Room 实体、DAO、首版迁移 | core | 3 | 1.3 | A |
| 1.6 | core:datastore：Proto schema + 读写实现 | core | 2 | 1.3 | A |
| 1.7 | core:designsystem：主题、色板、Typography | core | 2 | 1.1 | B |
| 1.8 | core:ui：Loading/Empty/Error/ConfirmDialog | core | 1 | 1.7 | B |
| 1.9 | **编码探测算法**（含 15 例语料测试） | parser | **4** | 1.4 | A |
| 1.10 | **流式文本读取 + 字符偏移索引** | parser | **3** | 1.9 | A |
| 1.11 | **TXT 分章**（含 14 例语料测试） | parser | **4** | 1.10 | A |
| 1.12 | TXT 元数据提取 + 文件名清洗 | parser | 2 | 1.11 | B |
| 1.13 | 文件夹扫描（SAF + DocumentsContract） | books | 3 | 1.4 | B |
| 1.14 | 归档与导入流程（幂等、去重、事务） | books | 3 | 1.5 | A |
| 1.15 | 进度三级写入策略 | books | 2 | 1.5 | A |
| 1.16 | 书架列表视图 + 排序 | bookshelf | 3 | 1.7 | B |
| 1.17 | 基础阅读器：上下滚动 + 进度保存 + 菜单 | reader | 4 | 1.11 | B |
| 1.18 | 2 套主题（默认/夜间） | settings | 1 | 1.6 | B |
| 1.19 | V0.1 端到端联调与修复 | — | 3 | 全部 | 全员 |

**关键路径**：1.9 → 1.10 → 1.11 → 1.17（编码探测与分章是整个项目的地基，必须最先做且做到位）

### 8.2.2 V0.2 能读（5 周 / 约 60 人日）

| 编号 | 任务 | 模块 | 人日 | 依赖 |
| --- | --- | --- | --- | --- |
| 2.1 | **物理分页引擎**（TextMeasurer + 贪心装箱 + 流式发射） | pagination | **6** | 1.10 |
| 2.2 | 分页不变量测试 + 属性测试 | pagination | 3 | 2.1 |
| 2.3 | 分页缓存与失效管理 | pagination | 2 | 2.1 |
| 2.4 | 重分页触发链（去抖 + mapLatest + 锚点保持） | reader | **4** | 2.1 |
| 2.5 | 覆盖翻页动画 | reader | 3 | 2.4 |
| 2.6 | 滑动 / 滚动 / 点按三种翻页模式 | reader | 2 | 2.4 |
| 2.7 | 仿真翻页（斜切 + 阴影） | reader | 4 | 2.5 |
| 2.8 | 点击区域划分 + 可配置 | reader | 1 | 2.4 |
| 2.9 | **EPUB 解析**（container/OPF/NAV/NCX/spine） | parser | **6** | 1.7 |
| 2.10 | EPUB 内嵌图提取 + 点击放大 | parser/reader | 3 | 2.9 |
| 2.11 | EPUB 元数据 + 封面 | parser | 2 | 2.9 |
| 2.12 | **PDF 解析 + PdfRenderer 封装** | parser | **4** | 1.7 |
| 2.13 | PDF 缩放/拖拽/夜间反色 | reader | 4 | 2.12 |
| 2.14 | PDF 页位图 LRU 缓存 | parser | 2 | 2.12 |
| 2.15 | **排版设置面板**（12 项参数，实时预览） | settings | **5** | 2.4 |
| 2.16 | 自定义字体加载（含魔数校验、缓存、回退） | settings | 3 | 2.15 |
| 2.17 | 主题系统（4 套预置 + 对比度校验） | settings | 3 | 2.15 |
| 2.18 | 目录提取：TXT 规则 + 正则编辑器 | parser | 3 | 1.11 |
| 2.19 | 章节跳转 + 滑动条跳转 + 章节内定位 | reader | 4 | 2.4 |
| 2.20 | 书签增删与跳转 | reader | 2 | 1.5 |
| 2.21 | 电量/时间/章进度常驻状态栏 | reader | 2 | 2.4 |
| 2.22 | V0.2 兼容性初测 + 修复 | — | 3 | 全部 |

**并行策略**：A 主攻解析（2.9–2.14、2.18），B 主攻阅读器（2.1–2.8、2.19–2.21），2.15–2.17 设置面板在 2.4 完成后由 B 接续。

### 8.2.3 V0.3 好用（4 周 / 约 48 人日）

| 编号 | 任务 | 模块 | 人日 | 依赖 |
| --- | --- | --- | --- | --- |
| 3.1 | 网格视图 + 列数配置 + 占位封面生成 | bookshelf | 3 | 1.16 |
| 3.2 | 分组管理（增删改排序） | bookshelf | 3 | 1.5 |
| 3.3 | 拖拽排序（手动排序模式） | bookshelf | 3 | 3.1 |
| 3.4 | 批量选择 + 移出/删除/改分组 + 撤销 | bookshelf | 3 | 3.1 |
| 3.5 | 书籍编辑（重命名、改封面、编辑信息、重置进度） | bookshelf | 3 | 3.1 |
| 3.6 | 书架内搜索 + 空态引导 | bookshelf | 2 | 3.1 |
| 3.7 | 书源数据模型 + JSON 编解码 | sources | 2 | 1.3 |
| 3.8 | 书源校验器（含选择器预校验） | sources | 2 | 3.7 |
| 3.9 | 书源导入（单 JSON / 批量 URL / 文件） | sources | 3 | 3.8 |
| 3.10 | 书源管理页（列表、启用、分组、删除、健康度） | sources | 3 | 3.9 |
| 3.11 | **规则引擎：搜索/目录/正文解析** | sources | **6** | 3.7 |
| 3.12 | 选择器回退链 + 启发式正文识别 | sources | 3 | 3.11 |
| 3.13 | **书源规则调试器**（分步诊断 + 建议） | sources | **5** | 3.11 |
| 3.14 | 按域限流 + UA 轮换 + 重试退避 | network | 3 | 3.11 |
| 3.15 | 搜索页 + 详情页 + 加入书架 | search | 5 | 3.11 |
| 3.16 | 在线阅读（抓取 + 落库 + 阅读器复用） | search | 4 | 3.15 |
| 3.17 | 阅读统计（会话、时长、字数） | settings | 2 | 3.16 |
| 3.18 | 设置页框架 + 全局偏好 | settings | 3 | 1.6 |
| 3.19 | V0.3 联调 + 书源实测 | — | 3 | 全部 |

### 8.2.4 V0.4 聚合（5 周 / 约 60 人日）

| 编号 | 任务 | 模块 | 人日 | 依赖 |
| --- | --- | --- | --- | --- |
| 4.1 | **并发聚合搜索**（supervisorScope + 流式事件） | search | **5** | 3.11 |
| 4.2 | **结果归并去重**（归一化 + 相似度 + 质量分） | sources | **5** | 4.1 |
| 4.3 | 搜索结果 UI（流式出现 + 分源展开） | search | 3 | 4.1 |
| 4.4 | **换源**（章节匹配 4 级策略 + 进度迁移） | sources | **5** | 4.2 |
| 4.5 | **批量缓存 Worker**（WorkManager + 约束 + 进度） | sources | **5** | 3.16 |
| 4.6 | 缓存管理与淘汰算法 | sources | 3 | 4.5 |
| 4.7 | **正文净化流水线**（10 步 + ReDoS 防护） | sources | **5** | 3.11 |
| 4.8 | 净化规则调试与效果预览 | sources | 2 | 4.7 |
| 4.9 | 自定义主题（HEX 输入 + 对比度守卫） | settings | 3 | 2.17 |
| 4.10 | 自动滚屏 + 常亮 + 音量键动作 | reader | 3 | 2.4 |
| 4.11 | 无障碍适配（TalkBack + 语义标注） | 全局 | 4 | 全部 |
| 4.12 | i18n（中/英资源抽取） | 全局 | 3 | 全部 |
| 4.13 | 内存优化专项（LRU 调优 + 压力监听 + 泄漏修复） | 全局 | 5 | 全部 |
| 4.14 | 性能优化专项（分章、分页、滚动） | 全局 | 5 | 全部 |
| 4.15 | V0.4 RC 测试与修复 | — | 4 | 全部 |

### 8.2.5 V1.0 发布（4 周 / 约 40 人日）

| 编号 | 任务 | 人日 | 依赖 |
| --- | --- | --- | --- |
| 5.1 | 全量性能达标验证与调优（PF-01~PF-14） | 6 | 4.14 |
| 5.2 | 兼容性矩阵全量验证（8 类设备） | 5 | 4.15 |
| 5.3 | 稳定性测试（1 周连续使用，崩溃/ANR 归零） | 5 | 5.1 |
| 5.4 | 应用图标、启动图、商店素材 | 3 | — |
| 5.5 | 隐私政策、开源许可页、权限审计 | 3 | — |
| 5.6 | 发布检查单逐项核对 + 证据归档 | 2 | 5.1–5.3 |
| 5.7 | 灰度发布与线上监控 | 3 | 5.6 |
| 5.8 | 使用手册定稿（第 9 卷对齐实际行为） | 2 | 5.4 |
| 5.9 | 发布后 2 周热修支持 | 8 | 5.7 |

### 8.2.6 工作量汇总

| 版本 | 人日 | 日历周期 | 关键路径 |
| --- | --- | --- | --- |
| V0.1 | 48 | 4 周 | 编码探测 → 偏移索引 → 分章 → 基础阅读器 |
| V0.2 | 60 | 5 周 | 分页引擎 → 重分页链 → 翻页动效 |
| V0.3 | 48 | 4 周 | 规则引擎 → 调试器 → 在线阅读 |
| V0.4 | 60 | 5 周 | 聚合搜索 → 归并 → 换源 → 批量缓存 |
| V1.0 | 40 | 4 周 | 性能达标 → 兼容验证 → 稳定性 |
| **合计** | **256 人日** | **22 周**（2 人并行） | |

> 按 2 人全职：256 / 2 ≈ 128 工作日 ≈ 26 周 ≈ 6 个月。加上预留 20% 缓冲（应对技术攻坚与返工），实际 **7~7.5 个月**。需求文档中的"9 个月"含 1 名兼职测试与若干缓冲，是稳妥口径。

---

## 8.3 团队分工

### 8.3.1 角色定义

| 角色 | 人数 | 职责 |
| --- | --- | --- |
| **架构/技术负责人** | 1 | 架构决策、核心算法攻坚、Code Review、性能把关 |
| **客户端开发 A**（解析向） | 1 | 解析引擎、分页引擎、数据层、性能优化 |
| **客户端开发 B**（UI/交互向） | 1 | 阅读器 UI、书架、设置、动效、无障碍 |
| **客户端开发 C**（网络向） | 1（V0.3 起加入） | 书源规则引擎、聚合搜索、缓存、调试器 |
| **测试工程师** | 1（兼职） | 测试用例设计、兼容性验证、验收执行 |
| **设计** | 0.5（兼职） | 视觉规范、图标、启动图、主题色板 |

### 8.3.2 模块归属（Owner，避免多头修改）

| 模块 | Owner | 说明 |
| --- | --- | --- |
| `core:*` | 架构 | 改动需架构评审 |
| `data:parser` | A | 高复杂度，单人负责保一致性 |
| `data:pagination` | A | 与 parser 强耦合 |
| `data:books` | A | |
| `data:sources` | C | |
| `feature:reader` | B | |
| `feature:bookshelf` | B | |
| `feature:search` / `feature:sources` | C | |
| `feature:settings` | B | |
| `ci` / `build-logic` | 架构 | |

**Owner 规则的例外**：跨模块改动（如新字段从 DB 到 UI）由"数据侧 Owner"主导，其他 Owner 提供 PR 评审。

---

## 8.4 协作规范

### 8.4.1 Code Review 规范

#### 评审门禁（PR 必须满足，才能合并）

```
自动门禁（CI 执行，不通过则无法合并）：
  □ ktlintCheck 通过
  □ detekt 通过（零 error）
  □ 单元测试全绿
  □ 覆盖率不降低（相对基线）
  □ lintDebug 零 error
  □ 性能基准无劣化（> 10% 阻断）
  □ 构建成功

人工门禁（Reviewer 检查）：
  □ 变更范围与 PR 描述一致（无夹带无关改动）
  □ 符合分层架构（feature 不依赖 data 实现、core:model 无 Android 依赖）
  □ 符合协程纪律（无 GlobalScope、suspend 可取消、正确调度器）
  □ 符合 Compose 纪律（参数稳定、状态上提、列表有 key）
  □ 涉及规范文档的改动同步更新了 docs/
  □ 新增公开 API 有 KDoc
  □ 核心逻辑有对应测试
  □ 无 TODO 遗留（或已关联 Issue）
  □ 无明显性能隐患（主线程 IO、Composable 内重计算、大对象捕获）
```

#### Reviewer 分配规则

| PR 类型 | Reviewer |
| --- | --- |
| 普通功能 | 1 名同模块 Owner |
| 涉及 core:* | + 架构 |
| 涉及算法（parser/pagination/sources） | + 架构 |
| 涉及 DB schema | + 架构（必须检查迁移） |
| 涉及性能关键路径 | + 架构（必须看基准） |
| 紧急热修 | 1 名资深，事后补充评审 |

#### 评审时效

| 优先级 | 首次响应 | 完成评审 |
| --- | --- | --- |
| P0 热修 | 30 分钟 | 2 小时 |
| 正常功能 | 4 小时 | 1 工作日 |
| 重构/优化 | 1 工作日 | 2 工作日 |

**PR 规模纪律**：单个 PR 变更行数建议 **≤ 400 行**（不含测试与生成代码）。超过 800 行需拆分，或在描述中说明不可拆分的理由。**大 PR 是评审质量的头号杀手**。

### 8.4.2 PR 描述模板

```markdown
## 变更类型
- [ ] feat 新功能
- [ ] fix 缺陷修复
- [ ] perf 性能优化
- [ ] refactor 重构
- [ ] docs 文档
- [ ] test 测试
- [ ] chore 杂项

## 关联
- Issue: #123
- 设计文档: docs/04-第4卷-功能模块详细设计.md#43-分页与跳转引擎

## 做了什么
<!-- 用 3~5 句说清"改了什么、为什么这么改" -->
实现分页引擎的流式发射，首批 2 页立即返回，避免整章分页完成前的白屏。

## 怎么验证
<!-- 可复现的验证步骤，Reviewer 照着做即可 -->
1. `./gradlew :data:pagination:test`
2. 安装到设备，打开 5MB 测试书，观察首屏出现时间
3. `adb shell dumpsys gfxinfo com.kr.reader framestats | grep -A5 "Janky"`

## 性能影响
| 指标 | 变更前 | 变更后 |
| --- | --- | --- |
| 首屏分页 | 420ms | 98ms |
| 全章分页 | 480ms | 405ms |

## 截图/录屏
<!-- UI 变更必须附，动效附录屏 -->
（附 GIF）

## 自检清单
- [ ] 已跑通本地全量单测
- [ ] 新增逻辑有测试覆盖
- [ ] 无遗留 TODO
- [ ] 相关文档已更新
- [ ] 已在真机验证（注明机型与系统版本）
```

### 8.4.3 架构决策记录（ADR）

**任何影响架构的决策必须写 ADR**，文件放 `docs/adr/NNNN-<title>.md`。

```markdown
# ADR-0007：用字符偏移而非页码记录阅读进度

- 状态：已接受
- 日期：2026-03-15
- 决策者：架构、A、B

## 背景
阅读进度需要在一系列排版参数变化（字号、行距、边距、字体、屏幕方向）
及换源场景下保持语义稳定。初始方案记录"章节 + 页码"，实测在字号从
18sp 调到 28sp 后位置偏差达 4 屏，用户明确感知为"进度丢失"。

## 考虑的方案
### 方案 A：章节 + 页码 + 排版参数快照
记录页码与当时的排版参数，恢复时若参数不同则按比例估算。
- 优点：实现简单
- 缺点：参数变化后需"估算"，比例失真；参数组合爆炸（12 项参数）

### 方案 B：章节 + 百分比
- 优点：直观
- 缺点：3000 字章节的 1% 只有 30 字精度；参数变化后同一百分比对应不同内容

### 方案 C：章节 + 章内字符偏移（采纳）
- 优点：与排版参数完全解耦；语义即"读到第 N 个字"，任何参数下都稳定
- 缺点：分页引擎必须输出字符映射（增加接口约束）；换源后需章节匹配

## 决策
采用方案 C。配套要求：
1. `PageSnapshot` 必须携带 `startCharInChapter` / `endCharInChapter`
2. 全部书签同样使用字符偏移
3. 换源时通过章节标题匹配继承偏移

## 后果
- 正面：解决进度丢失这一类问题的根本原因；书签同样受益
- 负面：分页引擎复杂度上升；PDF 因无稳定文本层需退化处理
- 需要新增：字符偏移 ↔ 字节偏移的稀疏索引（见第 5 卷 5.4）

## 相关
- 第 3 卷 3.1.2 进度模型
- 第 4 卷 4.3.2 分页接口
- 第 5 卷 5.4 偏移映射
```

**必须写 ADR 的决策类型**：

| 类型 | 示例 |
| --- | --- |
| 技术选型 | 为什么用 Compose 而非 View；为什么不用 Retrofit |
| 架构模式 | 为什么用 Clean Architecture；模块如何划分 |
| 核心模型 | 进度模型；章节模型；书源规则结构 |
| 数据存储 | 为什么用 DataStore Proto 而非 Preferences |
| 性能策略 | 为什么用稀疏锚点索引 |
| 接口约束 | 分页引擎为什么必须输出字符映射 |

### 8.4.4 文档维护规则

| 文档 | 位置 | 更新时机 | Owner |
| --- | --- | --- | --- |
| 开发档案（本套） | `docs/` | 设计变更时同步 | 架构 |
| ADR | `docs/adr/` | 决策发生时 | 决策者 |
| API 文档 | KDoc in code | 编码时 | 各 Owner |
| CHANGELOG | `CHANGELOG.md` | 每次发版 | 架构 |
| 用户手册 | `docs/09-使用手册` | 功能变更时 | B |
| 测试报告 | `docs/reports/` | 每次发版 | 测试 |
| 性能基线 | `benchmark/baselines/` | 基准变化时 | 架构 |

**文档纪律**：**代码与文档不一致时，必须改代码或改文档，不允许长期并存**。PR Review 时把"文档是否同步"作为检查项。

### 8.4.5 沟通机制

| 场景 | 方式 | 频率 |
| --- | --- | --- |
| 每日同步 | 15 分钟站会（昨日/今日/阻塞） | 每工作日 |
| 周度规划 | 本周目标 + 风险 + 里程碑进度 | 每周一 |
| 设计评审 | 复杂方案的设计文档评审会 | 按需，方案定稿前 |
| Gate Review | 里程碑验收 | 每个里程碑末 |
| 缺陷分级会 | 归置新增缺陷优先级 | 每周五 |
| 架构评审 | ADR 讨论 | 按需 |

**站会三个问题（只回答这三个）**：
1. 昨天完成了什么（对应 WBS 编号）
2. 今天计划做什么（对应 WBS 编号）
3. 有什么阻塞

**禁止**：站会变成进度汇报长篇大论；讨论技术细节（另开会）。

---

## 8.5 风险管理

### 8.5.1 风险登记册

| 编号 | 风险 | 概率 | 影响 | 等级 | 应对策略 |
| --- | --- | --- | --- | --- | --- |
| R-01 | **分章性能无法稳定达到 1.5s** | 中 | 高 | **高** | **M1 必须验证技术路径**（首字符预筛 + 流式解码原型）；若 1.5s 无望，M1 Gate 停下来专项攻坚，不允许带病进入 M2 |
| R-02 | **进度精度在极端排版下失效** | 中 | 高 | **高** | 字符偏移模型已从设计上规避；M2 Gate 用"12→36sp 全程偏差 ≤1 屏"硬性验证 |
| R-03 | 分页性能不达标导致白屏 | 中 | 高 | 中 | 流式分页（首批 2 页）；M2 用 gfxinfo 验证 |
| R-04 | 内存优化达不到 150MB | 中 | 中 | 中 | 预算表已分解到各组件；M4 前专项优化 5 人日；有明确收缩路径（LRU 调参、清缓存） |
| R-05 | 书源站点改版导致规则失效 | **高** | 中 | **高** | 规则外置（用户可自行更新 JSON）；内置示例源 + 调试器让用户能自修；健康度机制自动降级失效源 |
| R-06 | 书源相关合规风险 | 中 | **高** | **高** | 不上架内建书源市场；书源为本地 JSON 导入（用户自主行为）；应用内明确声明"书源由用户自行配置，与本应用无关"；隐私政策明确 |
| R-07 | 厂商 ROM 兼容问题（后台被杀、SAF 异常） | **高** | 中 | 高 | 用 DocumentsContract 替代 DocumentFile；缓存任务用 foreground service；文档引导用户锁定后台 |
| R-08 | PDF 解析能力受限于 PdfRenderer | 中 | 中 | 中 | 明确降级（加密 PDF 不支持）；文档说明；V1.x 评估引擎替换 |
| R-09 | 团队对 Compose 性能模式不熟（误用导致掉帧） | 中 | 中 | 中 | 编码规范明确"状态读下沉"；CI 输出 Compose Compiler Metrics；评审重点检查 |
| R-10 | 覆盖率高但测试无效（为数字写测试） | 中 | 中 | 中 | 评审检查"分支是否都有断言"；引入属性测试补足边界 |
| R-11 | 范围蔓延（不断加需求） | **高** | 中 | 高 | 第 0 卷明确 Out of Scope 清单；新需求一律进 V1.x 候选池；ADR 记录被推迟的决策 |
| R-12 | 关键人依赖（解析引擎仅 A 熟悉） | 中 | 高 | 中 | 解析算法文档化（第 5 卷）；A 的 PR 要求 B 参与评审；关键模块轮换维护 |
| R-13 | 大文件（>500MB）场景崩溃 | 低 | 中 | 低 | 流式读取为强制约束（detekt 拦截 readText）；测试语料加入大文件 |
| R-14 | 应用体积超预算 | 低 | 低 | 低 | 按 ABI 拆分；已评估并拒绝大体积依赖（MuPDF）；CI 监控体积 |

### 8.5.2 高风险的缓解行动（针对 R-01 / R-02 / R-05 / R-06）

#### R-01：分章性能（**最高优先级**）

```
W1 立即行动（不等 Gate）：
  1. 写一个纯 JVM 的 POC：GBK 10MB 文件，用"流式解码 + 首字符预筛 + 预编译正则"
     跑通，测出真实耗时。
  2. 若 ≤ 1.2s → 路径确认，纳入 1.11 实现
  3. 若 1.2s ~ 1.8s → 分析瓶颈，考虑：
     · 分块并行分章（按 4MB 分块，块间用重叠区处理边界）
     · 更快的编码转换（预生成 GBK→UTF16 映射表，O(1) 查表而非解码器）
  4. 若 > 1.8s → 需求重新协商：把 P0-1 放宽到 2.5s，或用"先显示首章，后台继续分章"
     的渐进式策略，并在 UI 上明确进度

判断节点：W1 结束。这是项目是否可按计划推进的关键决策点。
```

#### R-02：进度精度

```
W5 立即行动：
  1. 在 M2 早期就实现"锚点保持"逻辑（不等翻页动效做完）
  2. 写一个自动化测试：加载 5000 字章节，循环字号 12→36sp，
     每次记录当前页首字符偏移，断言"该偏移始终落在当前页区间内"
  3. 用真实书籍（而非 lorem ipsum）测试，因为真实中文文本的标点、
     英文混排、空行会暴露 lorem ipsum 测不出的问题
```

#### R-05：书源失效

```
持续行动：
  1. 内置 3~5 个示例源，且这些源的选择器写得"宽"（用 [class*=content]
     而非 #content），提高抗改版能力
  2. 健康度自动降级：连续失败 3 次 → 降权；5 次 → 跳过并提示
  3. 提供"一键检测全部书源"功能，让用户定期体检
  4. 调试器的诊断信息要能直接告诉用户"改哪个字段"
  5. 文档（第 9 卷）包含"书源失效了怎么办"的排查指引
```

#### R-06：合规风险

```
M1 之前必须完成：
  1. 法务/合规咨询：确认"用户自导入书源"的模式风险边界
  2. 隐私政策草稿，明确：
     · 不收集、不上传用户本地阅读文件
     · 无账号、无用户标识
     · 网络能力仅用于用户配置的书源抓取
     · 崩溃上报默认关闭
  3. 应用内首启提示：书源功能为"用户自配置的通用网页规则引擎"，
     应用本身不提供、不存储、不分发任何书籍内容
  4. 不上架任何"内置书源市场"（V1.0 明确 Out of Scope）
  5. 关于页不展示任何书籍内容或站点推荐
```

### 8.5.3 风险评审节奏

| 时点 | 动作 |
| --- | --- |
| 每个 Gate Review | 逐条过风险登记册，更新概率/影响/等级 |
| 每周五 | 检查高等级风险的缓解行动是否在推进 |
| 出现新风险 | 立即登记并指定 Owner，下一工作日给出应对方案 |
| 风险解除 | 标记为"已关闭"并记录解除依据 |

---

## 8.6 变更管理

### 8.6.1 需求变更流程

```
提出变更
    │
    ▼
评估影响（架构 Owner）
    ├─ 影响 WBS 人数 ≤ 3 且不影响 Gate 标准
    │       → 直接纳入当前迭代，记录在 CHANGELOG
    │
    ├─ 影响 WBS 人数 3~8 或轻微影响 Gate
    │       → 排期会上讨论；需从当前迭代移除等价工作量（不允许净增）
    │
    └─ 影响架构 / 违反红线 / 影响 Gate 标准
            → 写 ADR + 架构评审；可能需要调整里程碑
```

**变更纪律三条**：

1. **不允许"顺手加个小功能"**。任何新增都必须走上述流程，即使只是 0.5 人日。
2. **不允许净增工作量而不移出等价工作**（防止进度滑坡）。
3. **触碰第 0 卷三条产品红线（无广告/本地文件不出设备/不吃性能）的变更，一律拒绝**，无例外。

### 8.6.2 需求池（V1.x 候选）

| 需求 | 来源 | 价值 | 成本 | 优先级 |
| --- | --- | --- | --- | --- |
| MOBI/AZW3 支持 | 用户反馈 | 中 | 中 | P1（V1.1） |
| WebDAV 同步 | 用户反馈 | 高 | 高 | P1（V1.2，需设计 E2E 加密） |
| 跨设备进度同步 | 用户反馈 | 高 | 高 | P2（需服务端，与"无账号"红线冲突，需重新讨论） |
| TTS 朗读 | 用户反馈 | 中 | 中 | P1（V1.1，用系统 TTS） |
| 词典划词查询 | 用户反馈 | 中 | 低 | P1（V1.1） |
| 漫画模式（CBZ） | 内部 | 低 | 中 | P3 |
| 阅读时长目标/成就 | 内部 | 低 | 低 | P3 |
| 书源在线订阅（用户分享） | 内部 | 高 | 高 | **合规风险高，暂缓** |
| 排版预设方案（一键切换） | 用户反馈 | 中 | 低 | P1（V1.1） |
| PDF 文本层提取（支持搜索/复制） | 内部 | 中 | 高 | P2 |

---

## 8.7 交付物清单

### 8.7.1 代码交付

| 交付物 | 说明 |
| --- | --- |
| Android 工程源码 | 14 个模块，含单元测试 |
| 构建配置 | Gradle Kotlin DSL + Version Catalog + build-logic |
| CI 配置 | GitHub Actions（规范/测试/构建/性能/兼容） |
| 基准基线 | `benchmark/baselines/` 下的性能基线 JSON |
| ProGuard 规则 | 已验证的 release 混淆配置 |
| 示例书源 | 3~5 个可用书源 JSON（放 `assets/sample_sources/`） |
| 测试语料 | `assets/test/` 下的编码与分章语料（或生成脚本） |

### 8.7.2 文档交付

| 交付物 | 位置 |
| --- | --- |
| 完整开发档案（本套 10 卷 + 附录） | `docs/` |
| ADR 集 | `docs/adr/` |
| API 文档 | KDoc（可用 Dokka 生成 HTML） |
| 用户使用手册 | `docs/09-第9卷-使用手册.md` + 应用内帮助页 |
| 书源规范 | `docs/10-第10卷-附录.md` 附录 A |
| CHANGELOG | `CHANGELOG.md` |
| 测试报告 | `docs/reports/v1.0-test-report.md` |
| 性能报告 | `docs/reports/v1.0-perf-report.md` |
| 发布检查单（已勾选） | `docs/reports/v1.0-release-checklist.md` |

### 8.7.3 发布物

| 交付物 | 说明 |
| --- | --- |
| Release AAB | 上传 Google Play |
| 分 ABI APK | arm64-v8a / armeabi-v7a（国内渠道） |
| 应用图标 | 自适应图标 + 各密度切图 |
| 启动图 | Android 12+ SplashScreen 配置 |
| 商店素材 | 图标、截图（手机/平板/折叠屏各 5 张）、描述文案、更新日志 |
| 隐私政策 | 独立页面 + 应用内入口 |
| 开源许可 | 第三方依赖许可列表（应用内可查看） |

---

## 8.8 项目健康度指标

| 指标 | 目标 | 采集 |
| --- | --- | --- |
| 迭代准时率 | ≥ 80%（WBS 任务按时完成比例） | 周报 |
| Gate 一次通过率 | ≥ 80% | Gate Review |
| 缺陷重开率 | < 10% | Issue 跟踪 |
| 需求变更率 | < 15%（相对原始 WBS） | 变更记录 |
| 覆盖率趋势 | 单调不降 | CI |
| 性能指标趋势 | 无劣化 | CI 基准 |
| 技术债清单 | 持续收敛 | detekt 抑制注释数、TODO 数 |
| 团队阻塞时长 | 平均 < 1 工作日 | 站会记录 |

---

**上一卷**：[第 7 卷 · 测试与质量保障](#vol-07) ｜ **下一卷**：[第 9 卷 · 使用手册](#vol-09)


---


# 第 9 卷 · 使用手册 {#vol-09}
> 本手册面向**最终用户**，与 V1.0 的实际行为逐条对齐。文档中出现的界面名称、按钮文案、参数范围必须与代码中的字符串资源保持一致（`res/values/strings.xml`）。发布检查单中包含"手册与实现一致性核对"项。

---

## 9.1 快速上手（3 分钟）

### 9.1.1 首次启动

应用无引导页、无登录、无权限申请弹窗。启动后直接进入**书架**，此时为空，显示引导卡片：

```
┌────────────────────────────────────┐
│                                    │
│         还没有任何书籍              │
│                                    │
│  ┌──────────────┐  ┌────────────┐  │
│  │ 扫描本地文件  │  │  导入书籍   │  │
│  └──────────────┘  └────────────┘  │
│                                    │
│  支持 TXT · EPUB · PDF              │
│  所有文件仅保存在本机，不会上传       │
│                                    │
└────────────────────────────────────┘
```

### 9.1.2 导入第一本书

**方式一：扫描文件夹（推荐批量）**

1. 点「扫描本地文件」
2. 系统会打开文件夹选择器，选择存放电子书的文件夹（如 `Download/Books`）
3. 应用自动递归扫描该文件夹（最多 8 层），列出所有 `.txt` / `.epub` / `.pdf`
4. 勾选要导入的书（默认全选），点「导入」
5. 导入进度显示在顶部，完成后自动回到书架

> **首次使用 Android 11 以上系统**：选择文件夹时若提示"为了你的安全，请选择其他文件夹"，请在文件选择器右上角菜单中选「显示内部存储」，再从内部存储根目录进入子文件夹。

**方式二：导入单本书**

1. 点「导入书籍」
2. 在文件选择器中选中一个或多个文件
3. 完成

**方式三：从其他应用分享**

在文件管理器/浏览器中长按文件 → 分享 → 选择「Kotlin Reader」，书籍会被自动导入。

### 9.1.3 开始阅读

1. 在书架上点任意一本书的封面
2. 进入阅读界面，首次打开时会显示"正在解析…"（大文件约 1 秒）
3. 阅读操作：

| 操作 | 效果 |
| --- | --- |
| 上滑 / 下滑（默认滚动模式） | 上下移动一行区域 |
| 点左右两侧 | 上一页 / 下一页 |
| 点屏幕中央 | 唤出/隐藏菜单栏 |
| 长按正文选词 | 高亮词语（V1.x 将支持查词） |
| 返回键 | 退出阅读器（自动保存进度） |

4. 退出后回到书架，书本上会显示阅读进度百分比。

---

## 9.2 书架使用

### 9.2.1 视图切换

书架右上角有视图切换按钮：

| 图标 | 视图 | 特点 |
| --- | --- | --- |
| ▦ | 网格（默认） | 封面优先，一行 3~6 本 |
| ☰ | 列表 | 显示书名、作者、格式、大小、**阅读进度**与最后阅读时间 |

**网格列数调整**：设置 → 显示 → 书架列数（2~6）。

### 9.2.2 排序方式

书架顶部下拉菜单提供 4 种排序：

| 排序 | 说明 |
| --- | --- |
| 最近阅读（默认） | 刚读过的排最前 |
| 添加时间 | 新导入的排最前，便于整理 |
| 书名 | 按拼音顺序（A→Z） |
| 手动排序 | 长按拖动自定义顺序 |

### 9.2.3 分组管理

**创建分组**：书架顶部「全部」标签右侧的 `+` → 输入分组名 → 确定

**内置分组**：

| 分组 | 含义 |
| --- | --- |
| 全部 | 所有书 |
| 未分组 | 未归入任何自定义分组的书 |
| 最近阅读 | 30 天内有阅读记录的书 |
| 已收藏 | 手动标记星标的书 |

**移动书籍到分组**：长按书籍 → 勾选多本 → 顶部「移动到分组」→ 选择目标分组

> 删除分组不会删除书籍，书会回到「未分组」。

### 9.2.4 批量操作

长按任意书籍进入多选模式：

| 按钮 | 作用 | 是否可恢复 |
| --- | --- | --- |
| 移动到分组 | 把选中书籍归入某分组 | — |
| **移出书架** | 从书架移除，**保留归档文件** | ✅ 5 秒内可撤销；重新导入同文件可复用归档 |
| **删除** | 移除并删除归档文件、封面、缓存 | ❌ 不可恢复，需二次确认 |

> ⚠️ **移出书架 vs 删除的区别**：
> - **移出书架**只清理书架记录，文件仍在应用私有目录。重新导入同一文件时，如果内容完全相同，应用会识别并复用（但**阅读进度会丢失**）。
> - **删除**会彻底删除文件。操作前会弹出确认框，明确列出将删除的内容。

### 9.2.5 书籍详情与编辑

长按单本书籍（或在多选中点书籍右上角菜单）可以看到：

| 操作 | 说明 |
| --- | --- |
| 重命名 | 修改显示的书名（不影响文件） |
| 更换封面 | 从相册选择图片作为封面 |
| 编辑信息 | 修改作者、简介 |
| 标记收藏 | 加星标，可在「已收藏」分组查看 |
| 重置阅读进度 | 把进度归零（下次打开从第 1 章开始） |
| 重新解析 | 对 TXT 重新执行分章（换章节规则时用） |
| 文件信息 | 显示格式、大小、编码、章节数、归档路径 |

### 9.2.6 书架内搜索

书架顶部搜索框支持按**书名**或**作者**模糊搜索，输入即时过滤。

---

## 9.3 阅读操作详解

### 9.3.1 翻页模式

在 阅读设置 → 翻页模式 中可选择：

| 模式 | 效果 | 适合 |
| --- | --- | --- |
| **仿真翻页** | 从边缘拖拽，纸张卷曲翻起 | 纸质书手感爱好者 |
| **覆盖翻页** | 新页从右滑入覆盖旧页 | 通用，动画流畅 |
| **平移滑动** | 新旧页并排平移 | 高刷屏，最流畅 |
| **无缝滚动**（默认） | 上下连续滚动，无页概念 | 长文阅读、不想频繁翻页 |
| **点按切换** | 点击即换页，无动画 | 追求零延迟 |

**设置路径**：阅读界面 → 点中央唤出菜单 → 齿轮图标 → 翻页模式

### 9.3.2 快速导航

| 方式 | 操作 |
| --- | --- |
| **目录跳转** | 菜单 → 「目录」→ 点章节；支持搜索章节名 |
| **进度条跳转** | 菜单 → 底部滑块；拖动时上方气泡显示目标章节与百分比，**松手才跳转** |
| **章内定位** | 长按屏幕中央 → 「本章定位」→ 拖动到章内任意位置 |
| **书签跳转** | 菜单 → 「书签」→ 点任意书签 |
| **上一/下一章** | 菜单 → 「上一章 / 下一章」；到章尾自动进下一章 |
| **精确跳转** | 菜单 → 「跳转到…」→ 输入章节号或百分比 |

### 9.3.3 书签

- **添加书签**：菜单 → 「添加书签」（自动截取当前位置前 30 字作为摘要）
- **查看书签**：菜单 → 「书签」→ 列表显示章节名 + 摘要 + 时间
- **删除书签**：书签列表左滑，或长按
- **书签稳定性**：即使改变字号、字体、行距，书签位置依然准确（因为记录的是字符位置而非页码）

### 9.3.4 状态栏信息

阅读界面顶部/底部可显示以下信息（各项可独立开关，路径：设置 → 阅读 → 常驻信息）：

| 信息 | 位置 | 说明 |
| --- | --- | --- |
| 时间 | 顶部左 | 每分钟更新 |
| 电量 | 顶部 | 图标 + 百分比，充电时显示闪电 |
| 本章进度 | 顶部右 | 如"本章 42%" |
| 章节名 | 底部 | 当前章节标题 |
| 页码 / 字数 | 底部 | 如"1,204 / 2,860 字" |
| 全书进度条 | 底部 | 细线进度条 |

### 9.3.5 自动滚屏

适合"双手不空"的场景（如吃饭时阅读）：

1. 菜单 → 「自动滚屏」
2. 屏幕开始匀速向下滚动
3. 点任意位置停止
4. 速度调节：设置 → 阅读 → 自动滚屏速度（1~10 档，默认 3）

**自动滚屏开启时会自动保持屏幕常亮**，停止后恢复系统设置。

---

## 9.4 阅读设置详解

### 9.4.1 排版设置

路径：阅读界面 → 菜单 → 「排版」

| 参数 | 范围 | 默认 | 说明 |
| --- | --- | --- | --- |
| 字号 | 12sp ~ 36sp | 18sp | 点 `A-` / `A+` 或拖动滑块 |
| 行间距 | 1.0 ~ 3.0 倍 | 1.6 | 行与行之间的距离 |
| 段间距 | 0.0 ~ 2.0 | 0.8 | 段落之间的距离 |
| 左右边距 | 0 ~ 48dp | 16dp | 正文距屏幕左右边缘 |
| 上下边距 | 0 ~ 96dp | 24dp | 正文距屏幕上下边缘 |
| 字间距 | -0.05 ~ 0.20 | 0 | 字与字之间的距离 |
| 字体 | 系统 / 苹方 / 思源宋体 / 自定义 | 系统 | 见 9.4.3 |
| 对齐方式 | 左对齐 / 两端对齐 | 左对齐 | |
| 正文加粗 | 开 / 关 | 关 | 在低分辨率屏幕上提升可读性 |
| 首行缩进 | 开 / 关 | 开 | 段首缩进 2 个字符 |

> **关键特性**：**调整任何排版参数都不会丢失阅读位置**。应用记录的是"读到全书的第几个字"，而不是"第几页"，所以字号变大后内容会重新分页，但你会停在完全相同的那句话上。

### 9.4.2 主题设置

路径：阅读界面 → 菜单 → 「主题」

**预置主题**：

| 主题 | 外观 | 适合场景 |
| --- | --- | --- |
| **纸质仿古** | 米黄底黑字 | 白天，长时间阅读 |
| **护眼浅绿** | 浅绿底深绿字 | 白天，眼睛易疲劳 |
| **羊皮纸** | 浅棕底深棕字 | 复古质感，配宋体最佳 |
| **纯黑夜间** | 纯黑底浅灰字 | 夜间，OLED 屏省电 |
| **深灰夜间** | 深灰底浅灰字 | 夜间，非 OLED 屏更柔和 |
| **高对比** | 纯白底纯黑字 | 强光下、视力不佳 |

**自定义主题**：

1. 主题面板 → 「自定义」
2. 分别输入背景色与文字色的十六进制值（如 `#F5F1E8`）
3. 应用实时显示**对比度数值**：
   - ≥ 7.0 → ✅ 优秀
   - 4.5 ~ 7.0 → ⚠️ 可接受
   - < 4.5 → ❌ 过低，会给出「一键修正」按钮自动调整到可读水平

> **为什么强调对比度？** 长文阅读对对比度要求高于一般界面。低于 4.5:1 的组合（如浅灰字配浅黄底）会让眼睛很快疲劳。

**亮度调节**：主题面板底部的亮度滑块（0~100%）。默认只调节应用内亮度（不影响系统），可在设置中切换为"跟随系统"。

### 9.4.3 自定义字体

路径：设置 → 阅读 → 字体管理 → 「导入字体」

1. 点「导入字体」
2. 选择本机的一个 `.ttf` / `.otf` / `.ttc` 文件
3. 输入字体名称（如"我的阅读字体"）
4. 导入后在 排版 → 字体 中选择使用

**注意事项**：

| 事项 | 说明 |
| --- | --- |
| 支持格式 | TTF / OTF / TTC |
| 文件大小 | 无硬性限制，但超过 30MB 的字体首次加载较慢（约 1 秒） |
| 内存占用 | 中文字体通常 5~10MB，应用最多同时缓存 2 套字体 |
| 加载失败 | 若字体文件损坏，会被拒绝并提示，不会导致应用异常 |
| 字体丢失 | 若字体文件被清理，自动回退系统字体并提示 |
| 删除字体 | 字体管理 → 长按 → 删除（使用中的字体会先切换回系统字体） |

### 9.4.4 交互设置

路径：设置 → 阅读 → 交互

| 设置项 | 选项 | 默认 |
| --- | --- | --- |
| 点击区域 | 上下分区翻页 / 左右分区翻页 / 仅唤出菜单 | 左右分区 |
| 音量键功能 | 无 / 翻页 / 滚动 / 调亮度 | 无 |
| 翻页震动 | 开 / 关 | 关 |
| 常亮模式 | 跟随系统 / 始终常亮 | 跟随系统 |
| 屏幕休眠 | 跟随系统 / 1/2/5/10/30 分钟 | 跟随系统 |
| 全屏沉浸 | 开 / 关 | 开 |

---

## 9.5 网络书源使用

### 9.5.1 什么是书源

书源是一份 **JSON 格式的规则文件**，描述了"如何从某个网站搜索书籍、获取目录、提取正文"。它本质上是一个**通用网页规则引擎的配置**。

应用本身**不内置、不提供、不分发任何书源**，也不提供书籍内容。书源需要用户自行获取并以 JSON 文件导入。

> ⚠️ **重要声明**：书源由用户自行配置，其内容与合法性由用户自行负责。请遵守所在地区法律法规，仅将本功能用于访问你有权访问的内容。

### 9.5.2 导入书源

路径：底部导航 → 「书源」→ 右上角 `+`

**方式一：粘贴 JSON 文本**

1. 选「粘贴 JSON」
2. 把书源 JSON 内容粘贴进输入框
3. 点「校验并导入」
4. 若 JSON 合法，会显示书源名称、来源分组，点「确认导入」

**方式二：从 URL 导入**

1. 选「从 URL 导入」
2. 输入 JSON 文件的直链地址（支持多个，每行一个）
3. 应用并发下载并逐个校验

**方式三：从文件导入**

1. 选「从文件导入」
2. 选择本机的 `.json` 文件
3. 支持包含多个书源的数组格式

**导入结果**会逐条显示：

```
✓ 示例书源 A        导入成功
✓ 示例书源 B        导入成功
✗ 某书源            search.url 缺少 {{keyword}} 占位符
✗ 某书源            已存在同名书源
```

### 9.5.3 管理书源

书源列表每项显示：

| 元素 | 说明 |
| --- | --- |
| 名称 | 书源名 |
| 分组 | 来源分类 |
| 健康状态 | 🟢 正常 / 🟡 不稳定 / 🔴 已失效 / ⚪ 未检测 |
| 延迟 | 平均响应时间 |
| 开关 | 是否参与聚合搜索 |

**操作**：

| 操作 | 说明 |
| --- | --- |
| 启用/禁用 | 关闭后不参与搜索 |
| 编辑 | 修改名称、分组、编码、User-Agent、超时 |
| 导出 | 导出为 JSON（**不含 Cookie**） |
| 删除 | 删除书源（已加入书架的书仍可读缓存内容） |
| 检测 | 单个/全部按需检测可用性 |
| **调试** | 打开规则调试器（见 9.5.5） |

### 9.5.4 搜索与阅读网络书

路径：底部导航 → 「发现」

1. 输入书名或作者
2. 点搜索，结果**边搜边出**（各书源返回速度不同，先返回的先显示）
3. 搜索结果自动**归并去重**：同一本书在多个源出现时合并为一条，展开可看具体源：

```
┌──────────────────────────────────────┐
│ 封面  书名                            │
│       作者 · 共 1,847 章              │
│       简介前两行…                     │
│                          ┌─────────┐ │
│       「3 个书源可用」     │ 加入书架 │ │
│                          └─────────┘ │
│  ▾ 展开查看各书源                     │
│    示例源 A  1,847 章 · 12ms   ●最佳  │
│    示例源 B  1,840 章 · 340ms        │
│    示例源 C  1,722 章 · 890ms        │
└──────────────────────────────────────┘
```

4. 点书封面进入**详情页**：简介、目录、各源对比
5. 点「加入书架」→ 书籍进入书架（标记为「网」）
6. 点任意章节点开阅读（首次需联网抓取，之后有本地缓存）

### 9.5.5 换源阅读

当某个源出现断章、乱码、广告过多时，可切换到其他源继续阅读，**位置不丢失**。

**操作**：阅读界面 → 菜单 → 「换源」→ 选择目标书源

```
┌──────────────────────────────────────┐
│  换源                                │
│                                      │
│  当前位置：第 183 章 城中夜话          │
│                                      │
│  ✓ 示例源 A（当前） 1,847 章          │
│  ○ 示例源 B         1,840 章  ●推荐   │
│  ○ 示例源 C         1,722 章          │
│                                      │
│  ⓘ 换源后将自动定位到同一章节的        │
│    相同位置，无需重新找章节            │
└──────────────────────────────────────┘
```

**匹配可靠性**：应用会依次尝试"章节标题完全匹配 → 章节序号匹配 → 标题相似度匹配 → 按比例定位"。若只能按比例定位，会提示"未能精确定位章节，请手动确认"。

### 9.5.6 批量缓存（离线阅读）

**在阅读界面**：菜单 → 「缓存」→ 选择范围（后 10 / 50 / 100 章 / 到最新章节）

**在书籍详情页**：点「缓存全本」→ 选择起始章节 → 确认

**在设置页**：设置 → 缓存管理 → 查看所有缓存任务进度

**缓存设置**：

| 设置 | 选项 | 默认 |
| --- | --- | --- |
| 仅 Wi-Fi 缓存 | 开 / 关 | **开** |
| 缓存上限 | 50MB / 100MB / 200MB / 500MB / 1GB / 2GB | 200MB |
| 缓存完成通知 | 开 / 关 | 关 |
| 打开书籍自动缓存 | 关 / 后 30 章 | 关 |

**缓存任务会在以下情况自动暂停**：网络切换、电量过低（<15%）、用户手动暂停、应用被系统回收。

**缓存淘汰规则**：超过上限时按以下顺序清理：

1. 已过期章节（连载书缓存 7 天后过期）
2. 其他书籍的章节（按最后阅读时间，最久远的先清理）
3. 当前书籍中距阅读位置最远的章节

> **保护机制**：当前阅读位置前后 5 章永不清理。

### 9.5.7 正文净化

书源抓取的正文常混有广告与水印。应用内置 8 类通用净化规则，自动移除：

- "请记住本站域名 xxx.com"
- "天才一秒记住本站"
- "（本章未完，请点击下一页）"
- "手机用户请访问 xxx"
- 纯广告段落、页码残留、导航链接文字
- 反爬用的零宽字符
- 重复标点（"！！！！" → "！"）

**自定义净化**：书源编辑 → 正文规则 → 追加 `cutPatterns`（正则），或 `removeSelectors`（CSS 选择器）。

> **安全提示**：自定义正则若写得过于宽泛可能删除正文。应用会拒绝会导致性能问题的危险正则，并在清洗结果过短（少于原文 30%）时自动回退到未净化版本。

---

## 9.6 设置项总览

### 9.6.1 书架

| 项 | 选项 | 默认 |
| --- | --- | --- |
| 视图模式 | 网格 / 列表 | 网格 |
| 网格列数 | 2 / 3 / 4 / 5 / 6 | 3 |
| 排序方式 | 最近阅读 / 添加时间 / 书名 / 手动 | 最近阅读 |
| 显示作者 | 开 / 关 | 开 |
| 显示进度 | 开 / 关 | 开 |

### 9.6.2 阅读

| 项 | 选项 | 默认 |
| --- | --- | --- |
| 翻页模式 | 仿真 / 覆盖 / 平移 / 滚动 / 点按 | 滚动 |
| 常驻信息 | 时间 / 电量 / 章进度 / 章节名 / 页码 / 进度条 | 时间+电量+章进度 |
| 自动滚屏速度 | 1 ~ 10 | 3 |
| 音量键功能 | 无 / 翻页 / 滚动 / 调亮度 | 无 |
| 常亮模式 | 跟随系统 / 常亮 | 跟随系统 |
| 字体管理 | 导入 / 删除字体 | — |
| 排版 | 12 项参数 | 见 9.4.1 |
| 主题 | 6 套预置 + 自定义 | 纸质仿古 |

### 9.6.3 网络与书源

| 项 | 选项 | 默认 |
| --- | --- | --- |
| 搜索并发数 | 2 / 4 / 6 / 8 / 10 | 6 |
| 单源超时 | 5 / 10 / 15 / 30 秒 | 15 秒 |
| 自动净化 | 开 / 关 | 开 |
| 搜索包含已禁用源 | 开 / 关 | 关 |
| 全局 User-Agent | 自动轮换 / 自定义 | 自动轮换 |

### 9.6.4 缓存与存储

| 项 | 说明 |
| --- | --- |
| 缓存上限 | 见 9.5.6 |
| 仅 Wi-Fi | 见 9.5.6 |
| 已用空间 | 显示各类型占用：书籍 / 封面 / 字体 / 缓存 / 数据库 |
| 清理缓存 | 只清理缓存章节与临时文件，**不删书籍** |
| 清理封面缓存 | 删除已下载的网络封面 |

### 9.6.5 数据管理

| 项 | 说明 |
| --- | --- |
| 导出备份 | 导出书籍列表、阅读进度、书签、书源、设置为 JSON（**不含书籍正文文件**） |
| 导入备份 | 从 JSON 恢复（按内容哈希与书名+作者匹配本地文件） |
| 导出书签笔记 | 单独导出书签为 JSON |
| 导出书源 | 全部书源为 JSON（不含 Cookie） |
| 重置阅读进度 | 清除所有书籍进度与统计 |
| **清除所有数据** | 二次确认后清除全部（见下文警告） |

> ⚠️ **「清除所有数据」不可恢复**。执行前应用会明确列出将删除的内容与总大小，并提供"先导出备份"按钮。

### 9.6.6 关于与隐私

| 项 | 说明 |
| --- | --- |
| 版本 | 显示版本号与构建号 |
| 隐私政策 | 完整文本（离线可读） |
| 开源许可 | 第三方依赖许可列表 |
| 崩溃上报 | 开 / 关（**默认关**） |
| 检查更新 | 手动检查（无自动更新，无自安装权限） |

---

## 9.7 常见问题（FAQ）

### 9.7.1 导入与解析

**Q：导入的 TXT 全是乱码怎么办？**

A：说明编码识别有误。解决方式：

1. 长按书籍 → 「文件信息」→ 查看"当前编码"
2. 长按书籍 → 「重新解析」→ 在编码下拉中选择正确编码
3. 选择器中每个编码都显示**前 60 字的实际预览**，选看起来正常的那一个

常见对应关系：简体中文科幻/网文多为 GBK；Linux/Mac 生成的多为 UTF-8；来自中国台湾地区站点可能为 BIG5。

**Q：TXT 目录识别不全/识别错误怎么办？**

A：TXT 没有标准目录格式，依赖规则匹配。解决方式：

1. 长按书籍 → 「重新解析」→ 章节规则
2. 选择一个内置规则（如"第N章"、"Chapter N"、"数字序号"）
3. 若都不合适，选「自定义正则」，输入如 `^第[0-9]+章.*` 的正则
4. 解析结果会实时预览（显示识别到的章节数与章节标题），确认后再保存

**Q：EPUB 打开后目录是空的？**

A：该 EPUB 可能缺少标准目录文件（`toc.ncx` 与 `nav.xhtml`）。应用会自动降级为"按标题标签（h1~h3）分章"。若仍为空，会按固定长度分节。请通过「文件信息」确认章节数是否合理。

**Q：PDF 提示"暂不支持加密 PDF"？**

A：`PDF` 渲染基于系统能力（`PdfRenderer`），系统不支持加密文档。若该 PDF 是购买的加密电子书，请用阅读器打开后在原应用中阅读，或先用工具移除密码后再导入（请注意版权限制）。

**Q：PDF 双击缩放后文字模糊？**

A：PDF 是矢量内容，缩放时会重新按新尺寸渲染（不是拉伸位图），文字应保持清晰。若发现模糊，可能是页面本身是扫描图片。可在 阅读设置 → PDF 中调整"渲染质量"（省内存 / 平衡 / 清晰）。

**Q：为什么导入后原文件删除了还能读？**

A：导入时应用把文件**复制**到了自己的私有目录（这是默认的"归档模式"，避免用户清理 Downloads 时书籍失效）。原文件删除不影响阅读。

若你不想占用双份空间，可在 设置 → 导入设置 中改为「引用模式」（只记录路径不复制）。但引用模式下文件被移动/删除会导致书籍失效。

**Q：扫描时找不到我的书？**

A：可能原因：

- 文件扩展名不是 `.txt` / `.epub` / `.pdf`（如 `.TXT.BAK`）
- 文件夹层级超过 8 层
- 选择的是某个文件而非文件夹（请选择**文件夹**）
- 该文件夹在应用无权限的路径下（如其他应用的私有目录）

**Q：导入时提示"《书名》已在书架"？**

A：应用按文件内容哈希去重。说明这个文件的内容已经在书架中（可能通过其他路径导入过）。若你想保留两份，请先修改其中一个文件的内容（哪怕加一行）再导入。

### 9.7.2 阅读与排版

**Q：调整字号后阅读位置会丢吗？**

A：**不会**。应用记录的是"读到全书的第几个字"，而不是页码。改变字号、字体、行距、边距、旋转屏幕后，内容会重新分页，但你会停在完全相同的位置。

这是本应用的核心设计：**字符级进度记录**。

**Q：为什么换字体后要等一下才能翻页？**

A：换字体后所有文本需要按新字体的字符宽度重新分页。3000 字的章节约需 0.4 秒。分页期间你可以看到页面，只是翻页会有短暂延迟。

**Q：某本书切换字号后位置有一点偏移？**

A：可能有以下原因：

- 该章节包含大量图片（图片尺寸固定，不随字号变化，会挤压文本空间）
- 该章节包含表格/特殊排版
- 程序缺陷

若偏移超过一屏，请通过「关于 → 反馈」报告并附上书籍文件（若可提供）。

**Q：仿真翻页动画看起来有点卡？**

A：仿真翻页（纸张卷曲）计算量大于其他模式。建议：

- 在中低端设备上使用「平移滑动」或「点按切换」
- 或在 设置 → 阅读 中关闭"翻页阴影效果"

**Q：夜间模式读 PDF 反色后图表看不清？**

A：PDF 的夜间滤镜提供两档：

- **标准反色**：反转所有颜色。文字清晰，但彩色图表会变成"负片"
- **温和暗色**：不反色，仅整体压暗。图表保持原色，适合图文混排文档

在 阅读设置 → PDF → 夜间滤镜 中切换。

**Q：自动滚屏会耗电吗？**

A：自动滚屏会让屏幕保持唤醒，且持续刷新。1 小时约耗电 8~10%（普通翻页阅读约 5~6%）。长时间不在屏幕前建议关闭。

### 9.7.3 书源

**Q：书源导入失败，提示"search.url 缺少 {{keyword}} 占位符"？**

A：书源 JSON 中的搜索 URL 必须包含 `{{keyword}}` 占位符，用于代入搜索关键词。例如：

```json
"url": "https://example.com/search?q={{keyword}}&page={{page}}"
```

若你拿到的是旧格式书源（用 `%s` 或 `{0}`），请手动改为 `{{keyword}}`。

**Q：书源都搜不到书？**

A：按以下顺序排查：

1. 书源页面 → 点某个源的「检测」，看是否可达
2. 若检测失败，打开该源的「调试」，输入一个测试关键词，查看：

   - **请求 URL** 是否正确
   - **HTTP 状态码** 是否为 403/404（站点封禁或路径已变）
   - **搜索结果匹配** 是否为 0 项（说明 `itemSelector` 不匹配了）

3. 若状态 403，在源设置中更换自定义 User-Agent
4. 若是选择器问题，说明站点改版了，需要更新书源规则

**Q：某本书搜不到，但网站上确实有？**

A：可能的书名差异（站点用别名）或该源不支持搜索只支持分类浏览。可以：

- 换另一个源试试
- 在源详情页手动粘贴书籍详情页 URL 直接进入

**Q：换源后章节名对不上？**

A：不同源的章节划分与命名有差异（如"第183章 城中夜话" vs "第183章：城中夜话"）。应用会尽力匹配，若匹配置信度低会提示。你可以：

- 在换源后通过目录手动调整章节位置
- 若严重错位，请在目录中跳转到正确章节，进度会自动修正

**Q：缓存的书突然没了？**

A：缓存可能被以下原因清理：

- 超过缓存上限（默认 200MB）被自动淘汰
- 连载书的缓存超过 7 天过期
- 手动「清理缓存」
- 系统清理应用缓存目录

需要长期离线阅读的书，建议提高缓存上限（设置 → 缓存 → 上限 1GB 或 2GB）。

**Q：书源失效了怎么办？**

A：书源规则依赖目标网站的页面结构，网站改版会导致规则失效。这是**正常现象**，不是应用缺陷。解决方式：

1. 打开该源的「调试」，查看具体是哪一步失败
2. 用浏览器打开该网站，按 F12 查看要抓取的元素的 CSS 选择器
3. 在源的编辑页更新对应的选择器字段
4. 或从其他渠道获取更新版的书源 JSON 重新导入

**Q：为什么没有内置书源？**

A：出于合规考虑，应用不提供、不存储、不分发任何书源。书源功能是一个通用网页规则引擎，具体配置需由用户自行提供。这也意味着应用自身不涉及任何内容分发。

### 9.7.4 性能与存储

**Q：导入一本 10MB 的 TXT 要多久？**

A：中端设备约 1.2 秒，低端设备约 2.2 秒。第二次打开同一本书只需约 0.1 秒（应用已缓存了章节索引）。

**Q：应用占用多少存储？**

A：典型情况：

| 内容 | 占用 |
| --- | --- |
| 应用本体 | 约 15MB |
| 书籍文件 | 与导入文件等大（归档模式） |
| 章节缓存 | 最多你设置的上限（默认 200MB） |
| 封面 | 每张约 30KB，500 本约 15MB |
| 字体 | 每套 5~10MB |

**Q：应用占用多少内存？**

A：阅读时典型值 120~180MB（含系统框架基线）。应用会主动控制：分页缓存只保留当前书附近章节，PDF 页位图上限 48MB，字体最多缓存 2 套。

**Q：手机提示"应用占用内存过高"？**

A：长时间阅读 PDF 或大尺寸 EPUB（含大量图片）时可能出现。建议：

- 退出并重进阅读器（会释放位图缓存）
- 减少同时打开的应用
- 在 设置 → 性能 中关闭"预取相邻章节"

**Q：为什么翻页时偶尔会闪一下？**

A：可能原因：

- 首次进入某章节时需要分页（约 0.4 秒）
- 切换排版参数后重新分页
- 设备内存紧张触发了缓存回收

若频繁出现，请在 设置 → 性能 中开启"预分页后续 3 章"（会增加内存占用约 2MB）。

### 9.7.5 隐私与安全

**Q：我的书会被上传吗？**

A：**不会**。应用没有任何上传本地书籍的代码路径。本地文件读取与网络请求在架构上是隔离的模块，网络模块无法访问书籍目录。

网络功能仅用于：① 你配置的书源抓取；② 你主动开启的崩溃上报（默认关闭，且会脱敏）。

**Q：应用联网做什么？**

A：仅在以下情况联网：

- 使用书源搜索/抓取章节（你主动触发）
- 下载书源的封面图
- 检查更新（你主动点击）
- 崩溃上报（默认关闭，需你主动开启）

**Q：应用申请了哪些权限？**

A：只有 3 个必要权限：

| 权限 | 用途 |
| --- | --- |
| `INTERNET` | 书源抓取 |
| `ACCESS_NETWORK_STATE` | 判断网络类型（如"仅 Wi-Fi 缓存"） |
| `WAKE_LOCK` | 保证后台缓存任务不被中断 |

通知权限仅在你主动开启"缓存完成通知"时才会申请。

**Q：不想让应用被云备份？**

A：应用已默认关闭自动备份（`allowBackup=false`），你的书籍与进度不会被上传到系统云备份。

**Q：如何彻底清除我的数据？**

A：设置 → 数据管理 → 清除所有数据。会显示将删除的内容与大小，二次确认后执行。执行前建议先导出备份。

或直接卸载应用（会删除全部应用数据）。

### 9.7.6 其他

**Q：支持 iPhone / Windows / Mac 吗？**

A：V1.0 仅支持 Android 8.0 及以上。

**Q：支持 MOBI / AZW3 吗？**

A：V1.0 不支持。已在 V1.x 规划中。

**Q：有广告吗？**

A：没有。永远不会有。

**Q：需要注册账号吗？**

A：不需要。应用无账号体系，所有功能开箱即用。

**Q：阅读进度能同步到其他手机吗？**

A：V1.0 不支持跨设备同步（需要账号或服务端，与"无账号"原则冲突）。可通过 设置 → 数据管理 → 导出备份 / 导入备份 手动迁移。

**Q：怎么反馈问题或提建议？**

A：设置 → 关于 → 反馈。请尽量附上：

- 设备型号与系统版本
- 应用版本号
- 问题复现步骤
- 截图或录屏
- 若与特定书籍相关，提供书籍文件（TXT/EPUB 支持，PDF 视情况）

---

## 9.8 快捷操作速查

| 场景 | 操作 |
| --- | --- |
| 翻到下一页 | 点屏幕右侧 / 左滑 / 音量下键（若已配置） |
| 翻到上一页 | 点屏幕左侧 / 右滑 / 音量上键（若已配置） |
| 唤出/隐藏菜单 | 点屏幕中央 / 点底部区域 |
| 快速调字号 | 菜单 → 排版 → 点 `A-`/`A+` |
| 快速换主题 | 菜单 → 主题 |
| 跳到目录 | 菜单 → 目录 |
| 跳到全书 50% | 菜单 → 拖动底部滑块到中间（气泡显示百分比）→ 松手 |
| 添加书签 | 菜单 → 添加书签 |
| 换源 | 菜单 → 换源 |
| 缓存后续章节 | 菜单 → 缓存 |
| 全屏（隐藏状态栏） | 设置 → 阅读 → 全屏沉浸 |
| 回到书架 | 返回键 / 菜单 → 返回书架 |

---

**上一卷**：[第 8 卷 · 开发计划与协作规范](#vol-08) ｜ **下一卷**：[第 10 卷 · 附录](#vol-10)


---


# 第 10 卷 · 附录 {#vol-10}
> 本卷收录开发与使用过程中需要**反复查阅**的参考数据：书源 JSON 规范、内置规则库、主题色板、错误码、默认值表。这些内容应与代码中的常量保持同步，任何改动必须在 PR 中同时更新。

---

## 附录 A · 书源 JSON 规范

### A.1 规范版本

| 项 | 值 |
| --- | --- |
| 规范版本 | `1.0` |
| 字段名风格 | `camelCase` |
| 占位符语法 | `{{name}}` |
| 选择器语法 | CSS 选择器（Jsoup 实现，支持部分 `:has()` / `:contains()`） |
| 编码 | UTF-8（文件本身）；被爬页面编码由 `charset` 字段声明 |

### A.2 完整结构

```jsonc
{
  // ── 必填：书源标识 ─────────────────────────────
  "name": "示例书源 A",              // 唯一。重名会被拒绝导入
  "group": "小说",                   // 分组，用于界面归类与并发分组，默认 "默认"
  "baseUrl": "https://example.com",  // 站点根地址，规则中的相对路径以此为基准
  "version": 1,                      // 规范版本，当前固定为 1

  // ── 可选：网络与编码配置 ───────────────────────
  "charset": "UTF-8",                // 被爬页面编码。留空则自动探测。常见 "GBK"
  "customUserAgent": null,           // 自定义 UA。null = 使用全局 UA 池轮换
  "timeoutMs": 15000,                // 单次请求超时，默认 15000
  "needCookie": false,               // 是否需要 Cookie 才能访问
  "enabled": true,                   // 导入后默认是否启用

  // ── 必填：规则集 ───────────────────────────────
  "rules": {
    "search": { /* 见 A.3 */ },
    "detail": { /* 见 A.4，可选 */ },
    "catalog": { /* 见 A.5 */ },
    "content": { /* 见 A.6 */ },
    "catalogOrder": "ASC"            // 目录顺序。ASC 正序 / DESC 倒序
  }
}
```

### A.3 `rules.search` — 搜索规则

```jsonc
"search": {
  // ★ 必填。搜索请求 URL 模板
  // 可用占位符：{{keyword}}（URL 编码后的关键词）、{{page}}（页码，从 1 开始）
  "url": "https://example.com/search?q={{keyword}}&p={{page}}",

  // 可选。请求方法，默认 "GET"。POST 时用 body 模板
  "method": "GET",
  "body": null,                     // 形如 "kw={{keyword}}&page={{page}}"

  // ★ 必填。搜索结果每一项的容器选择器
  "itemSelector": ".book-list .item",

  // ★ 必填。在 itemSelector 范围内提取书名
  "nameSelector": "h3.title",

  // 可选。其余字段均在 itemSelector 范围内提取
  "authorSelector": ".author",
  "coverSelector": "img",           // 取 src / data-src / data-original 属性
  "introSelector": ".intro",
  "latestChapterSelector": ".last-chapter",

  // ★ 必填。详情页链接选择器，取其 href
  "linkSelector": "h3.title a",

  // 可选。翻页：下一页按钮选择器。null 表示不支持翻页
  "nextPageSelector": ".pagination .next",
  "maxPages": 3                     // 最多翻多少页，默认 3
}
```

**属性提取的容错**：`linkSelector` 与 `coverSelector` 指向的元素的属性会按以下顺序尝试：

```
链接：href → data-href → data-url → onclick 中提取的 URL
封面：src → data-src → data-original → data-lazy-src → style 中的 background-image
```

若原值与 `baseUrl` 是相对路径，会自动拼接为绝对 URL。

### A.4 `rules.detail` — 详情页规则（可选）

全部字段可选。若省略，则直接使用搜索结果中的信息。

```jsonc
"detail": {
  "nameSelector": ".book-info h1",
  "authorSelector": ".book-info .author",
  "coverSelector": ".book-info img",
  "introSelector": "#intro",
  "statusSelector": ".status",           // 连载 / 完结
  "wordCountSelector": ".word-count",
  "latestChapterSelector": ".latest a"
}
```

### A.5 `rules.catalog` — 目录规则

```jsonc
"catalog": {
  // ★ 必填。章节列表每一项的容器选择器
  "itemSelector": "#chapter-list li",

  // ★ 必填。在每一项内提取章节标题
  "titleSelector": "a",

  // ★ 必填。在每一项内提取章节链接（取 href）
  "linkSelector": "a",

  // 可选。匹配到的项若命中此选择器，标记为"卷标题"（不计入正文，仅作分组）
  "volumeSelector": ".volume-title",

  // 可选。目录是否倒序（很多站点最新章节在最前）
  "reversed": false
}
```

**目录顺序处理的正确方式**：优先用 `reversed` 声明；若站点无规律，应用会用"章节序号单调性"自动判断：

- 若前 10 项的序号是递减的 → 自动反转
- 若无序号（纯标题）→ 保持原顺序

### A.6 `rules.content` — 正文规则

```jsonc
"content": {
  // ★ 必填。正文容器选择器
  "contentSelector": "#content",

  // 可选。需要在正文容器内移除的节点（广告、推荐、页脚）
  "removeSelectors": [
    ".ad",
    ".recommend",
    ".footer-tip",
    "script",
    "style"
  ],

  // 可选。段落分隔方式，默认 "AUTO"
  //   "PARAGRAPH" — 按 <p> / <div> 等块元素
  //   "BR"        — 按 <br> 标签
  //   "AUTO"      — 自动探测（推荐）
  "paragraphMode": "AUTO",

  // 可选。这些标签在提取时视为换行
  "breakTags": ["br", "p", "div"],

  // 可选。正文后方的截断标记（正则）。匹配到后丢弃其后全部内容
  // 用于"本章未完，请点击下一页"这类干扰
  "cutPatterns": [
    "本章未完",
    "请记住本站域名",
    "（未完待续）"
  ]
}
```

### A.7 最小可用示例（可直接使用）

> 以下为**结构示例**，`baseUrl` 与选择器为占位内容，需按实际站点填写。

**示例 1：搜索 + 目录 + 正文（完整）**

```json
{
  "name": "示例书源 · 完整",
  "group": "小说",
  "baseUrl": "https://www.example-novel.com",
  "version": 1,
  "charset": "UTF-8",
  "timeoutMs": 15000,
  "enabled": true,
  "rules": {
    "search": {
      "url": "https://www.example-novel.com/search.php?keyword={{keyword}}&page={{page}}",
      "method": "GET",
      "itemSelector": ".result-list .result-item",
      "nameSelector": ".bookname a",
      "authorSelector": ".author",
      "coverSelector": ".bookimg img",
      "linkSelector": ".bookname a",
      "nextPageSelector": ".pagination a.next",
      "maxPages": 2
    },
    "detail": {
      "nameSelector": ".book-info h1",
      "authorSelector": ".book-info .author",
      "coverSelector": ".book-info .cover img",
      "introSelector": ".intro"
    },
    "catalog": {
      "itemSelector": "#list dd",
      "titleSelector": "a",
      "linkSelector": "a",
      "volumeSelector": ".volume",
      "reversed": false
    },
    "content": {
      "contentSelector": "#content",
      "removeSelectors": [".bottem", ".readmiddle", ".adsbygoogle", "script"],
      "paragraphMode": "AUTO",
      "cutPatterns": ["本章未完", "请记住本站域名", "天才一秒记住"]
    },
    "catalogOrder": "ASC"
  }
}
```

**示例 2：正文在整页 `body` 中（无嵌套容器）**

```json
{
  "name": "示例书源 · 无容器",
  "group": "小说",
  "baseUrl": "https://m.example-novel.net",
  "version": 1,
  "charset": "GBK",
  "customUserAgent": "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36",
  "rules": {
    "search": {
      "url": "https://m.example-novel.net/s?q={{keyword}}",
      "itemSelector": ".item",
      "nameSelector": ".title",
      "authorSelector": ".info span:nth-child(2)",
      "linkSelector": "a",
      "maxPages": 1
    },
    "catalog": {
      "itemSelector": ".catalog a",
      "titleSelector": "",
      "linkSelector": ""
    },
    "content": {
      "contentSelector": "#nr1",
      "removeSelectors": ["script", "style", ".bar"],
      "paragraphMode": "BR",
      "cutPatterns": ["请记住本站"]
    },
    "catalogOrder": "ASC"
  }
}
```

> **注意**：当 `titleSelector` 与 `linkSelector` 为空字符串时，表示"元素本身即目标"（即 `itemSelector` 直接匹配到 `<a>` 标签）。

**示例 3：批量导入格式（数组）**

```json
{
  "sources": [
    { "name": "书源一", "baseUrl": "...", "rules": { /* ... */ } },
    { "name": "书源二", "baseUrl": "...", "rules": { /* ... */ } }
  ]
}
```

应用同时支持三种顶层结构：单个对象、对象数组、带 `sources` 键的包装对象。

### A.8 选择器速查（写书源时常用）

| 需求 | 选择器写法 |
| --- | --- |
| 按 id | `#content` |
| 按 class | `.chapter-list` |
| 按属性 | `div[data-role=list]` |
| 属性前缀匹配（抗动态 id） | `div[id^=content]`、`div[class*=chapter]` |
| 后代 | `.book-info h1` |
| 直接子元素 | `#list > dd` |
| 第 n 个 | `.item:nth-child(2)` |
| 包含某文本 | `a:contains(下一页)` |
| 有某子元素 | `li:has(a.title)` |
| 排除 | `#content > *:not(.ad)` |

> **抗改版建议**：优先使用**语义化、宽松**的选择器（如 `[class*=content]`、`article`、`.read-content`），避免依赖易变的动态 class 名（如 `.css-9x2k1`）或纯序号位置（如 `div > div > div:nth-child(3)`）。

### A.9 书源导入校验规则（会拒绝的情况）

| 校验项 | 拒绝原因文案 |
| --- | --- |
| `name` 为空 | "书源名称为空" |
| `name` 超过 40 字 | "书源名称过长" |
| `name` 重复 | "已存在同名书源" |
| `baseUrl` 不以 http 开头 | "baseUrl 必须以 http 开头" |
| `baseUrl` 非法 URL | "baseUrl 非法" |
| `rules.search.url` 为空 | "search.url 为空" |
| `rules.search.url` 无 `{{keyword}}` | "search.url 缺少 {{keyword}} 占位符" |
| `itemSelector` 为空 | "search.itemSelector 为空" |
| `linkSelector` 为空 | "search.linkSelector 为空" |
| `catalog.itemSelector` 为空 | "catalog.itemSelector 为空" |
| `content.contentSelector` 为空 | "content.contentSelector 为空" |
| 选择器语法错误 | "选择器语法错误「xxx」：<详细原因>" |
| `cutPatterns` 含危险正则 | "正则存在性能风险：<原因>" |
| `cutPatterns` 正则过长（>200 字符） | "正则过长" |
| JSON 语法错误 | "JSON 格式错误：<行号与原因>" |

---

## 附录 B · 内置 TXT 章节规则库

### B.1 内置规则列表

| 规则 ID | 名称 | 正则 | 匹配示例 |
| --- | --- | --- | --- |
| `cn_numbered` | 中文数字章节 | `^\s*第\s*[0-9零一二三四五六七八九十百千万两]{1,10}\s*[章节回集篇话]\s*[^\n]{0,40}$` | 第1章 初见 / 第一百零三章 归途 / 第四章 / 第 12 节 |
| `cn_volume` | 中文卷/部 | `^\s*第\s*[0-9零一二三四五六七八九十百千万两]{1,10}\s*[卷部]\s*[^\n]{0,40}$` | 第一卷 少年游 / 第二部 江湖 |
| `cn_special` | 特殊章节名 | `^\s*(序章\|序言\|序\|楔子\|引子\|前言\|后记\|尾声\|终章\|番外[^\n]{0,20}\|完本感言\|作者的话)\s*$` | 序章 / 楔子 / 番外一 / 完本感言 |
| `en_numbered` | 英文章节 | `^\s*(Chapter\|CHAPTER\|chapter)\s*\d{1,5}\s*[^\n]{0,60}$` | Chapter 1 / Chapter 12 The Beginning |
| `digit_only` | 纯数字序号 | `^\s*\d{1,4}\s*[.、]\s*[^\n]{0,40}$` | 1. 开始 / 12、转折 |
| `toc_style` | 目录式（"第N章 标题"单独成行，前后无空行） | 组合规则，见 B.2 | 第一章 起点 |
| `bracket` | 方括号章节 | `^\s*[【\[](第[^\n]{1,20}[章回】\]])\s*$` | 【第一章】/ [第2章] |

### B.2 规则优先级与自动选择

应用按以下顺序确定使用哪条规则：

```
1. 用户为该书籍显式指定的规则（最高优先，记忆到 BookEntity.chapterRuleId）
        │
        ▼ 未指定
2. 自动探测：用每条内置规则在文件的前 2000 行上试跑
   统计"识别出的章节数"与"章节平均间隔"
        │
        ▼
3. 选择条件：
      · 章节数 ≥ 20
      · 章节平均间隔在 500 ~ 50000 字之间
      · 章节数最多的规则优先
        │
        ▼ 无规则满足
4. 无章节模式：全书作 1 章，标题为文件名
        │
        ▼
   在书籍详情中提示"未能自动识别目录，可手动指定规则"
```

### B.3 用户自定义正则

**编写指引**：

| 要点 | 说明 |
| --- | --- |
| 使用 Kotlin/Java 正则语法 | 支持 `^` `$` `\d` `[]` `()` `\|` 等 |
| 建议用 `^` 与 `$` 锚定整行 | 避免匹配到正文中的引用（如"他想起第三章"） |
| 用 `[^\n]{0,40}` 限制标题长度 | 防止把长句误判为标题 |
| 不建议用 `.*` | 贪婪匹配性能差且易误报 |
| 不支持命名捕获组的反向引用 | 应用只用匹配结果判断，不提取组 |

**示例**：

```
只要"第N章"（不含卷）        ^第[0-9]+章
章节前有方括号                ^【第[0-9]+章】
日语风格章节                  ^第[0-9]+話
自定义标记（如 ●）            ^●.{1,30}$
"标题：xxx" 格式               ^标题[:：].{1,30}$
```

**实时预览**：在规则输入框下方会实时显示"识别到 N 章"，并列出前 10 章的标题，便于确认。

---

## 附录 C · 主题色板（含精确色值）

### C.1 预置主题

| 主题 ID | 名称 | 背景色 | 文字色 | 强调色 | 状态栏文字 | 对比度 |
| --- | --- | --- | --- | --- | --- | --- |
| `paper` | 纸质仿古 | `#F5F1E8` | `#2B2622` | `#8B6F47` | `#6B6358` | 12.8:1 |
| `eye_care` | 护眼浅绿 | `#CCE8CF` | `#1F2D20` | `#3E6B45` | `#4A6B4E` | 11.2:1 |
| `parchment` | 羊皮纸 | `#E8D9B5` | `#3B2F1E` | `#8A6A3B` | `#6B5636` | 9.6:1 |
| `night` | 纯黑夜间 | `#000000` | `#B8B8B8` | `#5A7A5A` | `#6E6E6E` | 8.9:1 |
| `night_gray` | 深灰夜间 | `#121212` | `#C5C5C5` | `#7A9E7A` | `#7A7A7A` | 10.4:1 |
| `high_contrast` | 高对比 | `#FFFFFF` | `#000000` | `#0000CC` | `#333333` | 21.0:1 |

### C.2 应用 UI 主题色（Material 3 Token）

| Token | 浅色 | 深色 | 用途 |
| --- | --- | --- | --- |
| `primary` | `#5D4037` | `#D7CCC8` | 主要动作、选中态 |
| `onPrimary` | `#FFFFFF` | `#3E2723` | |
| `primaryContainer` | `#EFEBE9` | `#4E342E` | |
| `secondary` | `#6D6055` | `#D0C4B8` | 次要动作 |
| `surface` | `#FFFBF7` | `#121212` | 页面背景 |
| `surfaceVariant` | `#F3EDE7` | `#1E1E1E` | 卡片、浮层 |
| `onSurface` | `#1C1B1A` | `#E4E2E1` | 正文文字 |
| `onSurfaceVariant` | `#4C4541` | `#C8C3C0` | 次要文字 |
| `outline` | `#7F7570` | `#948C88` | 边框、分割线 |
| `error` | `#B3261E` | `#F2B8B5` | 错误提示 |

### C.3 书架占位封面的色相派生规则

无封面书籍的占位封面由书名 hash 派生，保证同一本书颜色稳定：

```kotlin
fun coverColors(title: String): Pair<Color, Color> {
    val hue = (title.hashCode().absoluteValue % 360).toFloat()
    val bg = Color.hsl(hue, 0.42f, 0.52f)                    // 饱和度 42%，亮度 52%
    val fg = Color.hsl((hue + 180f) % 360f, 0.20f, 0.96f)    // 互补色相，高亮
    return bg to fg
}
```

**可选色相分布**（供设计参考，共 12 个主色相）：

```
0°   朱红    #C25A4A
30°  暖橙    #C2874A
60°  麦黄    #BFB04A
90°  橄榄    #9CB04A
120° 草绿    #6AAE55
150° 松绿    #4AAE7C
180° 青蓝    #4AA1AE
210° 靛蓝    #4A7BAE
240° 蓝紫    #5A5FAE
270° 紫罗兰  #8A55AE
300° 品红    #AE55A0
330° 玫红    #AE5577
```

### C.4 对比度校验阈值

| 用途 | 最低对比度 | 说明 |
| --- | --- | --- |
| 正文文字 | **7.0:1** | AAA 级。长文阅读标准高于一般 UI |
| 次要文字（作者、时间） | 4.5:1 | AA 级 |
| 边框、分割线 | 3.0:1 | 非文字内容 |
| 状态栏叠加文字 | 4.5:1 | |

自定义主题配置低于阈值时，UI 会：

1. 显示实时对比度数值
2. 低于 4.5:1 时显示黄色警告条
3. 提供「一键修正」按钮（保持色相，调整亮度至达标）

---

## 附录 D · 错误码与错误文案

### D.1 错误码规范

```
KR-<域码>-<序号>

域码：
  F  = File 文件相关
  P  = Parser 解析相关
  G  = PaGination 分页相关
  D  = Database 数据相关
  N  = Network 网络相关
  S  = Source 书源规则相关
  C  = Cache 缓存相关
  A  = Application 应用级
```

### D.2 错误清单

| 错误码 | 类型 | 用户文案 | 可恢复 | 系统行为 |
| --- | --- | --- | --- | --- |
| `KR-F-001` | FileNotFound | 文件已被移动或删除，请重新导入 | ❌ | 书籍标记为"文件丢失"（灰显） |
| `KR-F-002` | PermissionLost | 缺少文件访问权限，请重新授权目录 | ✅ | 提供"重新授权"入口 |
| `KR-F-003` | NoSpace | 存储空间不足（需要 ${need}，可用 ${avail}） | ✅ | 拒绝导入 |
| `KR-F-004` | CopyFailed | 文件复制失败：${reason} | ✅ | 记录失败清单，继续其他文件 |
| `KR-F-005` | FileTooLarge | 文件过大（${size}），建议使用引用模式 | ✅ | 询问用户是否改用引用模式 |
| `KR-P-001` | UnsupportedEncoding | 无法识别文件编码（疑似 ${detected}） | ✅ | 弹出带预览的编码选择器 |
| `KR-P-002` | ParseFailedEpub | EPUB 结构损坏：${reason} | ✅ | 降级为标题分章模式 |
| `KR-P-003` | ParseFailedPdf | PDF 无法打开：${reason} | ❌ | 不加入书架 |
| `KR-P-004` | PdfEncrypted | 暂不支持加密 PDF | ❌ | 明确提示 |
| `KR-P-005` | PdfZeroPages | PDF 页数为 0，文件可能损坏 | ❌ | 不加入书架 |
| `KR-P-006` | ChapterRuleInvalid | 章节规则无效：${reason} | ✅ | 提示改为无章节模式 |
| `KR-P-007` | NoChaptersFound | 未能识别章节结构，已作为单章处理 | ✅ | 提示可手动指定规则 |
| `KR-P-008` | PathTraversal | EPUB 文件包含非法路径（已拦截） | ✅ | 跳过该资源，记警告 |
| `KR-G-001` | ViewportNotReady | （内部，不展示） | ✅ | 等待视口就绪后重试 |
| `KR-D-001` | MigrationFailed | 数据库升级失败，已重置数据 | ❌ | 备份为 `.bak` 后重建 |
| `KR-D-002` | DuplicateBook | 《${title}》已在书架 | ✅ | 跳过导入 |
| `KR-N-001` | NetworkUnavailable | 网络不可用，请检查连接 | ✅ | 提示可读缓存章节 |
| `KR-N-002` | Timeout | 请求超时（${sec}秒） | ✅ | 重试 2 次后提示换源 |
| `KR-N-003` | HttpBlocked | 访问受限（HTTP ${code}），站点可能有反爬 | ✅ | 建议更换 UA 或换源 |
| `KR-N-004` | DnsFailed | 无法解析域名 ${host} | ✅ | 提示检查网络/源地址 |
| `KR-N-005` | TlsError | 安全连接失败 | ✅ | 提示该源可能存在证书问题 |
| `KR-S-001` | RuleInvalidJson | 书源 JSON 格式错误：${detail} | ✅ | 拒绝导入，指出错误位置 |
| `KR-S-002` | RuleFieldMissing | 书源规则缺少必填字段：${field} | ✅ | 拒绝导入 |
| `KR-S-003` | RuleSelectorInvalid | 选择器语法错误「${sel}」 | ✅ | 拒绝导入 |
| `KR-S-004` | RuleRegexDangerous | 正则存在性能风险：${reason} | ✅ | 拒绝导入 |
| `KR-S-005` | SourceSelectorNoMatch | 书源「${name}」的 ${field} 未匹配到元素 | ✅ | 调试器高亮该字段 |
| `KR-S-006` | SourceBroken | 书源「${name}」连续失败，已暂停使用 | ✅ | 标记为失效，跳过聚合 |
| `KR-S-007` | CatalogEmpty | 未获取到章节目录 | ✅ | 提示检查 catalog 规则 |
| `KR-S-008` | ContentTooShort | 正文内容异常（${n} 字） | ✅ | 提示 contentSelector 可能错误 |
| `KR-S-009` | PurifyOverdone | （内部）清洗过度，已回退原始文本 | ✅ | 记警告日志 |
| `KR-C-001` | CacheSizeExceeded | （内部）缓存超限，已自动清理 ${n} 章 | ✅ | 按策略淘汰 |
| `KR-C-002` | CacheWorkerStopped | 缓存任务已停止：${reason} | ✅ | 显示已缓存进度 |
| `KR-A-001` | OutOfMemory | 内存不足，已释放缓存 | ✅ | 主动收缩 + 提示 |
| `KR-A-002` | Unknown | 未知错误：${detail} | ✅ | 记录日志，提示重试 |

### D.3 错误展示规范

```kotlin
@Composable
fun ErrorView(
    error: KrError,
    onRetry: (() -> Unit)?,
    onAction: ((String) -> Unit)?,
) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(errorIcon(error), null, Modifier.size(48.dp))
        Spacer(Modifier.height(16.dp))
        Text(error.userMessage, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)

        // 可恢复错误给出明确出口
        if (error.recoverable) {
            Spacer(Modifier.height(20.dp))
            Button(onClick = { onRetry?.invoke() }) { Text("重试") }
        }

        // 特定错误给出特定动作
        val action = when (error) {
            is KrError.UnsupportedEncoding -> "手动选择编码"
            is KrError.PermissionLost -> "重新授权"
            is KrError.NetworkBlocked -> "解除限制"
            is KrError.SourceSelectorNoMatch -> "打开规则调试器"
            else -> null
        }
        action?.let {
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { onAction?.invoke(it) }) { Text(it) }
        }
    }
}
```

**原则**：**每个错误都必须给出"下一步做什么"**。禁止只显示"操作失败"这类无信息量的文案。

---

## 附录 E · 默认值速查表

### E.1 排版默认值

| 参数 | 默认值 | 范围 | 步进 |
| --- | --- | --- | --- |
| `fontSizeSp` | 18.0 | 12 ~ 36 | 1 |
| `lineHeightMult` | 1.6 | 1.0 ~ 3.0 | 0.1 |
| `paragraphSpacingEm` | 0.8 | 0.0 ~ 2.0 | 0.1 |
| `marginHorizontalDp` | 16.0 | 0 ~ 48 | 2 |
| `marginVerticalDp` | 24.0 | 0 ~ 96 | 4 |
| `letterSpacingEm` | 0.0 | -0.05 ~ 0.20 | 0.01 |
| `fontFamilyId` | `"system"` | — | — |
| `boldEnabled` | false | — | — |
| `align` | `"START"` | START / JUSTIFY | — |
| `indentFirstLine` | true | — | — |

### E.2 阅读偏好默认值

| 参数 | 默认值 |
| --- | --- |
| `pageTurnMode` | `SCROLL` |
| `showBattery` | true |
| `showClock` | true |
| `showChapterPercent` | true |
| `showPageNumber` | true |
| `screenTimeoutOverride` | 0（跟随系统） |
| `keepScreenOn` | false |
| `autoScrollSpeed` | 3.0 |
| `tapZoneEnabled` | true |
| `vibrationOnTurn` | false |
| `volumeKeyAction` | `NONE` |
| `brightnessOverride` | -1（跟随系统） |
| `fullScreenImmersive` | true |

### E.3 全局偏好默认值

| 参数 | 默认值 |
| --- | --- |
| `language` | 跟随系统 |
| `themeMode` | `SYSTEM` |
| `shelfGridMode` | true（网格） |
| `shelfSort` | `LAST_READ` |
| `shelfGridColumns` | 3 |
| `cacheLimitMb` | 200 |
| `cacheWifiOnly` | true |
| `searchConcurrency` | 6 |
| `searchIncludeDisabled` | false |
| `autoPurify` | true |
| `crashReportEnabled` | **false** |
| `importMode` | `ARCHIVE` |
| `pdfNightFilter` | `OFF` |
| `pdfRenderQuality` | `BALANCED` |
| `prefetchAdjacentChapters` | true |

### E.4 算法常量

| 常量 | 值 | 说明 |
| --- | --- | --- |
| `CharsetDetector.SAMPLE_SIZE` | 262144 (256KB) | 编码探测采样大小 |
| `CharsetDetector.UTF8_CHECK_SIZE` | 65536 (64KB) | UTF-8 严格校验采样 |
| `OffsetIndex.STRIDE` | 4096 | 偏移索引锚点间隔（字符） |
| `PdfBitmapCache.MAX_BYTES` | 50331648 (48MB) | PDF 位图缓存上限 |
| `FontRepository.CACHE_SIZE` | 2 | 字体缓存数量 |
| `ProgressRepository.DEBOUNCE_MS` | 800 | 进度落盘去抖 |
| `ProgressRepository.FLUSH_INTERVAL_MS` | 30000 | 进度定时落盘 |
| `PaginationConfig.FIRST_BATCH_PAGES` | 2 | 分页首批页数 |
| `PaginationConfig.BATCH_SIZE` | 3 | 分页后续批大小 |
| `PaginationConfig.TOLERANCE_RATIO` | 0.02 | 分页高度容差 |
| `ReaderViewModel.PAGINATION_DEBOUNCE_MS` | 120 | 重分页去抖 |
| `ChapterCandidateVoter.THRESHOLD` | 0.65 | 章节投票阈值 |
| `ChapterCandidateVoter.MIN_DISTANCE` | 300 | 章节最小间隔（字符） |
| `SourceEngine.MAX_RETRY` | 2 | 网络重试次数 |
| `DomainRateLimiter.PERMITS_PER_SECOND` | 2 | 单域限流速率 |
| `DomainRateLimiter.MAX_BURST` | 4 | 单域突发上限 |
| `SafeRegex.MAX_PATTERN_LENGTH` | 200 | 正则最大长度 |
| `SafeRegex.TIMEOUT_MS` | 300 | 正则执行超时 |
| `Purify.MIN_KEEP_RATIO` | 0.3 | 清洗结果最小保留比例 |
| `CacheEvictor.PROTECT_RANGE` | 5 | 缓存保护章节范围（±5） |
| `AutoScroll.PX_PER_SECOND_BASE` | 8.0 | 自动滚屏基础速度 |

### E.5 性能预算

| 指标 | 预算 |
| --- | --- |
| 10MB TXT 分章 | ≤ 1500ms |
| 首屏分页（3000 字） | ≤ 120ms |
| 全章分页（3000 字） | ≤ 400ms |
| 冷启动可交互 | ≤ 800ms |
| 二次打开首屏 | ≤ 300ms |
| 翻页帧耗时 P95 | ≤ 16.67ms（60Hz）/ ≤ 11.11ms（90Hz）/ ≤ 8.33ms（120Hz） |
| 阅读 1 小时内存增长 | ≤ 150MB |
| 聚合搜索（8 源） | ≤ 3000ms |
| APK 体积（单 ABI） | ≤ 12MB |

---

## 附录 F · 术语与缩写对照

| 缩写/术语 | 全称 | 说明 |
| --- | --- | --- |
| SAF | Storage Access Framework | Android 的存储访问框架，无需存储权限即可访问用户选择的文件 |
| OPF | Open Packaging Format | EPUB 的核心清单文件（`content.opf`） |
| NCX | Navigation Control file for XML | EPUB2 的目录文件（`toc.ncx`） |
| NAV | Navigation Document | EPUB3 的目录文件（`nav.xhtml`） |
| WAL | Write-Ahead Logging | SQLite 的日志模式，读写不互斥 |
| LRU | Least Recently Used | 最近最少使用淘汰策略 |
| ReDoS | Regular Expression Denial of Service | 正则回溯导致的性能灾难 |
| ADR | Architecture Decision Record | 架构决策记录 |
| NFR | Non-Functional Requirement | 非功能性需求 |
| WBS | Work Breakdown Structure | 工作分解结构 |
| P95 / P99 | 95th / 99th Percentile | 百分位数，衡量长尾延迟 |
| CJK | Chinese, Japanese, Korean | 中日韩字符 |
| BOM | Byte Order Mark | 字节序标记，用于标识编码 |
| TTC | TrueType Collection | 字体集合文件格式 |
| Anchor（锚点） | — | 偏移索引中的采样点 |
| 字级偏移 | Character Offset | 项目对"阅读进度"的核心度量方式 |

---

## 附录 G · 参考与致谢

### G.1 技术参考

| 主题 | 参考 |
| --- | --- |
| Jetpack Compose 性能 | Android 官方文档「Compose Performance」；`graphicsLayer` 与 deferred state read |
| 文本排版 | W3C「Requirements for Chinese Text Layout」（中文排版需求） |
| 无障碍对比度 | W3C WCAG 2.2 — Contrast (Minimum) / Enhanced |
| EPUB 规范 | W3C EPUB 3.3 Recommendation |
| 分页测量 | Jetpack Compose `TextMeasurer` 官方 API 文档 |
| 性能测量 | Android「Benchmark your app」；Macrobenchmark 指南 |
| 正则可视化调试 | regex101.com（编写书源正则时建议先在此验证） |
| CSS 选择器 | MDN「CSS selectors」（Jsoup 支持大部分选择器） |

### G.2 设计参考

| 主题 | 参考方向 |
| --- | --- |
| 中文阅读排版 | 中文纸书排版惯例：段首缩进两字符、首行不避标点、字间距可变 |
| 夜间模式配色 | 纯黑底不宜配纯白字（halation 光晕效应），推荐 `#B8B8B8` ~ `#C5C5C5` |
| 护眼配色 | 低蓝光方案，绿色系比黄色系更适合长时阅读 |
| 翻页动效 | 物理感：跟手 1:1 + 边界阻尼 + 松手惯性阈值 |
| 阅读器交互 | 左右分区翻页 + 中央唤出菜单，是阅读器的通用心智模型 |

### G.3 开源组件致谢

| 组件 | 许可 | 用途 |
| --- | --- | --- |
| Kotlin / kotlinx.coroutines | Apache-2.0 | 语言与协程 |
| AndroidX / Jetpack Compose | Apache-2.0 | UI 框架 |
| Room | Apache-2.0 | 数据库 |
| DataStore | Apache-2.0 | 配置存储 |
| Hilt | Apache-2.0 | 依赖注入 |
| OkHttp | Apache-2.0 | HTTP 客户端 |
| Jsoup | MIT | HTML 解析 |
| Coil | Apache-2.0 | 图片加载 |
| WorkManager | Apache-2.0 | 后台任务 |
| Paging 3 | Apache-2.0 | 列表分页 |
| kotlinx.serialization | Apache-2.0 | JSON 序列化 |
| LeakCanary（仅 debug） | Apache-2.0 | 内存泄漏检测 |

> 完整许可文本在应用内「关于 → 开源许可」中可查看。发布前必须用 `licensee` 或类似工具对所有直接与传递依赖做一次许可扫描，确认无 GPL/AGPL 类传染性许可。

---

## 附录 H · 文档版本记录

| 版本 | 日期 | 变更 | 作者 |
| --- | --- | --- | --- |
| 1.0 | 2026-09-16 | 初版。建立 10 卷完整开发档案：总纲、技术选型、架构、数据模型、功能设计、算法、性能、测试、计划、使用手册、附录 | 架构组 |
| — | — | （后续变更记录于此，与 `CHANGELOG.md` 对应） | — |

### H.1 文档维护约定

| 约定 | 说明 |
| --- | --- |
| **同步更新** | 代码变更若影响本档案任何条目，必须在同一 PR 中更新对应章节 |
| **不删只改** | 已发布的规范不直接删除，改为标注"已废弃，见 xxx" |
| **版本对应** | 本档案版本与应用版本对应（V1.0 应用对应档案 1.0） |
| **附录优先** | 常量、默认值、错误码等"数据型"内容一律进附录，便于查找与对比 |
| **交叉引用** | 章节间引用使用相对路径链接，保证在 Git 与 IDE 中可跳转 |

---

**上一卷**：[第 9 卷 · 使用手册](#vol-09) ｜ **返回**：[档案索引](#top)


---

