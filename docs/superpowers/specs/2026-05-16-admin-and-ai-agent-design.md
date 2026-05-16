# 校园自习座位预约系统 — 管理端、AI 对话智能体、前端重设计 Spec

## 全局视觉风格

统一整个系统的 UI 风格：
- **主色调**：紫色渐变 `#667eea → #764ba2`（按钮、头部、强调元素）
- **背景色**：`#f5f6fa`（内容区）、白色（卡片）
- **圆角**：卡片 12-16px，按钮 8-10px，输入框 8-10px
- **状态色**：空闲 `#52c41a`、已预约 `#fa8c16`、占用 `#d9d9d9`、暂离 `#1890ff`、禁用/爽约 `#ff4d4f`
- **阴影**：卡片 `0 1px 4px rgba(0,0,0,0.05)`，弹窗 `0 8px 32px rgba(0,0,0,0.15)`
- **字体层级**：标题 16-18px bold，正文 13-14px，辅助 11-12px

---

## Part 1: 管理端

### 概述

在现有学生端基础上增加管理端功能。使用预设管理员账号（admin）登录后跳转至管理后台，支持区域管理、座位管理、用户管理。

### 数据模型变更

**User 表新增字段：**

```sql
ALTER TABLE user ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'student';
```

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| role | VARCHAR(20) | `student` | `student` or `admin` |

**预设管理员账号**：在 `DataInitializer` 中启动时初始化，不存在则创建：
- studentId: `admin`, password: `admin123`, name: `管理员`, role: `admin`

### 后端 API

#### 登录改造

`POST /api/student/login` 响应 data 中增加 `role` 字段，前端据此跳转。

#### 管理员鉴权

所有 `/api/admin/*` 请求需携带 `role = admin` 的用户 session。实现：`AdminAuthInterceptor` + `WebMvcConfigurer`，匹配 `/api/admin/**`，非管理员返回 `{ code: 401, message: "无管理员权限" }`。

#### Admin REST API

Base: `/api/admin`

**仪表盘**

| Method | Path | Response |
|--------|------|----------|
| GET | `/dashboard` | `{ totalSeats, availableSeats, todayReservations, violationUsers }` |

**区域管理**

| Method | Path | Body/Params | Description |
|--------|------|-------------|-------------|
| GET | `/areas` | — | 全部区域列表（含已禁用） |
| POST | `/areas` | `name`, `floor` | 新增区域 |
| PUT | `/areas/{id}` | `name`, `floor` | 编辑区域 |
| PUT | `/areas/{id}/disable` | `disabled` (boolean) | 启用/禁用区域 |

**座位管理**

| Method | Path | Body/Params | Description |
|--------|------|-------------|-------------|
| GET | `/seats` | `areaId` (optional) | 座位列表，可按区域筛选 |
| POST | `/seats` | `areaId`, `seatNumber` | 新增座位 |
| PUT | `/seats/{id}` | `areaId`, `seatNumber` | 编辑座位 |
| PUT | `/seats/{id}/disable` | `disabled` (boolean) | 启用/禁用座位 |
| PUT | `/seats/{id}/release` | — | 手动释放座位（状态→空闲，关联预约→已结束） |

**用户管理**

| Method | Path | Body/Params | Description |
|--------|------|-------------|-------------|
| GET | `/users` | — | 用户列表（不含 admin），含违规次数和限制状态 |
| PUT | `/users/{id}/ban` | `banned` (boolean) | 封禁/解封用户 |
| PUT | `/users/{id}/violation-reset` | — | 违规次数归零，解除限制 |

**新增 Controller**: `AdminController`，新增 Service: `AdminService` / `AdminServiceImpl`。

### 前端

#### 路由

```
/admin/dashboard  → AdminLayout + Dashboard
/admin/areas       → AdminLayout + AreaManage
/admin/seats       → AdminLayout + SeatManage
/admin/users       → AdminLayout + UserManage
/admin             → redirect → /admin/dashboard
```

#### 页面结构

- **App.vue**：保持现有紫色渐变背景（学生端）；管理端 `AdminLayout` 使用白色背景
- **Login.vue**：登录成功后根据 `role` 跳转 — `admin` → `/admin/dashboard`，否则 → `/home`
- **AdminLayout.vue**：左侧深色边栏 (200px, `#1a1a2e`) + 右侧内容区。导航项：仪表盘、区域管理、座位管理、用户管理，选中高亮 `#667eea`。底部退出登录按钮。
- **Dashboard.vue**：4 个统计卡片（总座位、空闲座位、今日预约、违规用户），调用 `GET /api/admin/dashboard`
- **AreaManage.vue**：表格 + 新增/编辑 Modal（名称 + 楼层），操作列：编辑 + 禁用/启用
- **SeatManage.vue**：区域筛选下拉 + 表格（座位号、所属区域、状态彩色 badge、操作）。操作列：编辑 + 释放（占用/暂离时显示）+ 禁用/启用
- **UserManage.vue**：表格（学号、姓名、违规次数、限制状态 badge、操作）。操作列：封禁/解封 + 重置违规。确认弹窗防止误操作

### 错误处理

- 统一 `{ code, message, data }` 格式
- 401: 非管理员访问 `/api/admin/*`
- 404: 实体不存在
- 400: 校验失败（如重复座位号）
- 前端 `alert(result.message)`

---

## Part 2: AI 对话智能体

### 概述

在学生端新增"AI 自习助手"页面，通过自然语言对话帮助学生预约座位和规划自习计划。后端代理调用 DeepSeek API，使用 Function Calling 实现工具调用。

### 架构

```
Vue Chat 页面
    │  POST /api/student/chat  { message, conversationId }
    ▼
ChatController
    │
    ▼
ChatService
    │  ① 构建 system prompt + function definitions + 对话历史
    ▼
DeepSeek API  ──→  返回 function_call 或 text reply
    │
    ▼  (如果是 function_call)
ChatService 执行工具调用
    │  (查座位 / 创建预约 / 取消预约 / 查记录 / 查个人信息)
    ▼
结果回传 DeepSeek → 生成最终回复 → 返回前端
```

### 后端

#### 新增 API

**Chat endpoint**

| Method | Path | Body | Response |
|--------|------|------|----------|
| POST | `/api/student/chat` | `{ message, conversationId }` | `{ code, message, data: { reply, conversationId } }` |

conversationId 由前端生成（UUID），首次对话创建，后续复用，用于维护对话历史。

#### Function Definitions

注册给 LLM 的可用工具：

| Function | Parameters | Description |
|----------|------------|-------------|
| `search_seats` | `area` (optional), `date`, `startTime`, `endTime` | 查询指定日期时段的空闲座位 |
| `create_reservation` | `seatId`, `startTime`, `endTime` | 为用户创建预约 |
| `cancel_reservation` | `reservationId` | 取消指定预约 |
| `get_my_reservations` | — | 查询当前用户的预约列表 |
| `get_my_profile` | — | 查询用户个人信息（违规次数、限制状态） |

每个 function 执行前校验用户登录状态。`create_reservation` 执行时校验预约规则（次数限制、违规限制、时间冲突）。

#### System Prompt

角色设定："你是校园自习助手，帮助学生预约自习座位和规划学习计划。你应该先理解用户意图，必要时调用工具获取信息，最后用自然语言回复用户。"

约束规则：
- 预约前必须确认日期、时段、区域/座位偏好
- 创建预约前需用户明确确认
- 自习计划规划时，先查询可用座位再推荐，以表格呈现计划
- 批量预约时逐个创建，失败时告知用户哪些成功哪些失败
- 回复简洁友好，使用中文

#### 对话历史管理

后端内存存储（ConcurrentHashMap<String, List<Message>>），key 为 conversationId。每条消息包含 role（system/user/assistant/tool）和 content。服务重启丢失，可接受。

#### 新增文件

| File | Purpose |
|------|---------|
| `backend/.../controller/ChatController.java` | Chat REST endpoint |
| `backend/.../service/ChatService.java` | Chat logic interface |
| `backend/.../service/impl/ChatServiceImpl.java` | LLM 调用 + 工具执行 |
| `backend/.../config/DeepSeekConfig.java` | API Key 配置、HTTP Client Bean |

#### application.yml 新增配置

```yaml
deepseek:
  api-key: ${DEEPSEEK_API_KEY:your-api-key}
  api-url: https://api.deepseek.com/v1/chat/completions
  model: deepseek-chat
```

### 前端

#### 路由

```
/ai-assistant → ChatPage
```

#### 进入方式

在现有学生端主页（Home.vue）的顶部增加一个"AI 助手"按钮，点击跳转到 `/ai-assistant`。或在底部增加导航栏。

#### ChatPage.vue

全屏对话页面：
- **顶部栏**：渐变紫色背景（与 App.vue 一致），显示"自习助手 · 在线"，左侧返回按钮，右侧关闭按钮
- **消息列表**：滚动区域，用户消息靠右（紫色气泡），AI 消息靠左（白色气泡 + 头像），支持显示预约卡片和计划表格等富文本内容
- **底部输入栏**：输入框 + 发送按钮，支持 Enter 发送
- **自动滚动**：新消息到来时自动滚动到底部
- **加载状态**：AI 思考时显示"对方正在输入..."动画

#### 数据流

1. 页面挂载时生成 `conversationId = crypto.randomUUID()` 存入组件 data
2. 用户发送消息 → `POST /api/student/chat`
3. 收到回复后追加到消息列表
4. 对话历史由后端维护，前端只存 conversationId

### 自习计划规划流程

用户："我周二三四下午有空，想每天学 2 小时高数" →
1. LLM 解析意图 → 调用 `search_seats` 查询对应日期下午的可用座位
2. LLM 整理结果，以表格呈现推荐计划（日期、时段、座位号）
3. 用户确认 → LLM 逐个调用 `create_reservation`
4. LLM 返回汇总结果（成功 N 个，失败 M 个及原因）

### 错误处理

- LLM API 调用超时（10s）→ 返回"助手暂时无法响应，请稍后重试"
- Function 执行异常 → 返回错误信息给 LLM，LLM 转化为友好提示
- 用户未登录 → 返回 `{ code: 401, message: "请先登录" }`
- 当日预约次数已满 → LLM 友好提示用户

---

## Part 3: 学生端前端重设计

### 概述

对现有 4 个学生端页面进行视觉重设计，统一风格，与新增的管理端和 AI 助手页面保持一致。

### 全局布局变更

**App.vue**：去掉内部的居中 flex 布局和紫色渐变背景。改为：
- 学生端：`#f5f6fa` 灰色背景，页面自身处理布局
- 管理端：`AdminLayout` 自管理白色背景 + 侧边栏
- 移除 `min-height: 100vh; display: flex; justify-content: center; align-items: center;`

各页面自行处理容器样式。

### Login.vue — 登录/注册

从当前的简单白色卡片改造为：
- 页面背景：紫色渐变 `#667eea → #764ba2`，全屏高度居中
- 卡片：白色，`border-radius: 16px`，`box-shadow: 0 8px 32px rgba(0,0,0,0.15)`，宽度 360px
- 顶部 Logo 区：圆角渐变图标（📚） + "自习座位预约" 标题 + "校园智能自习管理" 副标题
- 输入框：`border-radius: 10px`，`border: 1.5px solid #e0e0e0`，focus 时紫色边框
- 登录按钮：渐变紫色 `#667eea → #764ba2`，`border-radius: 10px`，白色文字
- 注册链接文字改为 `#667eea`
- 注册弹窗 Modal：白色圆角卡片，居中弹出

### Home.vue — 首页座位浏览

改造为完整页面布局（非居中卡片）：
- **顶部导航栏**：白色背景，左侧标题"📚 自习座位预约"，右侧两个圆角按钮 — "🤖 AI 助手"（渐变紫色）和"👤 我的"（灰色）
- **区域筛选栏**：页面内，下拉选择框带圆角和边框
- **座位网格**：5 列网格，座位块 `aspect-ratio: 1`，圆角 10px，根据状态变色
  - 空闲：`#e6f7ee` 背景 + `#52c41a` 边框 + "可预约"文字
  - 已预约：`#fff1e6` + `#fa8c16`
  - 占用：`#f0f0f0` + `#d9d9d9`
  - 暂离：`#e6f7ff` + `#1890ff`
  - 禁用：`#fff2f0` + `#ff4d4f`
- **底部图例**：居中，简单文字 + 色块
- **底部座位详情卡片**：白色圆角，选中座位时展示，含"预约"按钮
- 点击"A I 助手"跳转 `/ai-assistant`，点击"我的"跳转 `/my-reservations`

### SeatSelection.vue — 预约确认页

- 页面背景：`#f5f6fa`
- **顶部**：返回按钮 + "确认预约"标题
- **座位信息卡片**：白色圆角卡片，左右分两列 — 座位号（A-01，大字体粗体）+ 区域（A区 · 1F）
- **日期选择**：横向滚动，每天一个圆角方块，选中态渐变紫色，未选中白色带边框
- **时段选择**：3 列网格，每个时段带状态 — 可选（白色边框）、已选（紫色填充）、不可选（灰色置灰）、推荐（绿色边框）
- **确认按钮**：底部固定或随内容，渐变紫色全宽按钮

### MyReservations.vue — 我的预约/个人中心

- 页面背景：`#f5f6fa`
- **顶部 Tab**：白色背景，三个等宽 Tab（当前预约 / 历史预约 / 个人中心），选中态底部紫色下划线 + 紫色文字
- **预约卡片**：白色圆角卡片列表，每张卡片包含：
  - 左侧：座位号（大字体） + 状态 badge（彩色圆角标签） + 日期时间 + 位置
  - 右侧：操作按钮组（暂离蓝色、结束橙色、取消红色），小圆角按钮
  - 状态 badge 颜色：已开始绿色、已预约橙色、暂离蓝色、已结束灰色、爽约红色、已取消红色
- **个人中心 Tab**：白色圆角卡片展示用户信息（学号、姓名、违规次数含颜色标识、账号状态）
- **退出登录按钮**：红色全宽按钮

### 路由结构调整

```
/                  → Login.vue
/home              → Home.vue
/seat-selection    → SeatSelection.vue
/my-reservations   → MyReservations.vue
/ai-assistant      → ChatPage.vue       (新增)
/admin/dashboard   → AdminLayout + Dashboard  (新增)
/admin/areas       → AdminLayout + AreaManage  (新增)
/admin/seats       → AdminLayout + SeatManage  (新增)
/admin/users       → AdminLayout + UserManage  (新增)
```

---

## 整体文件变更汇总

### 新建文件

| File | Feature |
|------|---------|
| `backend/.../controller/AdminController.java` | Admin |
| `backend/.../service/AdminService.java` | Admin |
| `backend/.../service/impl/AdminServiceImpl.java` | Admin |
| `backend/.../config/AdminAuthInterceptor.java` | Admin |
| `backend/.../controller/ChatController.java` | AI |
| `backend/.../service/ChatService.java` | AI |
| `backend/.../service/impl/ChatServiceImpl.java` | AI |
| `backend/.../config/DeepSeekConfig.java` | AI |
| `frontend/src/views/admin/AdminLayout.vue` | Admin |
| `frontend/src/views/admin/Dashboard.vue` | Admin |
| `frontend/src/views/admin/AreaManage.vue` | Admin |
| `frontend/src/views/admin/SeatManage.vue` | Admin |
| `frontend/src/views/admin/UserManage.vue` | Admin |
| `frontend/src/views/ChatPage.vue` | AI |

### 修改文件

| File | Change |
|------|--------|
| `backend/.../entity/User.java` | Add `role` field |
| `backend/.../controller/UserController.java` | Include `role` in login response |
| `backend/.../service/DataInitializer.java` | Insert preset admin account |
| `backend/.../config/WebConfig.java` | Register admin auth interceptor |
| `backend/.../resources/application.yml` | Add deepseek config |
| `frontend/src/App.vue` | Remove centered flex/gradient; delegate to pages |
| `frontend/src/router/index.js` | Add admin routes + ai-assistant route |
| `frontend/src/views/Login.vue` | Redesign with gradient bg, rounded card, logo area |
| `frontend/src/views/Home.vue` | Redesign with top nav, AI button, grid layout |
| `frontend/src/views/SeatSelection.vue` | Redesign with info card, date chips, time grids |
| `frontend/src/views/MyReservations.vue` | Redesign with tab bar, card list, colored badges |

---

## 测试要点

### 学生端重设计
- 手动验证：登录页 → 首页座位浏览 → 点击座位进入预约页 → 选择日期时段 → 确认预约 → 我的预约页查看
- 验证 AI 助手入口从首页可进入
- 验证底部导航/顶部按钮跳转正确
- 验证各状态座位颜色正确显示

### 管理端
- AdminService 单测：CRUD area/seat, release seat, ban user, reset violations
- AdminController 集成测：非 admin 返回 401，各 endpoint 正确响应
- 手动流程：登录 admin → 仪表盘 → 创建区域 → 添加座位 → 封禁用户 → 以该用户登录验证

### AI 智能体
- ChatService 单测：mock DeepSeek 响应，验证 function call 执行正确
- ChatController 集成测：验证对话流程（查座位 → 预约 → 返回）
- 手动流程：登录学生 → 进入 AI 助手 → "帮我查明天下午的空位" → 选择座位 → 确认预约
- 自习计划场景："帮我规划下周高数学习计划" → 确认 → 验证多天预约全部创建
