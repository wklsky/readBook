# Kotlin Reader · 项目长期记忆

## 项目定位

纯净、轻量、高自定义的多格式本地阅读 + 网络聚合阅读器（Android / Kotlin / Compose）。

## 三条产品红线（任何迭代不得违反）

1. 无广告、无推送、无账号强制登录
2. 本地文件不出设备（网络能力仅用于用户自配置的书源抓取）
3. 不吃性能（性能预算是功能准入硬门槛）

## 核心技术约定

| 主题 | 约定 |
| --- | --- |
| 阅读进度 | **字符级偏移**（`chapterIndex` + `charOffsetInChapter`），严禁用页码或百分比做恢复定位 |
| 分页产物 | `PageSnapshot` 必须携带 `startCharInChapter` / `endCharInChapter`，页区间必须严格连续覆盖全文 |
| 线程模型 | 分章/解析 → `KrDispatchers.Parse`；分页测量 → `Dispatchers.Default` + `Mutex` 串行；文件与网络 → `IO` |
| 大文件 | **禁止** `readText()` / `readBytes()`，一律流式解码（detekt ForbiddenMethodCall 强制） |
| 进步写入 | 三级：内存实时 → 800ms 去抖落盘 → 30s/退出强制落盘 |
| 分层 | `feature → domain → data → core`；`core:model` 纯 Kotlin 无 Android 依赖；feature 之间禁止互相依赖 |
| Compose | 参数只传数据 + lambda（不传 ViewModel）；动画用 `Animatable` + `graphicsLayer { }`（状态读下沉）；列表必须给稳定 key |
| 协程 | 禁 `GlobalScope`；suspend 函数必须可取消（大循环插 `ensureActive()`）；UI 用 `collectAsStateWithLifecycle` |

## 目录与文档约定

- 开发档案在 `docs/`，共 11 个文件（README + 00~10 卷）。
- 修改档案：编辑 `docs/` 下分卷 → 运行 `node scripts/build-archive.js` → 自动重生成根目录的合并版与 HTML 版。
- 不要直接编辑 `KotlinReader-完整开发档案.md` / `KotlinReader-开发档案.html`（生成产物）。
- 跨卷引用写 `./NN-xxx.md`，构建时自动转为 `#vol-NN` 锚点。

## 性能硬指标（V1.0 发布门槛）

| 指标 | 目标 |
| --- | --- |
| 10MB TXT 首次分章 | ≤ 1500ms（中端机） |
| 翻页帧率 | ≥ 60fps（120Hz 屏 ≥ 90fps） |
| 连续阅读 1 小时内存波动 | ≤ 150MB 且无泄漏 |
| 3000 字章节首屏分页 | ≤ 120ms |
| 二次打开书籍首屏 | ≤ 300ms |
| 冷启动可交互 | ≤ 800ms |
| 聚合搜索（8 源） | ≤ 3000ms |
| APK 体积（单 ABI） | ≤ 12MB |

## 模块划分（14 个）

`app` / `build-logic` / `core:{common,model,database,datastore,designsystem,ui,network,testing}` /
`domain` / `data:{books,parser,pagination,sources,settings}` /
`feature:{bookshelf,reader,settings,sources,search,about}`

## 开发进度

- 进度快照与后续计划写在 `docs/开发进度与后续计划.md`（该文件为交接文档，**不参与** `scripts/build-archive.js` 合并）。
- 每次开工先读该文件；每完成一个阶段同步更新「已完成清单 / 待复核点」。

## 版本路线

V0.1 骨架（4周）→ V0.2 能读（5周）→ V0.3 好用（4周）→ V0.4 聚合（5周）→ V1.0 发布（4周）。
合计约 256 人日。
