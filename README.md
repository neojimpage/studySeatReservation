# 校园自习座位预约系统（学生端）

一个面向高校学生的轻量级自习座位预约系统 Demo。本阶段只做**学生端**（不包含管理端/后台页面）。

## 目标与范围（MVP）

- **支持的核心流程**：选区域/楼层 → 看座位状态 → 选择时段预约 → 到场占用/暂离 → 结束/超时释放 → 查看记录
- **不在本阶段范围**：管理端、扫码/人脸签到、复杂审批、多校区多馆联动、支付/积分等

## 功能清单（学生端）

- **座位状态查看**：按楼层/区域查看座位（空闲/已预约/占用/暂离）
- **时段预约**：选择日期与时段（可配置规则：提前几天、每段时长、可预约次数等）
- **取消预约**：未开始的预约可取消
- **暂离**：暂离保留座位（例如 30 分钟），超时自动释放
- **预约记录**：当前预约、历史预约
- **违规限制（可选）**：累计违规达到阈值，限制次日预约

## 技术栈（建议）

- **后端**：Java 17、Spring Boot 3、MyBatis-Plus、MySQL 8
- **前端**：Vue 3、Vite、Element Plus（或纯 HTML/CSS/JS 也可）
- **接口风格**：RESTful + JSON

## 目录结构（前后端同目录）

```text
studySeat/
├─ backend/                 # Spring Boot 后端（仅学生端接口）
│  ├─ src/main/java/...      # controller/service/mapper/entity...
│  └─ src/main/resources/
│     └─ application.yml
├─ frontend/                # Vue 前端（学生端页面）
│  ├─ src/
│  └─ package.json
├─ docs/                    # 需求/原型/接口文档/测试用例（可选）
└─ README.md
```

## 快速开始（本地开发）

### 前置条件

- **JDK**：17+
- **Maven**：3.8+
- **MySQL**：8.0+
- **Node.js**：18+（推荐）

### 1) 数据库准备

创建数据库（示例名）：`campus_seat_db`，字符集：`utf8mb4`。

### 2) 启动后端

在 `backend/src/main/resources/application.yml` 配置数据库连接（示例）：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_seat_db?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
server:
  port: 8080
```

运行后端：

```bash
cd backend
mvn spring-boot:run
```

后端默认地址：`http://localhost:8080`

### 3) 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认地址：`http://localhost:5173`

## 学生端接口约定（Demo 级）

> 下面是 README 里用于对齐前后端的**最小接口清单**，实现时可按实际命名调整。

### 座位与布局

- `GET /api/student/areas`：区域/楼层列表
- `GET /api/student/seats?areaId=...`：座位列表（含状态）

### 预约

- `POST /api/student/reservations`：创建预约（`seatId`、`startTime`、`endTime`）
- `GET /api/student/reservations/current`：当前有效预约
- `GET /api/student/reservations`：预约记录（分页/筛选可选）
- `POST /api/student/reservations/{id}/cancel`：取消未开始预约

### 暂离/结束

- `POST /api/student/reservations/{id}/leave`：暂离（开始计时）
- `POST /api/student/reservations/{id}/back`：返回（结束暂离）
- `POST /api/student/reservations/{id}/finish`：结束使用（可选）

## 约定与规范（建议）

- **统一返回结构**：`{ code, message, data }`
- **时间字段**：统一使用 ISO-8601（例如 `2026-03-09T19:00:00`）
- **跨域**：本地开发允许 `http://localhost:5173` 访问 `http://localhost:8080`

## License

MIT（仅学习/实训用途）

