# Android AI IDE

全功能AI驱动开发环境，包含88个Kotlin引擎和Rust搜索引擎

## 特性

- **88个Kotlin引擎**：智能代码生成、分析、调试
- **Rust搜索引擎**：高性能全文搜索
- **零Bug保障系统**：多文件交叉验证
- **语义Skill匹配**：自然语言触发技能
- **多Agent协作**：DevSwarm多智能体系统
- **多模态支持**：文本、代码、图像分析

## 项目结构

```
android/           # Android应用（Kotlin）
  app/src/main/java/com/aiide/
    - 88个引擎文件
engine/            # Rust搜索引擎
docs/              # 技术文档
```

## 核心引擎

| 引擎 | 功能 |
|------|------|
| Bridge.kt | 核心调度器，367+ action路由 |
| ModelRouter.kt | 模型路由与负载均衡 |
| SkillManager.kt | 技能流水线管理 |
| DevSwarmEngine.kt | 多Agent协作 |
| MultiFileCrossValidator.kt | 零Bug验证 |
| SemanticSkillMatcher.kt | 语义匹配 |

## License

MIT
