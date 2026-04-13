# FitBoard Mobile

FitBoard Mobile 是 FitBoard 的移动端实现，当前基于 `Kotlin Multiplatform + Compose Multiplatform` 开发，主要围绕 iOS 使用场景构建，并保留 Android 目标。

## 当前页面结构

- 首页
  - Apple Health 摘要
  - 摘要卡片编辑
  - 热力图与近期状态

- 评分页
  - 当天健康分
  - 睡眠 / 步数 / 训练 / 补剂四项评分

- 记录页
  - 今日体重
  - 今日训练
  - 今日补剂
  - 备注

- 体重记录页
  - 今日最新体重
  - 最近 7 天 / 30 天趋势
  - 本周平均
  - 较上周变化
  - 最近记录列表

- 设置页
  - 训练类型
  - 补剂类型
  - 睡眠目标
  - 步数目标
  - 风格
  - 本地数据
  - 关于

## Apple Health 接入

当前 iOS 宿主层已接入 Apple Health 只读权限，读取以下数据：

- 步数 `stepCount`
- 睡眠 `sleepAnalysis`
- 训练 `workout`
- 体重 `bodyMass`

说明：

- 当前只读，不写回 Apple Health
- 今日体重优先显示当天最新一条
- 体重历史使用“每天最新一条”作为展示口径

## 本地数据

- 应用配置与每日记录保存到本地文件
- 当天健康摘要可同步进入记录状态
- 当前以本地单机使用为主

## 目录说明

- `composeApp/src/commonMain/kotlin`
  - 共享 Compose UI 和业务逻辑

- `composeApp/src/iosMain/kotlin`
  - iOS 侧实际实现

- `composeApp/src/androidMain/kotlin`
  - Android 侧实际实现

- `iosApp/iosApp`
  - iOS 宿主工程
  - 包含 SwiftUI 入口和 HealthKit 读取实现

## 常用命令

在 `fitness_dashboardMobile` 目录下运行：

```bash
./gradlew :composeApp:compileKotlinIosSimulatorArm64
```

```bash
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -destination 'generic/platform=iOS Simulator' build
```

## 当前开发重点

- iOS 体验优先
- 本地优先
- Apple Health 只读整合
- 页面结构和记录链路持续补齐
