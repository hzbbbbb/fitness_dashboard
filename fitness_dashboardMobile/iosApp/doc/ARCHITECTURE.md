# 架构与目录约束

## 当前架构结论

根据现有工程，当前 iOS 端采用以下结构：

`SwiftUI App Entry -> SwiftUI ContentView -> UIKit bridge -> ComposeApp framework`

其中，`ComposeApp` 通过 Xcode 构建阶段触发上层 Gradle 任务进行嵌入。

## 目录职责

`iosApp/`

- iOS 容器层源码
- 只放入口、桥接、宿主配置和必要平台能力

`Configuration/`

- Xcode 配置项
- 只放构建和签名相关基础配置

`doc/`

- AI 行为规则库
- 不放与规则无关的随手笔记

## 架构边界

### iOS 容器层应负责

- 应用入口
- 视图容器挂载
- 与共享框架的桥接
- iOS 平台能力接入
- 最小化配置和生命周期处理

### iOS 容器层不应负责

- 主业务页面编排
- 共享业务状态托管
- 共享数据模型复制
- 通过原生层绕过共享层实现业务闭环

## 依赖方向

允许的依赖方向：

- `iOS host -> ComposeApp`
- `iOS host -> iOS system frameworks`

不建议的依赖方向：

- `iOS host -> duplicated business layer`
- `iOS host -> ad-hoc local architecture unrelated to shared layer`

## 文件修改约束

- 改 `iosApp/iOSApp.swift` 时，先确认是否真的需要动入口
- 改 `iosApp/ContentView.swift` 时，优先保持其为薄容器
- 改 `Configuration/Config.xcconfig` 时，必须考虑构建环境影响
- 改 Xcode project 配置时，必须说明是否影响上层 Gradle 集成

## 架构禁止事项

- 不要在宿主层建立完整业务分层替代共享层
- 不要把宿主层变成长期业务开发主战场
- 不要新增与当前目录职责不匹配的大型模块
- 不要因为单次需求破坏桥接层简洁性
