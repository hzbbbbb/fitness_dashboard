# Git 提交与阶段命名规范

## 基本原则

- 一次提交只解决一个清晰问题
- 提交名必须让人一眼看出改动目标
- 文档、结构、功能、修复尽量分开提交
- 禁止把无关格式化、重命名、重构混入同一次提交

## 分支命名

推荐格式：

`type/stage-short-topic`

示例：

- `docs/stage1-doc-rules`
- `fix/stage1-ios-entry`
- `chore/stage1-xcode-config`

`type` 可选值：

- `docs`
- `fix`
- `feat`
- `refactor`
- `chore`

## 提交信息格式

推荐格式：

`<type>(<stage>): <summary>`

示例：

- `docs(stage1): add AI collaboration rule library`
- `fix(stage1): keep iOS host as thin compose container`

## stage 命名规则

当前项目统一采用阶段标识，默认使用：

- `stage0`: 初始化和工程接入
- `stage1`: 规则收敛与基础能力稳定
- `stage2`: 核心页面接入
- `stage3`: 数据链路和交互完善
- `stage4`: 打磨与发布准备

如果没有明确进入下一阶段，不得擅自把任务按更高阶段实现。

## AI 提交约束

- AI 不得私自修改历史提交
- AI 不得把多个独立问题压成一个无法审阅的提交
- AI 生成提交说明时，必须写清楚“改了什么”和“为什么”
- 若本次只改文档，提交信息必须明确写 `docs`

## 禁止事项

- 不要使用模糊提交名，如 `update`、`fix bug`、`change code`
- 不要把大规模格式化和功能修复放在同一次提交
- 不要为了“看起来整洁”而改写用户未要求的 Git 记录
