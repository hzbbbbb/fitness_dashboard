# FitBoard

FitBoard 是一个个人健身记录项目，目前包含两个端：

## 项目结构

- `fitness_dashboardDesktop`  
  桌面端，基于 Tauri + React + TypeScript 开发  
  用于本地记录训练、补剂、体重和热力图展示

- `fitness_dashboardMobile`  
  移动端，基于 Kotlin Multiplatform + Compose Multiplatform 开发  
  目标支持 iPhone 和未来的 Android 设备

## 当前状态

- 桌面端：已完成本地可用版本，并已发布 macOS Release
- 移动端：项目骨架已创建，正在开发中

## 说明

本项目当前以本地单机使用为主，优先保证本地记录、导入导出和跨设备手动迁移能力。