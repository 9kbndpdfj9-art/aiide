# AIIDE 项目审计与任务拆分

## 1. 当前已完成的工作

本轮已完成以下事项：

- 已从 `https://github.com/9kbndpdfj9-art/aiide/archive/refs/heads/main.zip` 下载并解压项目。
- 已确认当前仓库主体为 Android Kotlin 项目，核心代码位于 `android/app/src/main/java/com/aiide`。
- 已完成首轮静态审计，重点检查了：
  - Gradle 配置
  - Android Manifest
  - 入口 Activity
  - Bridge/Model/Skill/Intent 等核心链路
  - 资源目录与 assets 完整性
- 已识别出一批高优先级阻断问题、运行时风险和明显占位实现。

## 2. 项目现状结论

当前项目不是“少量 bug”，而是“原型骨架 + 大量声明式能力 + 不完整接线”的状态。

从结构上看，仓库声明了很多能力：

- 多模型路由
- Bridge 动作分发
- 技能系统
- 多 Agent 协作
- 搜索与审计能力
- Android WebView 入口

但实际落地状态存在明显断层：

- 构建链未完整接通
- Android 资源不完整
- Web 入口资源缺失
- 多个核心类存在编译级错误
- 很多功能虽然有 action、类名、函数名，但内部只是占位返回

## 3. 已确认的问题清单

### P0：构建/编译阻断

1. `android/app/build.gradle.kts` 未应用 Kotlin Android 插件。
   - 现状：只有 `com.android.application`
   - 风险：项目主代码全部是 `.kt`，构建链不完整

2. `android/app/build.gradle.kts` 缺少关键 AndroidX 依赖。
   - 现状：只有 junit
   - 风险：`AppCompatActivity`、常规 AndroidX 组件无法正常编译

3. `Bridge.kt` 中大批 lambda 使用了未定义的 `it`。
   - 现状：lambda 参数声明为 `input`，实现里却调用 `it.toJson()`
   - 风险：直接编译失败

4. `ModelProvider.kt` 默认 final，但 `OpenAIProvider.kt` 和 `AnthropicProvider.kt` 在继承它。
   - 风险：Kotlin 会直接报继承错误

5. 多个类使用 `ReentrantReadWriteLock.write {}`，但未导入对应扩展。
   - 典型位置：
     - `ModelRouter.kt`
     - `SkillManager.kt`
     - `AutoDebugger.kt`
   - 风险：未解析符号，无法编译

6. `MainActivity.kt` 读取版本号的对象使用错误。
   - 现状：从 `ApplicationInfo` 读取 `versionCode/longVersionCode`
   - 风险：编译失败或逻辑错误

7. Android 资源缺失。
   - `AndroidManifest.xml` 引用了：
     - `@style/Theme.AIIDE`
     - `@mipmap/ic_launcher`
     - `@mipmap/ic_launcher_round`
   - 现状：`res` 下只有：
     - `layout/activity_main.xml`
     - `values/strings.xml`
   - 风险：资源链接失败

8. `PreviewActivity.kt` 引用了不存在的布局和 View ID。
   - 缺失资源：
     - `activity_preview.xml`
     - `previewWebView`
     - `urlInput`
     - `loadButton`

### P1：高概率运行时问题

1. `MainActivity.kt` 加载 `file:///android_asset/www/index.html`，但项目内不存在对应 assets。
   - 风险：启动后 WebView 空白或 404

2. `Bridge.dispatch()` 采用动作字符串分发，但 UI 输入是自然语言。
   - 风险：大部分输入无法命中 action，功能看似存在，实际上不可用

3. `evaluateJavascript("window.receiveMessage($result)")` 直接拼接结果。
   - 风险：返回值若不是合法 JS 片段，前端执行会报错

4. `AtomicFileWriter.kt` 的“原子写入”并不可靠。
   - 现状：`renameTo()` 返回值未检查
   - 风险：失败时静默成功，可能造成数据不一致

5. `ScreenshotCapture.kt` 依赖旧式 drawing cache。
   - 风险：新系统兼容性差，可能得到空图或异常

### P2：明显未完整实现/占位实现

1. `Bridge.kt` 注册了大量 action，但绝大多数 handler 只是返回固定成功 JSON。
   - 结论：动作表很大，但实际能力未落地

2. `OpenAIProvider.kt` 和 `AnthropicProvider.kt` 没有真实 API 调用。
   - 现状：仅返回拼接字符串
   - 结论：AI 能力处于 mock 状态

3. `IntentOrchestrator.kt` 的执行器只是返回 `[HANDLED]`
   - 结论：意图编排未真正实现

4. `LiveCodeSession.kt` 的代码执行只是返回 `[EXECUTED]`
   - 结论：实时执行能力是占位

5. `SensorBridge.kt` 读取加速度计时直接返回 `null`
   - 结论：传感器桥接未完成

## 4. 架构层面的总体判断

这个仓库更像是“产品设想与命名体系已经铺开”，但离“可构建、可启动、可用”还有明显距离。

目前至少存在三层断裂：

1. 构建层断裂
   - Gradle 与 Kotlin/AndroidX 没接齐
   - 资源和布局缺失

2. 入口层断裂
   - 主界面依赖的 Web assets 不存在
   - 输入协议与 Bridge 分发协议不一致

3. 能力层断裂
   - AI Provider、意图编排、会话执行、传感器等都还停留在占位逻辑

## 5. 建议的任务拆分

考虑到 token/预算有限，建议按最小可交付单元拆分，不要一次铺太大。

### 阶段 A：先让项目能“构建”

目标：修复最基础的编译阻断。

任务：

- A1. 修复 `android/app/build.gradle.kts`
  - 应用 Kotlin Android 插件
  - 增加最小必要 AndroidX 依赖
  - 补齐基础 Android 配置

- A2. 补齐最小资源集
  - 创建 `Theme.AIIDE`
  - 补一个可用的 `activity_preview.xml`
  - 处理启动图标引用问题

- A3. 修复显式 Kotlin 编译错误
  - `Bridge.kt` 的 lambda 参数错误
  - `ModelProvider.kt` 继承问题
  - `write {}` 导入问题
  - `MainActivity.kt` 版本号读取问题

交付标准：

- 工程至少能通过基础编译检查

### 阶段 B：让应用能“启动”

目标：避免打开应用就是空白/崩溃。

任务：

- B1. 补齐 `android_asset/www/index.html` 最小页面
- B2. 让 `MainActivity` 与前端页面完成最小通信闭环
- B3. 修复 `evaluateJavascript` 返回值拼接方式
- B4. 处理 Preview 页面缺失资源问题

交付标准：

- App 启动后能看到界面
- 输入后不会立刻走空白/报错

### 阶段 C：让核心链路“真正能跑”

目标：最小化打通一个真实功能闭环。

任务：

- C1. 重新定义 `Bridge.dispatch()` 输入协议
  - 支持 JSON action
  - 或增加自然语言到 action 的映射层

- C2. 选一个最核心功能先做真实现
  - 推荐优先：
    - `code.review`
    - `search.web`
    - `file.read`
  - 不建议一开始就全做

- C3. 用真实实现替换部分固定成功 JSON

交付标准：

- 至少 1 到 3 个 action 不再是占位，而是可实际使用

### 阶段 D：再处理“名义上存在但实际上是 mock 的能力”

目标：控制范围，逐步去 mock 化。

任务：

- D1. AI Provider 真正接 API
- D2. IntentOrchestrator 真实编排
- D3. LiveCodeSession 引入真实执行策略
- D4. SensorBridge 补齐可用数据获取逻辑

交付标准：

- 每次只完成一个子系统，不做大面积同时施工

## 6. 低 token 执行策略

你说预算有限，所以后续建议严格按下面方式推进：

1. 每一轮只做一个阶段或一个子阶段
   - 例如只做 A1 + A2
   - 或只做 B1

2. 优先修“阻断型问题”
   - 先构建
   - 再启动
   - 再功能

3. 不要一开始就实现全部 AI/Agent 能力
   - 当前很多模块是命名完整、实现不完整
   - 全量修复成本很高，容易烧 token

4. 先建立最小可运行骨架
   - 先把项目从“不能构建”推进到“能启动”
   - 再选最关键的 1 到 3 个功能做深

## 7. 推荐的下一步执行顺序

如果下一步由我继续做，推荐顺序如下：

1. 先做阶段 A
   - 修 Gradle
   - 补资源
   - 修编译级错误

2. 再做阶段 B
   - 补 assets 页面
   - 打通最小 UI 闭环

3. 然后只挑一个功能做真实落地
   - 推荐先做 `file.read` 或 `code.review`

## 8. 后续同步策略

你已经说明：

- 仓库是你的
- token 已经在 MCP 里配置好了
- 后续我完成修改后可以直接帮你同步

因此后续流程可以固定为：

1. 我先在本地完成一个小阶段改动
2. 我做最小验证
3. 我整理变更说明
4. 再通过 GitHub MCP 帮你同步到仓库

建议不要一口气攒很多改动再同步，而是按阶段同步：

- `phase-a-build-fix`
- `phase-b-app-bootstrap`
- `phase-c-core-action`

这样更省 token，也更容易回滚与追踪。

## 9. 当前结论

当前项目最关键的事实是：

- 代码文件很多
- 宣称能力很多
- 但当前可运行程度很低

最正确的推进方式不是“全量实现所有功能”，而是：

- 先修阻断
- 再做最小可运行
- 再逐步把 placeholder 能力替换成真实实现

---

如需继续，下一轮建议直接执行：

`阶段 A：修复可构建性`
