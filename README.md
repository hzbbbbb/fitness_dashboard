# FitBoard

FitBoard 是一个本地优先的个人健身记录项目，当前包含桌面端和移动端两部分，围绕训练、补剂、步数、睡眠和体重做日常记录与查看。

## 项目结构

- `fitness_dashboardDesktop`
  - 桌面端
  - 基于 `Tauri + React + TypeScript + Vite`
  - 侧重本地记录、热力图和数据管理

- `fitness_dashboardMobile`
  - 移动端
  - 基于 `Kotlin Multiplatform + Compose Multiplatform`
  - 当前以 iOS 为主进行功能联调，并保留 Android 目标

- `doc`
  - 规范文件、开发记录和 bug 修复记录

## 当前状态

- 桌面端
  - 已完成本地可用版本
  - 已发布 macOS Release

- 移动端
  - 已完成首页 / 评分 / 记录 / 设置四个主页面
  - 已接入 Apple Health 只读数据
  - 已支持体重详情页和二级页面过渡动画

## 移动端已实现能力

- Apple Health 只读接入
  - 今日步数
  - 昨晚睡眠时长
  - 今日训练摘要
  - 今日体重
  - 最近 30 天体重历史

- 首页
  - 当日健康摘要
  - 摘要卡片排序与显隐编辑
  - 热力图与近期状态查看

- 评分页
  - 展示当天健康分
  - 基于睡眠、步数、训练、补剂四项计算

- 记录页
  - 体重卡片直接读取 Apple Health 今日体重
  - 训练、补剂、备注记录
  - 当日记录自动保存

- 体重记录页
  - 今日最新体重
  - 7 天 / 30 天趋势
  - 本周平均
  - 较上周变化
  - 最近记录列表

- 设置页
  - 训练类型
  - 补剂类型
  - 睡眠目标
  - 步数目标
  - 风格切换
  - 本地数据与关于页面

## 数据特点

- 本地优先
- 当前以单机使用为主
- 配置和每日记录保存到本地文件
- 不依赖在线账号体系

## 开发说明

- 桌面端说明见 [fitness_dashboardDesktop/README.md](./fitness_dashboardDesktop/README.md)
- 移动端说明见 [fitness_dashboardMobile/README.md](./fitness_dashboardMobile/README.md)
