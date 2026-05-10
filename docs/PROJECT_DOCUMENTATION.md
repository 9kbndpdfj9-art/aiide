# DevView IDE（开发视界）

## 项目概述

**DevView IDE** 是全球首款**纯 Android 原生**的 AI 驱动集成开发环境。

**核心理念**：从"帮你写代码"转变为"帮你表达意图，并保证输出的正确性"。

---

## 核心架构

```
DevView IDE
├── 意图层（Intent Layer）
│   ├── IntentFabricEngine - 意图图作为项目唯一真理源
│   ├── IntentParser - 自然语言意图解析
│   └── IntentVersioning - 意图版本控制
├── 语义层（Semantic Layer）
│   ├── SemanticShadowEngine - 语义影子
│   ├── SemanticFingerprint - 语义指纹推断
│   └── SCTPProtocol - 语义压缩协议（90% Token 减少）
├── 执行层（Execution Layer）
│   ├── DevGenieEngine - 全自主意图实现引擎
│   ├── ParetoCortex - 成本-收益理性层
│   └── SilentCouncil - 多模型匿名议会
├── 协同层（Collaboration Layer）
│   ├── WindowContextEngine - 窗口上下文管理
│   └── AITriggeredScheduler - AI 自然语言定时任务
└── 引擎层（Engine Layer）
    ├── Bridge - JS-Native 桥接（180+ action 路由）
    ├── SkillManager - 技能系统（10 内置 Skill）
    └── ModelRouter - 多模型路由
```

---

## 功能清单

### 意图驱动开发
- 自然语言意图输入
- 意图图可视化
- 意图版本控制
- 多模型并行综合
- 持续全量综合

### 语义影子系统
- 语义指纹推断
- 影子状态带（绿/黄/红/紫）
- 漂移检测
- 影子传播
- 影子历史
- 时间旅行

### DevGenie 全自主引擎
- Autopilot 全自主模式
- Copilot 手动模式
- ParetoCortex 理性层
- SilentCouncil 议会
- 渐进式交付

### 专业引擎（20+）
- CodeHealthAnalyzer - 代码健康分析
- CodeDNAAutoStyle - 代码风格学习
- CodeMigrationEngine - 代码迁移
- SmartFixEngine - 智能修复
- CodeCompletionEngine - 代码补全
- WebSearchEngine - Web 搜索
- McpProtocolManager - MCP 协议
- WorkspaceManager - 工作区管理
- IntentOrchestrator - 意图编排
- SandboxWorkspace - 沙箱隔离

---

## 技术规格

- **最低 API 级别**: 26 (Android 8.0)
- **目标 API 级别**: 34 (Android 14)
- **架构**: MVVM + Clean Architecture
- **语言**: 100% Kotlin
- **存储**: SQLite + SharedPreferences + 文件系统
- **并发**: ExecutorService + Kotlin Coroutines
- **桥接**: WebView.evaluateJavascript（180+ action 路由）

---

## 核心工作流

```
1. 用户语音/文字输入意图
2. DevGenie 自动解析（2-3 秒）
3. 多模型议会（10-30 秒）
4. 渐进式交付（1-3 分钟）
5. 成果呈现
6. 用户确认（仅高风险操作）
```

---

## 性能指标

| 指标 | 目标值 |
|------|--------|
| 冷启动时间 | < 2s |
| 意图解析延迟 | < 3s |
| 多模型议会延迟 | < 30s |
| 影子检测延迟 | < 100ms |
| Token 压缩比 | 90%+ |

---

## 安全与隐私

- **本地优先**: 代码和意图数据存储在设备本地
- **加密传输**: 所有 API 调用使用 HTTPS
- **API Key 安全**: 加密存储
- **沙箱隔离**: 代码变更在沙箱中预演

---

**DevView IDE** —— 你不需要写代码。你只需要表达意图。
