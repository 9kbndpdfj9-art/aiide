# AI IDE - 意图图谱 (Intent Graph) 功能更新

## 更新概述

本次更新成功实现了**阶段2**的核心功能：**意图图谱 (Intent Graph)**。这是 AI IDE 的颠覆性创新之一，允许将代码组织成语义网络，而非单纯的文件系统。

## 新增功能

### 1. IntentGraph.kt - 意图图谱核心数据结构
**文件位置**: `/workspace/android/app/src/main/java/com/aiide/IntentGraph.kt`

**核心功能**:
- `IntentNode` 数据类：表示意图节点，包含标签、类型、文件范围、描述、标签、元数据等
- `IntentEdge` 数据类：表示节点间的关系，包含源节点、目标节点、关系类型、强度
- `NodeType` 枚举：支持 FUNCTION, CLASS, MODULE, COMPONENT, API_ENDPOINT, DATABASE_QUERY, UI_ELEMENT, BUSINESS_LOGIC, UTILITY, TEST, CONFIG 等类型
- `EdgeType` 枚举：支持 DEPENDS_ON, AFFECTS, IMPLEMENTS, GENERATES, CALLS, IMPORTS, EXTENDS, INSTANTIATES, USES 等关系类型
- 线程安全的图谱操作：使用 `ReentrantReadWriteLock` 保证并发安全
- 影响传播分析：`getImpactedNodes()` 支持分析节点变更的影响范围
- JSON 序列化/反序列化：支持图谱的导入导出

### 2. DatabaseHelper.kt - 意图图谱持久化
**文件位置**: `/workspace/android/app/src/main/java/com/aiide/DatabaseHelper.kt`

**新增功能**:
- 新增 `intent_nodes` 表：存储意图节点数据
- 新增 `intent_edges` 表：存储节点关系数据
- `saveIntentNode()`: 保存单个意图节点
- `getIntentNode()`: 获取单个意图节点
- `getAllIntentNodes()`: 获取所有意图节点
- `saveIntentEdge()`: 保存单个关系边
- `getAllIntentEdges()`: 获取所有关系边
- `loadIntentGraph()`: 从数据库加载完整图谱
- `saveIntentGraph()`: 保存完整图谱到数据库
- 数据库版本升级到 v4

### 3. IntentAnalyzer.kt - 代码分析和意图提取
**文件位置**: `/workspace/android/app/src/main/java/com/aiide/IntentAnalyzer.kt`

**支持的语言**:
- JavaScript/TypeScript/JSX/TSX
- Python
- HTML
- CSS
- Kotlin/Java
- Markdown

**分析能力**:
- JavaScript: 提取函数声明、箭头函数、类定义
- Python: 提取函数和类定义
- HTML: 识别元素和 ID
- CSS: 分析选择器
- Kotlin: 提取类和函数
- Markdown: 分析标题结构
- 自动移除注释后再分析
- 从用户提示词中提取意图

### 4. Bridge.kt - 桥接层扩展
**文件位置**: `/workspace/android/app/src/main/java/com/aiide/Bridge.kt`

**新增 Action**:
- `initIntentGraph`: 初始化空图谱
- `loadIntentGraph`: 加载已保存的图谱
- `saveIntentGraph`: 保存图谱
- `analyzeFile`: 分析文件并提取意图节点
- `addIntentNode`: 添加单个意图节点
- `addIntentEdge`: 添加单个关系边
- `searchIntentGraph`: 搜索意图节点
- `getIntentGraphStats`: 获取图谱统计信息
- `getImpactedNodes`: 获取受影响的节点

### 5. 前端 UI 更新

**新增 UI 元素**:
- **工具栏按钮**: 
  - "Analyze" 按钮：分析当前文件
  - "Graph" 按钮：打开/关闭意图图谱面板
- **意图图谱面板**:
  - 可滑动的右侧面板
  - 显示节点统计 (Nodes/Edges)
  - 意图节点列表视图
  - 点击节点可跳转到对应文件
- **节点卡片**:
  - 显示节点类型标签
  - 节点名称和描述
  - 相关标签

### 6. 前端功能实现
**新增函数**:
- `initIntentGraph()`: 初始化图谱
- `analyzeCurrentFile()`: 分析当前打开的文件
- `toggleIntentGraph()`: 切换图谱面板显示
- `loadIntentGraphData()`: 从后端加载图谱数据
- `saveIntentGraphData()`: 保存图谱数据
- `renderIntentGraph()`: 渲染图谱节点列表
- `getIntentGraphStats()`: 获取统计信息
- `updateIntentIcons()`: 更新图标显示

## 技术特点

### 1. 线程安全
- 使用 `ReentrantReadWriteLock` 实现读写分离
- 支持多线程并发访问
- 使用 `ConcurrentHashMap` 存储节点和边

### 2. 数据持久化
- SQLite 数据库存储
- 支持完整图谱的保存和加载
- 增量更新单个节点和边
- 数据库版本管理

### 3. 多语言支持
- 正则表达式基础的语法分析
- 可扩展的语言处理器架构
- 注释自动过滤
- 行号精确追踪

### 4. 用户体验
- 响应式设计，支持移动设备
- 平滑的面板动画
- 直观的节点可视化
- 点击节点跳转到代码

## 使用示例

### 分析一个 JavaScript 文件
```javascript
// 1. 打开文件
// 2. 点击 "Analyze" 按钮
// 3. 系统自动提取函数和类
// 4. 点击 "Graph" 查看意图节点
```

### 搜索意图节点
```javascript
// 未来功能：按语义搜索相关代码
// searchIntentGraph("用户认证") 
// 结果：找到所有与认证相关的函数和类
```

### 影响分析
```javascript
// 未来功能：分析修改某个函数的影响范围
// getImpactedNodes("handleLogin") 
// 结果：显示所有依赖登录功能的节点
```

## 下一步计划

### 阶段2 - 继续完善
- [ ] 实现节点间关系的自动提取
- [ ] 添加意图图谱的可视化图表（使用 Canvas 或 SVG）
- [ ] 实现语义搜索功能
- [ ] 支持用户手动添加/编辑意图节点
- [ ] 添加代码时光机 (Code Time Travel) 基础
- [ ] 实现预测性编辑 (Predictive Edit)

### 阶段3 - 高级功能
- [ ] 实时差异感知 (Live Diff)
- [ ] 上下文编织 (Context Weaving)
- [ ] 自进化 AI (Self-Evolving AI)
- [ ] React 前端重构
- [ ] Rust 引擎完整集成

## 架构设计

```
┌─────────────────────────────────────────────────────────────┐
│                     前端 (WebView)                          │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────────────┐ │
│  │  代码编辑器   │  │  AI 聊天     │  │  意图图谱面板     │ │
│  └──────┬───────┘  └──────┬───────┘  └─────────┬─────────┘ │
└─────────┼─────────────────┼─────────────────────┼───────────┘
          │                 │                     │
          └─────────────────┼─────────────────────┘
                            │
┌───────────────────────────┼──────────────────────────────────┐
│         Kotlin 层        │                                  │
│  ┌───────────────────────▼───────────────────────────────┐  │
│  │                    Bridge.kt                            │  │
│  └────────────┬───────────────────┬──────────────────────┘  │
│               │                   │                         │
│  ┌────────────▼───┐  ┌────────────▼──────────┐            │
│  │ IntentAnalyzer │  │    IntentGraph        │            │
│  └────────────────┘  └────────────┬──────────┘            │
│                                    │                         │
│                         ┌──────────▼──────────┐            │
│                         │   DatabaseHelper    │            │
│                         └─────────────────────┘            │
└─────────────────────────────────────────────────────────────┘
```

## 总结

本次更新成功实现了意图图谱的基础框架，包括：
✅ 完整的意图图谱数据结构
✅ 数据库持久化支持
✅ 多语言代码分析能力
✅ 前端 UI 和交互
✅ 桥接层 API 设计

这为后续的高级功能（代码时光机、预测性编辑、语义搜索等）奠定了坚实基础！
