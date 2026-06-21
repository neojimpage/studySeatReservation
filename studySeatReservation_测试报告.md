# 校园自习座位预约系统 — 测试报告

> 项目地址: https://github.com/neojimpage/studySeatReservation  
> 报告日期: 2026-06-21  
> 项目类型: 高校课程实训项目（6周实训版）

---

## 目录

1. [项目概览](#1-项目概览)
2. [现有测试现状分析](#2-现有测试现状分析)
3. [安全测试分析](#3-安全测试分析)
4. [功能模块测试评估](#4-功能模块测试评估)
5. [代码质量问题](#5-代码质量问题)
6. [数据库与配置审查](#6-数据库与配置审查)
7. [推荐的测试策略](#7-推荐的测试策略)
8. [测试用例设计](#8-测试用例设计)
9. [综合评估](#9-综合评估)

---

## 1. 项目概览

### 1.1 基本信息

| 项目 | 详情 |
|------|------|
| **项目名称** | 校园自习座位预约系统 |
| **仓库地址** | https://github.com/neojimpage/studySeatReservation |
| **代码规模** | Java 51,355 行 · Vue 46,466 行 · Python 8,369 行 · JS 1,822 行 · HTML 300 行 |
| **技术栈** | Java 8+ (Spring Boot 2.x + MyBatis Plus) · Vue 3 + Vite · Python (FastAPI) |
| **数据库** | MySQL 8.x (`campus_seat_db`, 端口 3306) |
| **后端端口** | 8080 |
| **AI 服务端口** | 8000 |
| **构建工具** | Maven (mvnw) |
| **最后提交** | 2026-06-21 |

### 1.2 系统架构

```
┌──────────────────────────────────────────────────────────────┐
│                    Frontend (Vue 3 + Vite)                   │
│                                                              │
│   Login.vue → Home.vue → SeatSelection.vue → MyReservations.vue → ChatPage.vue   │
│                              │                               │
│                    Admin (AdminLayout)                       │
│          Dashboard │ AreaManage │ SeatManage │ UserManage     │
├──────────────────────────────────────────────────────────────┤
│                Backend (Spring Boot :8080)                   │
│                                                              │
│   Interceptor (AdminAuth) → Controller → Service → Mapper    │
│           │                      │            │              │
│           ▼                      ▼            ▼              │
│      WebMvcConfig          ServiceImpl    MyBatis Plus       │
│                                 │                            │
│                                 ▼                            │
│                          MySQL (campus_seat_db)              │
├──────────────────────────────────────────────────────────────┤
│                AI Service (Python :8000)                     │
│                                                              │
│         main.py → agent.py → tools.py                        │
│                    config.py (.env)                          │
└──────────────────────────────────────────────────────────────┘
```

### 1.3 后端代码结构

```
backend/src/main/java/com/studyseat/
├── Application.java              # Spring Boot 入口
├── config/
│   ├── AdminAuthInterceptor.java # 管理员权限拦截器
│   ├── DataInitializer.java      # 数据初始化
│   └── WebMvcConfig.java         # Web MVC 配置
├── controller/
│   ├── AdminController.java      # 管理后台 API (7093B)
│   ├── AreaController.java       # 区域管理 API (1298B)
│   ├── ChatController.java       # AI 对话 API (2131B)
│   ├── ReservationController.java# 预约管理 API (5637B)
│   ├── SeatController.java       # 座位管理 API (1475B)
│   └── UserController.java       # 用户管理 API (3026B)
├── dto/
│   └── ChatRequest.java          # 对话请求 DTO
├── entity/
│   ├── Area.java                 # 区域实体
│   ├── Reservation.java          # 预约实体
│   ├── Seat.java                 # 座位实体
│   └── User.java                 # 用户实体
├── mapper/
│   ├── AreaMapper.java           # 区域 DAO
│   ├── ReservationMapper.java    # 预约 DAO
│   ├── SeatMapper.java           # 座位 DAO
│   └── UserMapper.java           # 用户 DAO
└── service/
    ├── AdminService.java
    ├── AreaService.java
    ├── ReservationService.java
    ├── SeatService.java
    ├── UserService.java
    └── impl/
        ├── AdminServiceImpl.java
        ├── AreaServiceImpl.java
        ├── ReservationServiceImpl.java
        ├── SeatServiceImpl.java
        └── UserServiceImpl.java
```

### 1.4 前端代码结构

```
frontend/src/
├── App.vue                    # 根组件
├── main.js                    # 入口 JS
├── router/
│   └── index.js               # 路由配置 (1420B)
└── views/
    ├── Login.vue              # 登录页 (4990B)
    ├── Home.vue               # 首页 (5563B)
    ├── SeatSelection.vue      # 选座页 (5826B)
    ├── MyReservations.vue     # 我的预约 (7561B)
    ├── ChatPage.vue           # AI 对话页 (5264B)
    └── admin/
        ├── AdminLayout.vue    # 管理布局 (1861B)
        ├── Dashboard.vue      # 仪表盘 (1631B)
        ├── AreaManage.vue     # 区域管理 (4667B)
        ├── SeatManage.vue     # 座位管理 (6174B)
        └── UserManage.vue     # 用户管理 (2583B)
```

---

## 2. 现有测试现状分析

### 2.1 单元测试状况 — **极其薄弱**

后端测试目录仅存在 **1 个测试文件**：

```
backend/src/test/java/com/example/backen/BackenApplicationTests.java
```

#### 严重问题

| # | 问题 | 严重程度 | 说明 |
|---|------|----------|------|
| 1 | **测试包名不匹配** | 🔴 严重 | 测试包路径 `com.example.backen`，主代码路径 `com.studyseat`，spring boot test 无法正确扫描组件 |
| 2 | **包名拼写错误** | 🟡 一般 | `backen` 应为 `backend` |
| 3 | **覆盖率极低** | 🔴 严重 | 51K 行 Java 代码仅 1 个测试，预估覆盖率 < 1% |
| 4 | **前端无测试** | 🔴 严重 | 无 Vitest/Jest 配置，无任何 `.test.js`/`.spec.js` |
| 5 | **AI 服务无测试** | 🟡 一般 | Python 代码无 `test_*.py` 或 `*_test.py` |

### 2.2 各模块测试覆盖率

| 模块 | 文件数 | 测试文件 | 测试用例 | 覆盖率(估计) | 状态 |
|------|--------|----------|----------|-------------|------|
| Controller 层 | 6 | 0 | 0 | 0% | ❌ 未覆盖 |
| Service 层 | 10 (5接口+5实现) | 0 | 0 | 0% | ❌ 未覆盖 |
| Mapper 层 | 4 | 0 | 0 | 0% | ❌ 未覆盖 |
| Entity 层 | 4 | 0 | 0 | 0% | ❌ 未覆盖 |
| Config 层 | 3 | 0 | 0 | 0% | ❌ 未覆盖 |
| Vue 组件 | 10 | 0 | 0 | 0% | ❌ 未覆盖 |
| Python AI 服务 | 4 | 0 | 0 | 0% | ❌ 未覆盖 |
| **总计** | **41** | **1** | **~1** | **<1%** | ❌ |

### 2.3 缺失的测试基础设施

- [ ] 未配置 H2/HSQLDB 内存数据库用于测试
- [ ] 无 `application-test.yml` 测试环境配置
- [ ] 无 Mockito/ PowerMock 依赖（虽然 Spring Boot 自带）
- [ ] 前端无 Vitest / Jest / Vue Test Utils 依赖
- [ ] 前端 `package.json` 中无测试脚本
- [ ] AI 服务 `requirements.txt` 中无 pytest 依赖
- [ ] 无 CI/CD 流水线（GitHub Actions / Jenkinsfile）

---

## 3. 安全测试分析

### 3.1 已识别的安全隐患

#### 🔴 高风险

| # | 问题 | 位置 | 说明 | 修复建议 |
|---|------|------|------|----------|
| 1 | **数据库密码明文提交** | `application.yml:4` | `password: 123456` — 弱密码，且明文在 Git 中 | 使用 `${DB_PASSWORD}` 环境变量；轮换当前密码 |
| 2 | **Spring Security 弱凭据** | `application.yml:9-10` | `name: user` / `password: password` — 默认凭据 | 修改为强密码并使用环境变量 |
| 3 | **用户密码疑似明文存储** | `User.java:13` | `private String password` — 未见 `@PasswordEncoder` 或 BCrypt 加密 | 引入 `BCryptPasswordEncoder`，存储哈希 |
| 4 | **`.env` 文件泄露** | `ai-service/.env` | 被提交到 Git，可能含 API Key | 立即从 Git 历史中清除，加入 `.gitignore` |
| 5 | **SSL 禁用** | `application.yml:2` | `useSSL=false` | 生产环境启用 SSL |

#### 🟡 中风险

| # | 问题 | 说明 | 修复建议 |
|---|------|------|----------|
| 6 | **无令牌认证机制** | 未发现 JWT/Token 配置，可能仅依赖 Session | 引入 Spring Security + JWT 无状态认证 |
| 7 | **CORS 策略未确认** | `WebMvcConfig.java` 内容未审计 | 明确配置允许的 Origin 列表 |
| 8 | **无请求限流** | 登录/预约接口无频率限制 | 引入 Rate Limiter 或 Guava RateLimiter |
| 9 | **接口无输入校验注解** | 未确认是否使用 `@Valid` / `@NotBlank` 等 | 添加 Bean Validation 注解 |

#### 🟢 低风险

| # | 问题 | 说明 |
|---|------|------|
| 10 | 无登录失败次数限制 | 存在暴力破解风险 |
| 11 | 无操作审计日志 | 无法追溯管理员操作 |

### 3.2 安全测试检查清单

| 测试项 | 状态 | 说明 |
|--------|------|------|
| 未登录访问 API | ❌ 未测 | 应返回 401 |
| 学生访问管理后台 | ❌ 未测 | 应返回 403 |
| SQL 注入（登录表单） | ❌ 未测 | `' OR 1=1 --` |
| XSS（AI 对话输入） | ❌ 未测 | `<script>alert(1)</script>` |
| CSRF 防护 | ❌ 未测 | 修改请求 Referer |
| 密码强度校验 | ❌ 未测 | 弱密码注册 |
| 并发登录 | ❌ 未测 | 同账号多设备登录 |
| API 限流 | ❌ 未测 | 高频请求 |

---

## 4. 功能模块测试评估

### 4.1 用户模块 (`UserController.java` · 3026B)

**核心功能:**
- 用户注册（学号、姓名、密码）
- 用户登录（学号+密码验证）
- 个人信息查询与修改
- 角色管理（student / admin）

**测试要点:**

| # | 测试场景 | 预期结果 | 优先级 |
|---|----------|----------|--------|
| 1 | 正常注册新用户 | 200，返回用户信息 | P0 |
| 2 | 重复学号注册 | 409 Conflict | P0 |
| 3 | 空学号/密码注册 | 400 Bad Request | P0 |
| 4 | 正确学号+密码登录 | 200，返回用户信息+Token | P0 |
| 5 | 错误密码登录 | 401 Unauthorized | P0 |
| 6 | 不存在学号登录 | 401 Unauthorized | P1 |
| 7 | 学生角色访问管理接口 | 403 Forbidden | P0 |
| 8 | 违规次数≥3时状态变更为限制 | 自动更新 `isRestricted=true` | P1 |

**边界条件:**
- 学号长度边界（空、1位、最大长度、超长）
- 密码长度边界（空、1位、超长）
- 姓名为空或特殊字符
- 中文字符编码处理

### 4.2 预约模块 (`ReservationController.java` · 5637B)

**核心功能:**
- 创建座位预约
- 取消预约
- 预约签到
- 预约记录查询

**测试要点:**

| # | 测试场景 | 预期结果 | 优先级 |
|---|----------|----------|--------|
| 1 | 预约空闲座位 | 200，座位状态变为已预约 | P0 |
| 2 | 预约已被占用的座位 | 409 或特定错误码 | P0 |
| 3 | 取消未开始的预约 | 200，座位释放 | P0 |
| 4 | 取消已过期的预约 | 400 或不允许 | P1 |
| 5 | 预约签到（在有效时段内） | 200，状态变为已签到 | P1 |
| 6 | 超时未签到 | 自动取消预约 | P2 |
| 7 | 查询个人预约列表 | 200，仅返回当前用户数据 | P1 |
| 8 | 管理员查看所有预约 | 200，返回所有预约 | P1 |

**关键并发测试:**

```
线程A: 预约座位X (时间T) ──→ 成功
线程B: 预约座位X (时间T) ──→ 失败（已占用）

线程A: 预约座位X ──→ 成功
线程B: 取消预约座位X ──→ 成功  
线程C: 预约座位X ──→ 成功（座位已释放）
```

### 4.3 AI 对话模块 (`ChatController.java` · 2131B)

**测试要点:**

| # | 测试场景 | 预期结果 | 优先级 |
|---|----------|----------|--------|
| 1 | 发送正常对话消息 | 200，返回 AI 回复 | P1 |
| 2 | AI 服务不可用 (8000端口不通) | 500 或降级提示 | P1 |
| 3 | 发送超长消息 | 400 或截断处理 | P2 |
| 4 | 空消息 | 400 Bad Request | P2 |
| 5 | 特殊字符消息 | 正常处理 | P2 |

### 4.4 管理后台模块 (`AdminController.java` · 7093B)

**测试要点:**

| # | 测试场景 | 预期结果 | 优先级 |
|---|----------|----------|--------|
| 1 | 管理员增加区域 | 200 | P0 |
| 2 | 管理员删除有座位的区域 | 应提示先删除子座位 | P0 |
| 3 | 管理员增加座位 | 200 | P0 |
| 4 | 管理员删除已预约座位 | 应提示冲突 | P1 |
| 5 | 管理员封禁/解封用户 | 200，状态变更 | P1 |
| 6 | 非管理员访问管理接口 | 403 Forbidden | P0 |
| 7 | 仪表盘统计数据 | 数据准确 | P2 |

### 4.5 前端页面测试要点

| 页面 | 测试点 |
|------|--------|
| **Login.vue** | 表单校验、错误提示、登录跳转、记住密码 |
| **Home.vue** | 区域列表加载、筛选搜索、导航跳转 |
| **SeatSelection.vue** | 座位状态（空闲/已占/禁用）展示正确、点击预约、时段选择 |
| **MyReservations.vue** | 列表加载、取消预约按钮、签到按钮、状态标签 |
| **ChatPage.vue** | 消息发送、回复展示、滚动加载、长消息截断 |
| **AdminLayout.vue** | 侧边栏导航、未登录拦截 |
| **AreaManage.vue** | CRUD 表单、删除确认对话框 |
| **SeatManage.vue** | 座位列表、批量操作 |
| **UserManage.vue** | 用户列表、封禁操作 |

---

## 5. 代码质量问题

### 5.1 结构性问题

| # | 问题 | 严重程度 | 位置 | 说明 |
|---|------|----------|------|------|
| 1 | 测试包名与主代码不匹配 | 🔴 高 | `test/java/com/example/backen` | 应为 `com/studyseat` |
| 2 | `node_modules` 已提交 | 🟡 中 | `frontend/node_modules/` | 应通过 `.gitignore` 排除 |
| 3 | `.idea/` 已提交 | 🟡 低 | `.idea/` | IDE 配置不应提交 |
| 4 | `package-lock.json` 存在但无 lock 文件一致性校验 | 🟢 低 | `frontend/` | 建议使用 `npm ci` |
| 5 | `node_modules` 目录出现在 Git tree 中 | 🟡 中 | GitHub API 返回了该目录 | 磁盘占用大 |

### 5.2 缺失文件

| # | 缺失文件 | 用途 | 优先级 |
|---|----------|------|--------|
| 1 | `.env.example` | 环境变量模板 | 🔴 高 |
| 2 | `docker-compose.yml` | 容器化部署 | 🟡 中 |
| 3 | `frontend/.env` / `.env.production` | 前端环境变量 | 🟡 中 |
| 4 | `.github/workflows/test.yml` | CI 自动化测试 | 🟡 中 |
| 5 | `Dockerfile` (backend/frontend) | 容器化 | 🟢 低 |
| 6 | `backend/src/test/resources/application-test.yml` | 测试环境配置 | 🔴 高 |
| 7 | 前端 `vitest.config.js` / `jest.config.js` | 前端测试配置 | 🔴 高 |

---

## 6. 数据库与配置审查

### 6.1 数据库配置 (`application.yml`)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_seat_db
    username: root                    # ⚠️ 使用 root 账户
    password: 123456                  # ⚠️ 弱密码，明文存储
    driver-class-name: com.mysql.cj.jdbc.Driver
  security:
    user:
      name: user                      # ⚠️ 默认用户名
      password: password              # ⚠️ 默认密码
server:
  port: 8080
mybatis-plus:
  mapper-locations: classpath*:mapper/**/*.xml
  type-aliases-package: com.studyseat.entity
ai-service:
  url: http://localhost:8000
```

**审查意见:**

| 配置项 | 当前值 | 建议值 |
|--------|--------|--------|
| 数据库用户 | `root` | 专用用户 `studyseat_app` |
| 数据库密码 | 明文 `123456` | 环境变量 `${DB_PASSWORD}` |
| Security 用户 | `user` | 禁用或改为强用户名 |
| Security 密码 | `password` | 环境变量 `${SECURITY_PASSWORD}` |
| SSL | `false` | 生产环境改为 `true` |

### 6.2 实体字段审查 (`User.java`)

```java
@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String studentId;
    private String password;           // ⚠️ 未见加密处理
    private String name;
    private Integer violationCount;
    private Boolean isRestricted;
    private String role;               // "student" or "admin"
    private LocalDateTime lastLoginTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

**审查意见:**
- `password` 字段应存储 BCrypt 哈希，不应存明文
- `role` 建议改为枚举类型而非字符串
- `violationCount` 的建议默认值为 0
- 建议增加 `email`、`phone` 字段用于通知

---

## 7. 推荐的测试策略

### 7.1 测试金字塔

```
           ╱  E2E 测试  ╲               ← 少量 (5-10个核心流程)
          ╱  集成测试    ╲              ← 适量 (20-30个)
         ╱   组件测试     ╲             ← 适量 (30-50个)
        ╱    单元测试      ╲            ← 大量 (100+ 个)
       ─────────────────────
```

### 7.2 分层测试目标

| 层级 | 目标覆盖率 | 推荐工具 | 预估用例数 | 优先级 |
|------|-----------|----------|-----------|--------|
| Service 层 | ≥ 80% | JUnit 5 + Mockito | 60+ | 🔴 P0 |
| Controller 层 | ≥ 70% | MockMvc / WebTestClient | 40+ | 🔴 P0 |
| Mapper 层 | ≥ 60% | @MybatisPlusTest + H2 | 20+ | 🟡 P1 |
| Vue 组件 | ≥ 50% | Vitest + Vue Test Utils | 30+ | 🟡 P1 |
| AI Service | ≥ 60% | pytest + httpx | 15+ | 🟡 P1 |
| E2E | 核心流程 | Playwright / Cypress | 8+ | 🟢 P2 |

### 7.3 后端测试环境搭建

**第一步：添加测试依赖**

```xml
<!-- pom.xml 补充 -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

**第二步：创建测试配置**

```yaml
# backend/src/test/resources/application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=MySQL
    driver-class-name: org.h2.Driver
  sql:
    init:
      mode: always
```

**第三步：修正测试包结构**

```bash
# 修正：将测试从 com.example.backen 移动到 com.studyseat
mv backend/src/test/java/com/example/backen \
   backend/src/test/java/com/studyseat
```

### 7.4 前端测试环境搭建

```bash
cd frontend
npm install -D vitest @vue/test-utils jsdom
```

```javascript
// vitest.config.js
import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  test: {
    environment: 'jsdom',
  },
})
```

### 7.5 AI 服务测试环境搭建

```bash
cd ai-service
pip install pytest pytest-asyncio httpx
```

---

## 8. 测试用例设计

### 8.1 预约服务单元测试示例

```java
@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationMapper reservationMapper;

    @Mock
    private SeatMapper seatMapper;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    @Test
    @DisplayName("预约空闲座位 → 成功")
    void shouldCreateReservationWhenSeatAvailable() {
        // Given
        Long userId = 1L;
        Long seatId = 100L;
        Seat seat = new Seat();
        seat.setId(seatId);
        seat.setStatus("available");

        when(seatMapper.selectById(seatId)).thenReturn(seat);
        when(reservationMapper.insert(any())).thenReturn(1);

        // When
        Reservation result = reservationService.createReservation(userId, seatId, startTime, endTime);

        // Then
        assertNotNull(result);
        assertEquals(seatId, result.getSeatId());
        verify(seatMapper).updateStatus(seatId, "occupied");
    }

    @Test
    @DisplayName("预约已占用座位 → 抛出异常")
    void shouldThrowWhenSeatOccupied() {
        // Given
        Seat seat = new Seat();
        seat.setStatus("occupied");
        when(seatMapper.selectById(anyLong())).thenReturn(seat);

        // When & Then
        assertThrows(BusinessException.class, () ->
            reservationService.createReservation(1L, 100L, startTime, endTime)
        );
    }

    @Test
    @DisplayName("违规用户无法预约")
    void shouldRejectRestrictedUser() {
        // Given
        User user = new User();
        user.setIsRestricted(true);
        when(userMapper.selectById(1L)).thenReturn(user);

        // When & Then
        assertThrows(BusinessException.class, () ->
            reservationService.createReservation(1L, 100L, startTime, endTime)
        );
    }

    @Test
    @DisplayName("时段冲突检测 → 拒绝重叠预约")
    void shouldDetectTimeSlotConflict() {
        // Given
        Reservation existing = new Reservation();
        existing.setSeatId(100L);
        existing.setStartTime(LocalDateTime.of(2026, 6, 21, 10, 0));
        existing.setEndTime(LocalDateTime.of(2026, 6, 21, 12, 0));

        when(reservationMapper.findConflicting(anyLong(), any(), any()))
            .thenReturn(List.of(existing));

        // When & Then
        assertThrows(TimeConflictException.class, () ->
            reservationService.createReservation(1L, 100L,
                LocalDateTime.of(2026, 6, 21, 11, 0),  // 与现有预约重叠
                LocalDateTime.of(2026, 6, 21, 13, 0))
        );
    }
}
```

### 8.2 Controller 层集成测试示例

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("POST /api/user/register → 注册成功")
    void shouldRegisterNewUser() throws Exception {
        String json = """
            {
                "studentId": "2024001",
                "name": "张三",
                "password": "Abc123!@#"
            }
        """;

        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.studentId").value("2024001"))
            .andExpect(jsonPath("$.name").value("张三"))
            .andExpect(jsonPath("$.password").doesNotExist()); // 密码不返回
    }

    @Test
    @DisplayName("POST /api/user/register → 重复学号返回 409")
    void shouldRejectDuplicateStudentId() throws Exception {
        // 先注册一个用户
        registerUser("2024001", "张三", "password");

        // 再次注册同学号
        String json = """
            {"studentId": "2024001", "name": "李四", "password": "password"}
        """;

        mockMvc.perform(post("/api/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/user/login → 正确凭据登录成功")
    void shouldLoginWithCorrectCredentials() throws Exception {
        // 先注册
        registerUser("2024001", "张三", "correctPassword");

        String json = """
            {"studentId": "2024001", "password": "correctPassword"}
        """;

        mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists());
    }

    @Test
    @DisplayName("POST /api/user/login → 错误密码返回 401")
    void shouldRejectWrongPassword() throws Exception {
        registerUser("2024001", "张三", "correctPassword");

        String json = """
            {"studentId": "2024001", "password": "wrongPassword"}
        """;

        mockMvc.perform(post("/api/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isUnauthorized());
    }
}
```

### 8.3 管理员权限测试示例

```java
@Test
@DisplayName("普通用户访问管理接口 → 403")
void shouldRejectStudentAccessToAdminAPI() throws Exception {
    // 以学生身份登录
    String studentToken = loginAsStudent();

    mockMvc.perform(get("/api/admin/users")
            .header("Authorization", "Bearer " + studentToken))
        .andExpect(status().isForbidden());
}

@Test
@DisplayName("未登录访问管理接口 → 401")
void shouldRejectUnauthenticatedAccess() throws Exception {
    mockMvc.perform(get("/api/admin/users"))
        .andExpect(status().isUnauthorized());
}

@Test
@DisplayName("管理员访问管理接口 → 200")
void shouldAllowAdminAccess() throws Exception {
    String adminToken = loginAsAdmin();

    mockMvc.perform(get("/api/admin/users")
            .header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk());
}
```

### 8.4 并发预约测试示例

```java
@Test
@DisplayName("并发预约同一座位 → 仅一个成功")
void shouldAllowOnlyOneConcurrentReservation() throws Exception {
    int threadCount = 10;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failCount = new AtomicInteger(0);

    for (int i = 0; i < threadCount; i++) {
        final long userId = i + 1;
        executor.submit(() -> {
            try {
                reservationService.createReservation(userId, 100L, startTime, endTime);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        });
    }

    latch.await(10, TimeUnit.SECONDS);
    executor.shutdown();

    assertEquals(1, successCount.get(), "仅应有一个线程预约成功");
    assertEquals(9, failCount.get(), "其余9个线程应失败");
}
```

### 8.5 前端组件测试示例

```javascript
// SeatSelection.spec.js
import { mount } from '@vue/test-utils'
import { describe, it, expect, vi } from 'vitest'
import SeatSelection from './SeatSelection.vue'

describe('SeatSelection.vue', () => {
  it('应正确显示空闲座位', () => {
    const wrapper = mount(SeatSelection, {
      props: {
        seats: [
          { id: 1, number: 'A01', status: 'available' },
          { id: 2, number: 'A02', status: 'occupied' },
        ]
      }
    })

    const availableSeats = wrapper.findAll('.seat.available')
    const occupiedSeats = wrapper.findAll('.seat.occupied')

    expect(availableSeats).toHaveLength(1)
    expect(occupiedSeats).toHaveLength(1)
  })

  it('点击空闲座位应触发预约事件', async () => {
    const wrapper = mount(SeatSelection, {
      props: {
        seats: [{ id: 1, number: 'A01', status: 'available' }]
      }
    })

    await wrapper.find('.seat.available').trigger('click')

    expect(wrapper.emitted('reserve')).toBeTruthy()
    expect(wrapper.emitted('reserve')[0]).toEqual([{ id: 1, number: 'A01' }])
  })

  it('点击已占用座位不应触发事件', async () => {
    const wrapper = mount(SeatSelection, {
      props: {
        seats: [{ id: 1, number: 'A01', status: 'occupied' }]
      }
    })

    await wrapper.find('.seat.occupied').trigger('click')

    expect(wrapper.emitted('reserve')).toBeFalsy()
  })

  it('空座位列表应显示提示信息', () => {
    const wrapper = mount(SeatSelection, {
      props: { seats: [] }
    })

    expect(wrapper.text()).toContain('暂无可选座位')
  })
})
```

### 8.6 安全测试用例

```java
@Test
@DisplayName("SQL注入防护 → 登录接口")
void shouldPreventSqlInjectionInLogin() throws Exception {
    String maliciousInput = "' OR '1'='1";
    String json = String.format(
        "{\"studentId\": \"%s\", \"password\": \"%s\"}", maliciousInput, maliciousInput
    );

    mockMvc.perform(post("/api/user/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isUnauthorized());  // 不应返回成功
}

@Test
@DisplayName("XSS防护 → 用户名注入")
void shouldEscapeXssInUserName() throws Exception {
    String xssName = "<script>alert('xss')</script>";

    String json = String.format(
        "{\"studentId\": \"99999\", \"name\": \"%s\", \"password\": \"test\"}", xssName
    );

    String response = mockMvc.perform(post("/api/user/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();

    // 响应中的 name 应被转义
    assertThat(response, not(containsString("<script>")));
    assertThat(response, containsString("&lt;script&gt;"));
}
```

---

## 9. 综合评估

### 9.1 评分卡

| 维度 | 评分 | 说明 |
|------|------|------|
| **测试覆盖率** | ⭐☆☆☆☆ (1/5) | 41 个文件仅 1 个测试文件，覆盖率 < 1% |
| **代码结构** | ⭐⭐⭐☆☆ (3/5) | 分层清晰(Controller→Service→Mapper)，但测试包名错误 |
| **安全性** | ⭐⭐☆☆☆ (2/5) | 密码明文、弱凭据、.env泄露，未做基本安全测试 |
| **可维护性** | ⭐⭐⭐☆☆ (3/5) | 结构规范，但缺失 CI/CD、Docker、环境变量模板 |
| **接口规范** | ⭐⭐⭐☆☆ (3/5) | RESTful 风格，但缺少 API 文档 |
| **并发处理** | ⭐⭐☆☆☆ (2/5) | 预约并发场景未验证 |
| **前端质量** | ⭐⭐⭐☆☆ (3/5) | 组件拆分合理，但无状态管理(Pinia/Vuex) |
| **综合评分** | ⭐⭐½☆☆ (2.5/5) | 需要大量测试补充和安全加固 |

### 9.2 风险矩阵

```
影响 ↑
严重 │  [并发预约]    [密码明文]
     │  [测试缺失]    [.env泄露]
     │
中等 │  [无CI/CD]     [弱口令]
     │  [输入校验]
     │
轻微 │  [IDE配置]     [拼写错误]
     │
     └──────────────────────────→ 概率
          低          中        高
```

### 9.3 整改建议优先级

| 优先级 | 任务 | 预估工时 | 影响范围 |
|--------|------|----------|----------|
| 🔴 P0 | 修正测试包结构 | 0.5h | 全局 |
| 🔴 P0 | 编写核心 Service 单元测试 | 16h | User/Reservation/Seat |
| 🔴 P0 | 密码加密 + .env 清除 | 2h | 安全 |
| 🔴 P0 | 权限验证集成测试 | 4h | 安全 |
| 🟡 P1 | 编写 Controller 集成测试 | 12h | 全部接口 |
| 🟡 P1 | 并发预约测试 | 4h | 核心业务 |
| 🟡 P1 | 前端组件测试 | 12h | 全部页面 |
| 🟡 P1 | Mapper 层测试 | 4h | 数据层 |
| 🟡 P1 | AI 服务测试 | 6h | AI 对话 |
| 🟢 P2 | E2E 测试 | 8h | 核心流程 |
| 🟢 P2 | CI/CD 流水线 | 4h | 自动化 |
| 🟢 P2 | 性能测试 | 4h | 非功能需求 |

---

## 附录 A — 测试环境清单

| 组件 | 版本/配置 | 用途 |
|------|-----------|------|
| JDK | 8 / 11 / 17 | 编译运行 |
| Maven | 3.8+ | 构建 |
| MySQL | 8.0 | 主数据库 |
| H2 | 2.x (test scope) | 测试数据库 |
| Node.js | 18+ | 前端开发 |
| npm | 9+ | 前端包管理 |
| Python | 3.10+ | AI 服务 |
| Git | 2.x | 版本控制 |

## 附录 B — 测试执行命令

```bash
# 后端单元测试
cd backend
./mvnw test

# 后端测试（指定测试类）
./mvnw test -Dtest=UserServiceTest

# 后端测试 + 覆盖率报告
./mvnw test jacoco:report

# 前端单元测试
cd frontend
npx vitest run

# 前端测试（监听模式）
npx vitest

# AI 服务测试
cd ai-service
python -m pytest -v

# AI 服务测试 + 覆盖率
python -m pytest --cov=. --cov-report=html
```

---

*报告结束*
