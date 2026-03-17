# 文档规范库总入口

本目录不是说明书集合，而是本项目的 AI 行为约束库。

任何 AI 在分析、写作、修改、补充代码或文档前，必须完整阅读 `doc/` 下全部 `.md` 文件。不得只看文件名后直接开始输出，不得跳过未直接相关的规则文件。

## 阅读顺序

1. `README.md`
2. `PROJECT.md`
3. `CONTRIBUTING.md`
4. `PROMPT_START.md`
5. `ARCHITECTURE.md`
6. `STACK.md`
7. `UI_RULES.md`
8. `STATE_RULES.md`
9. `STORAGE_RULES.md`
10. `GIT_RULES.md`

## 规则优先级

当多个文档同时约束同一问题时，按以下优先级执行：

1. `PROJECT.md`
2. `ARCHITECTURE.md`
3. `STACK.md`
4. `UI_RULES.md`
5. `STATE_RULES.md`
6. `STORAGE_RULES.md`
7. `CONTRIBUTING.md`
8. `PROMPT_START.md`
9. `GIT_RULES.md`

## 使用方式

- AI 接到任务后，先说明已阅读哪些 `doc` 文件。
- AI 必须先判断任务是否超出 `PROJECT.md` 中的阶段边界。
- AI 修改前后输出格式必须遵守 `PROMPT_START.md`。
- AI 涉及结构、页面、状态、存储、提交时，必须同时遵守对应规则文档。

## 违例处理

以下行为视为不合规：

- 未完整阅读 `doc/` 即开始分析或修改
- 以“顺手优化”为由进行跨模块重构
- 未说明风险点就直接修改核心链路
- 未区分“当前阶段必须做”和“暂不做”
- 在 iOS 容器层中堆积业务逻辑
- 未按输出模板汇报修改内容

## 当前工程事实

根据当前工程结构，本项目至少包含以下边界事实：

- 当前目录是 iOS App 容器工程。
- 入口为 `iosApp/iOSApp.swift`。
- `iosApp/ContentView.swift` 通过 `UIKit` 容器承载 `ComposeApp`。
- Xcode 构建阶段会调用上层 Gradle 任务 `:composeApp:embedAndSignAppleFrameworkForXcode`。

因此，本目录中的规则默认服务于“iOS 容器层 + 上游 Compose/KMP 集成”场景，而不是纯原生 SwiftUI 单端项目。
