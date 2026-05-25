# 校园自习座位预约系统

一个面向高校学生的自习座位预约系统，包含**学生端**、**管理端**和 **AI 对话智能体**。

## 功能清单

### 学生端
- **座位状态查看**：按楼层/区域查看座位（空闲 / 已预约 / 占用 / 暂离），可视化网格展示
- **时段预约**：选择日期与 2 小时时段，每日最多 2 个时段，未来 7 天内可预约
- **取消预约**：预约开始前 30 分钟可取消
- **暂离 / 返回**：暂离保留座位 30 分钟，超时自动释放
- **结束使用**：手动结束，释放座位
- **预约记录**：当前预约、历史预约分 Tab 查看
- **违规限制**：爽约累计 3 次自动限制次日预约，次日 24 点后解除

### 管理端
- **仪表盘**：总座位数、空闲座位、今日预约量、违规用户数统计卡片
- **区域管理**：新增 / 编辑 / 启用 / 禁用自习区域
- **座位管理**：新增 / 编辑 / 启用 / 禁用座位，手动释放占用中的座位
- **用户管理**：查看用户列表，封禁 / 解封用户，重置违规次数

### AI 自习助手
- **自然语言预约**：对话式查座位、预约、取消，AI 自动调用后端接口
- **自习计划规划**：输入空闲时间和目标，AI 推荐一周学习计划并一键批量预约
- **对话记忆**：SQLite 持久化，按会话隔离，支持多轮对话
- **底层模型**：DeepSeek API，LangChain + LangGraph React Agent

---

## 技术架构

```
┌─────────────────────────────────────────────────────┐
│                    Vue 3 前端 (:5173)                 │
│  Login / Home / SeatSelection / MyReservations       │
│  AdminLayout / Dashboard / Areas / Seats / Users    │
│  ChatPage (AI 对话)                                  │
└──────────┬──────────────────────────────┬───────────┘
           │                              │
    学生端 API (/api/student/*)     管理端 API (/api/admin/*)
           │                              │
           ▼                              ▼
┌─────────────────────────────────────────────────────┐
│              Java Spring Boot 3 (:8080)              │
│  MyBatis-Plus / MySQL 8 / RESTful API               │
│  AdminController · UserController · SeatController  │
│  ChatController (代理转发 → Python)                  │
└──────────────────────┬──────────────────────────────┘
                       │
               POST /ai/chat
                       │
                       ▼
┌─────────────────────────────────────────────────────┐
│           Python FastAPI (:8000)                     │
│  LangChain + LangGraph + DeepSeek API               │
│  React Agent · @tool ×5 · SQLite 对话记忆           │
└─────────────────────────────────────────────────────┘
```

## 技术栈

| 层 | 技术 |
|----|------|
| **后端** | Java 17, Spring Boot 3.1.5, MyBatis-Plus 3.5, MySQL 8 |
| **AI 服务** | Python 3.10+, FastAPI, LangChain, LangGraph, DeepSeek API |
| **前端** | Vue 3, Vite 5, Vue Router 4 |
| **工具** | Maven, npm, Uvicorn |

## 快速开始

### 前置条件

- **JDK** 17+
- **Maven** 3.8+
- **MySQL** 8.0+
- **Node.js** 18+
- **Python** 3.10+

### 1) 数据库

创建数据库 `campus_seat_db`，字符集 `utf8mb4`。启动后系统会自动建表（MyBatis-Plus）。

### 2) 启动后端 (Java)

```bash
cd backend
# 修改 src/main/resources/application.yml 中的数据库连接信息
mvn spring-boot:run
```

后端地址：`http://localhost:8080`

预设管理员账号：`admin` / `admin123`

### 3) 启动 AI 服务 (Python)

```bash
cd ai-service
# 配置 DeepSeek API Key
cp .env.example .env  # 编辑 .env 填入 DEEPSEEK_API_KEY
pip install -r requirements.txt
python main.py
```

AI 服务地址：`http://localhost:8000`
API 文档：`http://localhost:8000/docs`

### 4) 启动前端 (Vue)

```bash
cd frontend
npm install
npm run dev
```

前端地址：`http://localhost:5173`

## 学生端接口

Base: `/api/student`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/login` | 登录（返回 role 字段区分学生/管理员） |
| POST | `/register` | 注册 |
| GET | `/areas` | 区域列表 |
| GET | `/seats?areaId=` | 按区域查座位 |
| GET | `/seats/{id}` | 座位详情 |
| POST | `/reservations` | 创建预约 |
| GET | `/reservations/current` | 当前有效预约 |
| GET | `/reservations` | 全部预约记录 |
| POST | `/reservations/{id}/cancel` | 取消预约 |
| POST | `/reservations/{id}/leave` | 暂离 |
| POST | `/reservations/{id}/back` | 返回 |
| POST | `/reservations/{id}/finish` | 结束使用 |
| POST | `/chat` | AI 对话（代理到 Python 服务） |

## 管理端接口

Base: `/api/admin`（需携带 `X-User-Role: admin` 请求头）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/dashboard` | 仪表盘统计 |
| GET | `/areas` | 区域列表 |
| POST | `/areas` | 新增区域 |
| PUT | `/areas/{id}` | 编辑区域 |
| PUT | `/areas/{id}/disable` | 启用/禁用区域 |
| GET | `/seats` | 座位列表 |
| POST | `/seats` | 新增座位 |
| PUT | `/seats/{id}` | 编辑座位 |
| PUT | `/seats/{id}/disable` | 启用/禁用座位 |
| PUT | `/seats/{id}/release` | 手动释放座位 |
| GET | `/users` | 用户列表 |
| PUT | `/users/{id}/ban` | 封禁/解封用户 |
| PUT | `/users/{id}/violation-reset` | 重置违规次数 |

## AI 服务接口

Base: `http://localhost:8000`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/ai/chat` | AI 对话（由 Java ChatController 代理调用） |

## 项目结构

```
studySeat/
├── backend/                     # Spring Boot 后端
│   ├── src/main/java/com/studyseat/
│   │   ├── config/              # DataInitializer, WebMvcConfig, AdminAuthInterceptor
│   │   ├── controller/          # AdminController, ChatController, UserController, ...
│   │   ├── dto/                 # ChatRequest
│   │   ├── entity/              # User, Area, Seat, Reservation
│   │   ├── mapper/              # MyBatis-Plus Mapper 接口
│   │   └── service/             # 业务逻辑 + AdminService
│   └── pom.xml
├── ai-service/                  # Python AI 微服务
│   ├── main.py                  # FastAPI 入口
│   ├── agent.py                 # LangChain Agent + 记忆
│   ├── tools.py                 # @tool 函数 (HTTP 回调 Java)
│   ├── config.py                # 配置 (DeepSeek Key)
│   └── requirements.txt
├── frontend/                    # Vue 3 前端
│   ├── src/
│   │   ├── views/               # 页面组件
│   │   │   ├── Login.vue        # 登录/注册
│   │   │   ├── Home.vue         # 首页座位浏览
│   │   │   ├── SeatSelection.vue # 预约确认
│   │   │   ├── MyReservations.vue # 我的预约
│   │   │   ├── ChatPage.vue     # AI 助手对话页
│   │   │   └── admin/           # 管理端页面 (5个)
│   │   ├── router/index.js      # 路由配置
│   │   └── App.vue              # 根组件
│   └── package.json
└── docs/                        # 设计文档
    └── superpowers/
        ├── specs/               # 设计 Spec
        └── plans/               # 实现计划
```

## 统一返回格式

```json
{ "code": 200, "message": "操作成功", "data": {...} }
```

## License

MIT（仅学习/实训用途）
