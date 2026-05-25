# Admin Panel & AI Agent & Frontend Redesign — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build admin panel, AI chat agent (LangChain + FastAPI), and redesign all student-side pages with unified visual style.

**Architecture:** Java Spring Boot handles business logic + admin APIs + chat proxy; Python FastAPI service runs LangChain Agent with DeepSeek for conversational AI; Vue 3 frontend redesigned with unified purple-gradient theme across all pages.

**Tech Stack:** Java 17, Spring Boot 3.1.5, MyBatis-Plus 3.5.3.1, MySQL 8, Vue 3, Vite, Python 3.10+, FastAPI, LangChain, langchain-deepseek, SQLite

---

## Phase 1: Backend Foundation

### Task 1: Add `role` field to User entity and register

**Files:**
- Modify: `backend/src/main/java/com/studyseat/entity/User.java`
- Modify: `backend/src/main/java/com/studyseat/service/impl/UserServiceImpl.java`

- [ ] **Step 1: Add `role` field to User entity**

In `User.java`, add after the `isRestricted` field:

```java
private String role; // student or admin
```

- [ ] **Step 2: Set default role in UserServiceImpl.register**

In `UserServiceImpl.java`, in the `register` method, add after `user.setIsRestricted(false);`:

```java
user.setRole("student");
```

- [ ] **Step 3: Verify compilation**

Run: `cd backend && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/studyseat/entity/User.java backend/src/main/java/com/studyseat/service/impl/UserServiceImpl.java
git commit -m "feat: add role field to User entity"
```

---

### Task 2: Include `role` in login response

**Files:**
- Modify: `backend/src/main/java/com/studyseat/controller/UserController.java`

- [ ] **Step 1: Update login method response**

In `UserController.java`, the login endpoint already returns `result.put("data", user)` which includes all fields. Since `role` is now a field on User, it will be serialized automatically. No code change needed — verify it works.

- [ ] **Step 2: Verify by running Spring Boot**

Run: `mvn spring-boot:run`
Test with curl:
```bash
curl -X POST http://localhost:8080/api/student/login \
  -d 'studentId=admin&password=admin123'
```
Expected: Response `data` includes `role` field.

- [ ] **Step 3: Stop the server and commit**

```bash
git add backend/src/main/java/com/studyseat/controller/UserController.java
git commit -m "feat: role field now included in login response (auto via User entity)"
```

---

### Task 3: Insert preset admin account on startup

**Files:**
- Create: `backend/src/main/java/com/studyseat/config/DataInitializer.java`

- [ ] **Step 1: Write DataInitializer**

```java
package com.studyseat.config;

import com.studyseat.entity.User;
import com.studyseat.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserMapper userMapper;

    @Override
    public void run(String... args) {
        User existing = userMapper.findByStudentId("admin");
        if (existing == null) {
            User admin = new User();
            admin.setStudentId("admin");
            admin.setPassword("admin123");
            admin.setName("管理员");
            admin.setRole("admin");
            admin.setViolationCount(0);
            admin.setIsRestricted(false);
            admin.setCreateTime(java.time.LocalDateTime.now());
            admin.setUpdateTime(java.time.LocalDateTime.now());
            userMapper.insert(admin);
            System.out.println("=== 预设管理员账号已创建: admin / admin123 ===");
        }
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd backend && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/com/studyseat/config/DataInitializer.java
git commit -m "feat: auto-create preset admin account on startup"
```

---

### Task 4: Admin auth interceptor

**Files:**
- Create: `backend/src/main/java/com/studyseat/config/AdminAuthInterceptor.java`
- Create: `backend/src/main/java/com/studyseat/config/WebMvcConfig.java`

- [ ] **Step 1: Write AdminAuthInterceptor**

```java
package com.studyseat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

public class AdminAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String userId = request.getParameter("userId");
        String role = request.getHeader("X-User-Role");

        if (role == null || !"admin".equals(role)) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            Map<String, Object> body = Map.of("code", 401, "message", "无管理员权限", "data", null);
            response.getWriter().write(new ObjectMapper().writeValueAsString(body));
            return false;
        }
        return true;
    }
}
```

Note: The existing project does not use Spring Security or JWT session management. The frontend sends `userId` as a query param to admin endpoints, and the role check relies on the frontend passing `X-User-Role` header. The ChatController will validate the user properly. For admin endpoints, the interceptor provides the gate.

- [ ] **Step 2: Write WebMvcConfig**

```java
package com.studyseat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AdminAuthInterceptor())
                .addPathPatterns("/api/admin/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE");
    }
}
```

- [ ] **Step 3: Verify compilation**

Run: `cd backend && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/com/studyseat/config/AdminAuthInterceptor.java backend/src/main/java/com/studyseat/config/WebMvcConfig.java
git commit -m "feat: add admin auth interceptor for /api/admin/** paths"
```

---

### Task 5: Admin backend — AdminService + AdminServiceImpl + AdminController

**Files:**
- Create: `backend/src/main/java/com/studyseat/service/AdminService.java`
- Create: `backend/src/main/java/com/studyseat/service/impl/AdminServiceImpl.java`
- Create: `backend/src/main/java/com/studyseat/controller/AdminController.java`

- [ ] **Step 1: Write AdminService interface**

```java
package com.studyseat.service;

import com.studyseat.entity.Area;
import com.studyseat.entity.Seat;
import com.studyseat.entity.User;

import java.util.List;
import java.util.Map;

public interface AdminService {
    Map<String, Object> getDashboard();
    List<Area> getAllAreas();
    Area createArea(String name, Integer floor);
    Area updateArea(Long id, String name, Integer floor);
    void toggleArea(Long id, boolean disabled);
    List<Seat> getSeats(Long areaId);
    Seat createSeat(Long areaId, String seatNumber);
    Seat updateSeat(Long id, Long areaId, String seatNumber);
    void toggleSeat(Long id, boolean disabled);
    void releaseSeat(Long id);
    List<User> getAllUsers();
    void banUser(Long id, boolean banned);
    void resetViolation(Long id);
}
```

- [ ] **Step 2: Write AdminServiceImpl**

```java
package com.studyseat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studyseat.entity.*;
import com.studyseat.mapper.*;
import com.studyseat.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired private AreaMapper areaMapper;
    @Autowired private SeatMapper seatMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private ReservationMapper reservationMapper;

    @Override
    public Map<String, Object> getDashboard() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSeats", seatMapper.selectCount(null));
        stats.put("availableSeats",
            seatMapper.selectCount(new LambdaQueryWrapper<Seat>().eq(Seat::getStatus, "空闲")));
        stats.put("todayReservations",
            reservationMapper.selectCount(new LambdaQueryWrapper<Reservation>()
                .ge(Reservation::getStartTime, LocalDateTime.now().withHour(0).withMinute(0))
                .le(Reservation::getStartTime, LocalDateTime.now().withHour(23).withMinute(59))));
        stats.put("violationUsers",
            userMapper.selectCount(new LambdaQueryWrapper<User>().ge(User::getViolationCount, 3)));
        return stats;
    }

    @Override
    public List<Area> getAllAreas() {
        return areaMapper.selectList(null);
    }

    @Override
    public Area createArea(String name, Integer floor) {
        Area area = new Area();
        area.setName(name);
        area.setFloor(floor);
        area.setIsEnabled(true);
        area.setCreateTime(LocalDateTime.now());
        area.setUpdateTime(LocalDateTime.now());
        areaMapper.insert(area);
        return area;
    }

    @Override
    public Area updateArea(Long id, String name, Integer floor) {
        Area area = areaMapper.selectById(id);
        if (area == null) throw new RuntimeException("区域不存在");
        area.setName(name);
        area.setFloor(floor);
        area.setUpdateTime(LocalDateTime.now());
        areaMapper.updateById(area);
        return area;
    }

    @Override
    public void toggleArea(Long id, boolean disabled) {
        Area area = areaMapper.selectById(id);
        if (area == null) throw new RuntimeException("区域不存在");
        area.setIsEnabled(!disabled);
        area.setUpdateTime(LocalDateTime.now());
        areaMapper.updateById(area);
    }

    @Override
    public List<Seat> getSeats(Long areaId) {
        if (areaId != null) return seatMapper.findByAreaId(areaId);
        return seatMapper.selectList(null);
    }

    @Override
    public Seat createSeat(Long areaId, String seatNumber) {
        Seat seat = new Seat();
        seat.setAreaId(areaId);
        seat.setSeatNumber(seatNumber);
        seat.setStatus("空闲");
        seat.setIsEnabled(true);
        seat.setCreateTime(LocalDateTime.now());
        seat.setUpdateTime(LocalDateTime.now());
        seatMapper.insert(seat);
        return seat;
    }

    @Override
    public Seat updateSeat(Long id, Long areaId, String seatNumber) {
        Seat seat = seatMapper.selectById(id);
        if (seat == null) throw new RuntimeException("座位不存在");
        seat.setAreaId(areaId);
        seat.setSeatNumber(seatNumber);
        seat.setUpdateTime(LocalDateTime.now());
        seatMapper.updateById(seat);
        return seat;
    }

    @Override
    public void toggleSeat(Long id, boolean disabled) {
        Seat seat = seatMapper.selectById(id);
        if (seat == null) throw new RuntimeException("座位不存在");
        seat.setIsEnabled(!disabled);
        seat.setUpdateTime(LocalDateTime.now());
        seatMapper.updateById(seat);
    }

    @Override
    public void releaseSeat(Long id) {
        Seat seat = seatMapper.selectById(id);
        if (seat == null) throw new RuntimeException("座位不存在");
        seat.setStatus("空闲");
        seat.setUpdateTime(LocalDateTime.now());
        seatMapper.updateById(seat);

        // End any active reservation on this seat
        List<Reservation> active = reservationMapper.selectList(
            new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getSeatId, id)
                .in(Reservation::getStatus, List.of("已开始", "暂离"))
        );
        for (Reservation r : active) {
            r.setStatus("已结束");
            r.setUpdateTime(LocalDateTime.now());
            reservationMapper.updateById(r);
        }
    }

    @Override
    public List<User> getAllUsers() {
        return userMapper.selectList(
            new LambdaQueryWrapper<User>().ne(User::getRole, "admin"));
    }

    @Override
    public void banUser(Long id, boolean banned) {
        User user = userMapper.selectById(id);
        if (user == null) throw new RuntimeException("用户不存在");
        user.setIsRestricted(banned);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public void resetViolation(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new RuntimeException("用户不存在");
        user.setViolationCount(0);
        user.setIsRestricted(false);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }
}
```

- [ ] **Step 3: Write AdminController**

```java
package com.studyseat.controller;

import com.studyseat.entity.Area;
import com.studyseat.entity.Seat;
import com.studyseat.entity.User;
import com.studyseat.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "获取成功");
        result.put("data", adminService.getDashboard());
        return result;
    }

    @GetMapping("/areas")
    public Map<String, Object> getAreas() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "获取成功");
        result.put("data", adminService.getAllAreas());
        return result;
    }

    @PostMapping("/areas")
    public Map<String, Object> createArea(@RequestParam String name, @RequestParam Integer floor) {
        Map<String, Object> result = new HashMap<>();
        try {
            Area area = adminService.createArea(name, floor);
            result.put("code", 200);
            result.put("message", "创建成功");
            result.put("data", area);
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    @PutMapping("/areas/{id}")
    public Map<String, Object> updateArea(@PathVariable Long id, @RequestParam String name,
                                           @RequestParam Integer floor) {
        Map<String, Object> result = new HashMap<>();
        try {
            Area area = adminService.updateArea(id, name, floor);
            result.put("code", 200);
            result.put("message", "更新成功");
            result.put("data", area);
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    @PutMapping("/areas/{id}/disable")
    public Map<String, Object> toggleArea(@PathVariable Long id, @RequestParam boolean disabled) {
        Map<String, Object> result = new HashMap<>();
        try {
            adminService.toggleArea(id, disabled);
            result.put("code", 200);
            result.put("message", disabled ? "禁用成功" : "启用成功");
            result.put("data", null);
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    @GetMapping("/seats")
    public Map<String, Object> getSeats(@RequestParam(required = false) Long areaId) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "获取成功");
        result.put("data", adminService.getSeats(areaId));
        return result;
    }

    @PostMapping("/seats")
    public Map<String, Object> createSeat(@RequestParam Long areaId, @RequestParam String seatNumber) {
        Map<String, Object> result = new HashMap<>();
        try {
            Seat seat = adminService.createSeat(areaId, seatNumber);
            result.put("code", 200);
            result.put("message", "创建成功");
            result.put("data", seat);
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    @PutMapping("/seats/{id}")
    public Map<String, Object> updateSeat(@PathVariable Long id, @RequestParam Long areaId,
                                           @RequestParam String seatNumber) {
        Map<String, Object> result = new HashMap<>();
        try {
            Seat seat = adminService.updateSeat(id, areaId, seatNumber);
            result.put("code", 200);
            result.put("message", "更新成功");
            result.put("data", seat);
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    @PutMapping("/seats/{id}/disable")
    public Map<String, Object> toggleSeat(@PathVariable Long id, @RequestParam boolean disabled) {
        Map<String, Object> result = new HashMap<>();
        try {
            adminService.toggleSeat(id, disabled);
            result.put("code", 200);
            result.put("message", disabled ? "禁用成功" : "启用成功");
            result.put("data", null);
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    @PutMapping("/seats/{id}/release")
    public Map<String, Object> releaseSeat(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            adminService.releaseSeat(id);
            result.put("code", 200);
            result.put("message", "释放成功");
            result.put("data", null);
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    @GetMapping("/users")
    public Map<String, Object> getUsers() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "获取成功");
        result.put("data", adminService.getAllUsers());
        return result;
    }

    @PutMapping("/users/{id}/ban")
    public Map<String, Object> banUser(@PathVariable Long id, @RequestParam boolean banned) {
        Map<String, Object> result = new HashMap<>();
        try {
            adminService.banUser(id, banned);
            result.put("code", 200);
            result.put("message", banned ? "封禁成功" : "解封成功");
            result.put("data", null);
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    @PutMapping("/users/{id}/violation-reset")
    public Map<String, Object> resetViolation(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            adminService.resetViolation(id);
            result.put("code", 200);
            result.put("message", "违规已重置");
            result.put("data", null);
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
            result.put("data", null);
        }
        return result;
    }
}
```

- [ ] **Step 4: Verify compilation**

Run: `cd backend && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/studyseat/service/AdminService.java \
        backend/src/main/java/com/studyseat/service/impl/AdminServiceImpl.java \
        backend/src/main/java/com/studyseat/controller/AdminController.java
git commit -m "feat: add admin backend — AdminController, AdminService with full CRUD"
```

---

### Task 6: ChatController proxy + DTO

**Files:**
- Create: `backend/src/main/java/com/studyseat/dto/ChatRequest.java`
- Create: `backend/src/main/java/com/studyseat/controller/ChatController.java`
- Modify: `backend/src/main/resources/application.yml`

- [ ] **Step 1: Write ChatRequest DTO**

```java
package com.studyseat.dto;

public class ChatRequest {
    private String message;
    private String conversationId;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
}
```

- [ ] **Step 2: Write ChatController**

```java
package com.studyseat.controller;

import com.studyseat.dto.ChatRequest;
import com.studyseat.entity.User;
import com.studyseat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.*;

@RestController
@RequestMapping("/api/student")
public class ChatController {

    @Value("${ai-service.url}")
    private String aiServiceUrl;

    @Autowired
    private UserService userService;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody ChatRequest request,
                                     @RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            User user = userService.getById(userId);
            if (user == null) {
                result.put("code", 401);
                result.put("message", "请先登录");
                result.put("data", null);
                return result;
            }

            Map<String, Object> aiRequest = new HashMap<>();
            aiRequest.put("message", request.getMessage());
            aiRequest.put("conversationId", request.getConversationId());
            aiRequest.put("userId", userId.toString());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(aiRequest, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                aiServiceUrl + "/ai/chat", entity, Map.class);

            result.put("code", 200);
            result.put("message", "ok");
            result.put("data", response.getBody());
        } catch (Exception e) {
            result.put("code", 503);
            result.put("message", "AI 服务暂时不可用");
            result.put("data", null);
        }
        return result;
    }
}
```

- [ ] **Step 3: Add ai-service.url to application.yml**

Append to `application.yml`:

```yaml
ai-service:
  url: http://localhost:8000
```

- [ ] **Step 4: Verify compilation**

Run: `cd backend && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/com/studyseat/dto/ChatRequest.java \
        backend/src/main/java/com/studyseat/controller/ChatController.java \
        backend/src/main/resources/application.yml
git commit -m "feat: add ChatController proxy forwarding to Python AI service"
```

---

## Phase 2: Python AI Service

### Task 7: Python project setup and config

**Files:**
- Create: `ai-service/requirements.txt`
- Create: `ai-service/config.py`
- Create: `ai-service/.env`

- [ ] **Step 1: Create requirements.txt**

```
fastapi==0.115.0
uvicorn==0.30.0
langchain==0.3.0
langchain-deepseek==0.1.0
langchain-community==0.3.0
langgraph==0.2.0
python-dotenv==1.0.0
httpx==0.27.0
```

- [ ] **Step 2: Create config.py**

```python
import os
from dotenv import load_dotenv

load_dotenv()

DEEPSEEK_API_KEY = os.environ.get("DEEPSEEK_API_KEY", "your-api-key")
DEEPSEEK_MODEL = os.environ.get("DEEPSEEK_MODEL", "deepseek-chat")
JAVA_BASE_URL = os.environ.get("JAVA_BASE_URL", "http://localhost:8080")
```

- [ ] **Step 3: Create .env**

```
DEEPSEEK_API_KEY=sk-your-key-here
JAVA_BASE_URL=http://localhost:8080
```

- [ ] **Step 4: Install dependencies**

```bash
cd ai-service && pip install -r requirements.txt
```

- [ ] **Step 5: Commit**

```bash
git add ai-service/requirements.txt ai-service/config.py
git commit -m "feat: Python AI service setup — FastAPI + LangChain dependencies"
```

---

### Task 8: Tool functions — HTTP callbacks to Java API

**Files:**
- Create: `ai-service/tools.py`

> **Design note:** `user_id` is not a tool parameter visible to the LLM. It's passed via a `contextvars.ContextVar` set by main.py before each agent invocation. This keeps tool signatures clean for the LLM.

- [ ] **Step 1: Write tools.py**

```python
import httpx
from contextvars import ContextVar
from langchain.tools import tool
from config import JAVA_BASE_URL

current_user_id: ContextVar[str] = ContextVar("current_user_id", default="")


def _uid() -> str:
    return current_user_id.get()


def _java_get(path: str, params: dict = None) -> dict:
    url = f"{JAVA_BASE_URL}{path}"
    if params is None:
        params = {}
    params["userId"] = _uid()
    resp = httpx.get(url, params=params, timeout=10)
    resp.raise_for_status()
    return resp.json()


def _java_post(path: str, data: dict = None) -> dict:
    url = f"{JAVA_BASE_URL}{path}"
    if data is None:
        data = {}
    data["userId"] = _uid()
    resp = httpx.post(url, data=data, timeout=10)
    resp.raise_for_status()
    return resp.json()


@tool
def search_seats(date: str, start_time: str, end_time: str,
                  area: str = None) -> str:
    """
    Query available seats for a given date and time range.
    date: date string like '2026-05-17'
    start_time: start time like '14:00'
    end_time: end time like '16:00'
    area: optional area name or ID filter
    """
    try:
        params = {}
        if area:
            params["areaId"] = area
        result = _java_get("/api/student/seats", params)
        if result.get("code") == 200:
            seats = result.get("data", [])
            available = [s for s in seats if s.get("status") == "空闲" and s.get("isEnabled")]
            if not available:
                return "未找到可用座位"
            lines = ["找到以下空闲座位："]
            for s in available:
                lines.append(f"  - {s['seatNumber']} (ID: {s['id']})")
            return "\n".join(lines)
        return f"查询失败: {result.get('message')}"
    except Exception as e:
        return f"查询座位出错: {str(e)}"


@tool
def create_reservation(seat_id: int, start_time: str, end_time: str) -> str:
    """
    Create a reservation for a specific seat and time.
    seat_id: the seat ID to reserve
    start_time: datetime string like '2026-05-17T14:00:00'
    end_time: datetime string like '2026-05-17T16:00:00'
    """
    try:
        result = _java_post("/api/student/reservations", {
            "seatId": seat_id,
            "startTime": start_time,
            "endTime": end_time,
        })
        if result.get("code") == 200:
            return f"预约成功！预约ID: {result['data']['id']}"
        return f"预约失败: {result.get('message')}"
    except Exception as e:
        return f"创建预约出错: {str(e)}"


@tool
def cancel_reservation(reservation_id: int) -> str:
    """Cancel an existing reservation by its ID."""
    try:
        result = _java_post(f"/api/student/reservations/{reservation_id}/cancel")
        if result.get("code") == 200:
            return "取消成功"
        return f"取消失败: {result.get('message')}"
    except Exception as e:
        return f"取消预约出错: {str(e)}"


@tool
def get_my_reservations() -> str:
    """Get the current user's reservation list."""
    try:
        result = _java_get("/api/student/reservations")
        if result.get("code") == 200:
            reservations = result.get("data", [])
            if not reservations:
                return "你当前没有预约记录"
            lines = ["你的预约记录："]
            for r in reservations:
                lines.append(
                    f"  - ID:{r['id']} 座位{r.get('seatId')} "
                    f"{r['startTime']}~{r['endTime']} 状态:{r['status']}"
                )
            return "\n".join(lines)
        return f"查询失败: {result.get('message')}"
    except Exception as e:
        return f"查询预约出错: {str(e)}"


@tool
def get_my_profile() -> str:
    """Get the current user's profile information."""
    try:
        result = _java_get(f"/api/student/user/{_uid()}")
        if result.get("code") == 200:
            u = result.get("data", {})
            return (
                f"学号: {u.get('studentId')}\n"
                f"姓名: {u.get('name')}\n"
                f"违规次数: {u.get('violationCount', 0)}\n"
                f"账号状态: {'已限制' if u.get('isRestricted') else '正常'}"
            )
        return f"查询失败: {result.get('message')}"
    except Exception as e:
        return f"查询个人信息出错: {str(e)}"
```

- [ ] **Step 2: Verify syntax**

```bash
cd ai-service && python -c "import tools; print('OK')"
```
Expected: OK (may need to install deps first)

- [ ] **Step 3: Commit**

```bash
git add ai-service/tools.py
git commit -m "feat: LangChain tools — HTTP callbacks to Java backend"
```

---

### Task 9: LangChain agent with memory

**Files:**
- Create: `ai-service/agent.py`

- [ ] **Step 1: Write agent.py**

```python
from langchain_deepseek import ChatDeepSeek
from langgraph.prebuilt import create_react_agent
from langchain_community.chat_message_histories import SQLChatMessageHistory
from langchain_core.runnables.history import RunnableWithMessageHistory

from config import DEEPSEEK_API_KEY, DEEPSEEK_MODEL
from tools import (search_seats, create_reservation, cancel_reservation,
                    get_my_reservations, get_my_profile)


SYSTEM_PROMPT = """你是校园自习助手，帮助学生预约自习座位和规划学习计划。
先理解用户意图，必要时调用工具获取信息，最后用自然语言回复用户。

规则：
- 预约前必须确认日期、时段、区域/座位偏好
- 创建预约前需用户明确确认
- 自习计划规划时先查可用座位，以表格呈现计划
- 批量预约逐个创建，失败时说明原因
- 回复简洁友好，使用中文"""
```

- [ ] **Step 2: Add agent creation and session history functions to agent.py**

Append to `agent.py`:

```python
def get_session_history(session_id: str) -> SQLChatMessageHistory:
    return SQLChatMessageHistory(
        session_id=session_id,
        connection="sqlite:///chat_history.db"
    )


def create_agent():
    llm = ChatDeepSeek(
        model=DEEPSEEK_MODEL,
        api_key=DEEPSEEK_API_KEY,
        temperature=0.7,
    )

    tools = [
        search_seats,
        create_reservation,
        cancel_reservation,
        get_my_reservations,
        get_my_profile,
    ]

    base_agent = create_react_agent(llm, tools, SYSTEM_PROMPT)

    agent_with_memory = RunnableWithMessageHistory(
        base_agent,
        get_session_history,
        input_messages_key="messages",
        history_messages_key="history",
    )

    return agent_with_memory
```

- [ ] **Step 3: Verify syntax**

```bash
cd ai-service && python -c "import agent; print('OK')"
```

- [ ] **Step 4: Commit**

```bash
git add ai-service/agent.py
git commit -m "feat: LangChain React agent with SQLite-backed message history"
```

---

### Task 10: FastAPI main entry point

**Files:**
- Create: `ai-service/main.py`

- [ ] **Step 1: Write main.py**

```python
from fastapi import FastAPI
from pydantic import BaseModel
from agent import create_agent
from tools import current_user_id

app = FastAPI(title="StudySeat AI Service")

agent = create_agent()


class ChatRequest(BaseModel):
    message: str
    conversationId: str
    userId: str


class ChatResponse(BaseModel):
    reply: str
    conversationId: str


@app.post("/ai/chat")
async def chat(req: ChatRequest) -> ChatResponse:
    # Set user context so tools know which user is calling
    current_user_id.set(req.userId)

    config = {"configurable": {"session_id": req.conversationId}}
    result = agent.invoke(
        {"messages": [("user", req.message)]},
        config=config,
    )
    reply = result["messages"][-1].content
    return ChatResponse(reply=reply, conversationId=req.conversationId)


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
```

- [ ] **Step 2: Verify startup**

```bash
cd ai-service && timeout 5 python main.py 2>&1 || true
```
Expected: "Uvicorn running on http://0.0.0.0:8000"

- [ ] **Step 3: Commit**

```bash
git add ai-service/main.py
git commit -m "feat: FastAPI entry point with /ai/chat endpoint"
```

---

## Phase 3: Frontend Redesign

### Task 11: App.vue — remove centered flex layout

**Files:**
- Modify: `frontend/src/App.vue`

- [ ] **Step 1: Rewrite App.vue**

```vue
<template>
  <div class="app">
    <router-view />
  </div>
</template>

<script>
export default {
  name: 'App'
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}
.app {
  min-height: 100vh;
  background: #f5f6fa;
}
</style>
```

- [ ] **Step 2: Verify frontend compiles**

```bash
cd frontend && npx vite build --mode development 2>&1 | tail -5
```
Expected: no errors

- [ ] **Step 3: Commit**

```bash
git add frontend/src/App.vue
git commit -m "refactor: remove centered flex from App.vue, delegate layout to pages"
```

---

### Task 12: Router — add all new routes

**Files:**
- Modify: `frontend/src/router/index.js`

- [ ] **Step 1: Rewrite router/index.js**

```js
import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Home from '../views/Home.vue'
import SeatSelection from '../views/SeatSelection.vue'
import MyReservations from '../views/MyReservations.vue'
import ChatPage from '../views/ChatPage.vue'
import AdminLayout from '../views/admin/AdminLayout.vue'
import Dashboard from '../views/admin/Dashboard.vue'
import AreaManage from '../views/admin/AreaManage.vue'
import SeatManage from '../views/admin/SeatManage.vue'
import UserManage from '../views/admin/UserManage.vue'

const routes = [
  { path: '/', name: 'Login', component: Login },
  { path: '/home', name: 'Home', component: Home },
  { path: '/seat-selection', name: 'SeatSelection', component: SeatSelection },
  { path: '/my-reservations', name: 'MyReservations', component: MyReservations },
  { path: '/ai-assistant', name: 'ChatPage', component: ChatPage },
  {
    path: '/admin',
    component: AdminLayout,
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'Dashboard', component: Dashboard },
      { path: 'areas', name: 'AreaManage', component: AreaManage },
      { path: 'seats', name: 'SeatManage', component: SeatManage },
      { path: 'users', name: 'UserManage', component: UserManage },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/router/index.js
git commit -m "feat: add all routes — admin, ai-assistant, student pages"
```

---

### Task 13: Login.vue — redesign

**Files:**
- Modify: `frontend/src/views/Login.vue`

- [ ] **Step 1: Rewrite Login.vue completely**

```vue
<template>
  <div class="login-page">
    <div class="login-card">
      <div class="logo-area">
        <div class="logo-icon">📚</div>
        <h2>自习座位预约</h2>
        <p>校园智能自习管理</p>
      </div>
      <div class="form-group">
        <label>学号</label>
        <input type="text" v-model="studentId" placeholder="请输入学号" />
      </div>
      <div class="form-group">
        <label>姓名</label>
        <input type="text" v-model="password" placeholder="请输入姓名" />
      </div>
      <button class="login-btn" @click="login">登 录</button>
      <p class="register-link">还没有账号？<a href="#" @click.prevent="showRegister = true">立即注册</a></p>
    </div>

    <div v-if="showRegister" class="modal-overlay" @click.self="showRegister = false">
      <div class="modal-card">
        <h3>学生注册</h3>
        <div class="form-group">
          <label>学号</label>
          <input type="text" v-model="registerStudentId" placeholder="请输入学号" />
        </div>
        <div class="form-group">
          <label>姓名</label>
          <input type="text" v-model="registerName" placeholder="请输入姓名" />
        </div>
        <div class="modal-buttons">
          <button class="btn-primary" @click="register">注册</button>
          <button class="btn-cancel" @click="showRegister = false">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Login',
  data() {
    return {
      studentId: '',
      password: '',
      showRegister: false,
      registerStudentId: '',
      registerName: ''
    }
  },
  methods: {
    async login() {
      const response = await fetch('/api/student/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({ studentId: this.studentId, password: this.password })
      })
      const result = await response.json()
      if (result.code === 200) {
        localStorage.setItem('user', JSON.stringify(result.data))
        if (result.data.role === 'admin') {
          this.$router.push('/admin/dashboard')
        } else {
          this.$router.push('/home')
        }
      } else {
        alert(result.message)
      }
    },
    async register() {
      const response = await fetch('/api/student/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({ studentId: this.registerStudentId, name: this.registerName })
      })
      const result = await response.json()
      if (result.code === 200) {
        alert('注册成功，初始密码为学号后6位')
        this.showRegister = false
      } else {
        alert(result.message)
      }
    }
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}
.login-card {
  background: white;
  border-radius: 16px;
  padding: 36px 28px;
  width: 360px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.15);
}
.logo-area {
  text-align: center;
  margin-bottom: 28px;
}
.logo-icon {
  width: 56px; height: 56px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 14px;
  margin: 0 auto 12px;
  display: flex; align-items: center; justify-content: center;
  font-size: 26px;
}
.logo-area h2 { color: #333; font-size: 18px; margin: 0; }
.logo-area p { color: #999; font-size: 13px; margin: 4px 0 0; }
.form-group { margin-bottom: 18px; }
.form-group label { font-size: 13px; color: #666; display: block; margin-bottom: 6px; }
.form-group input {
  width: 100%; padding: 10px 14px;
  border: 1.5px solid #e0e0e0; border-radius: 10px;
  font-size: 14px; outline: none; transition: border-color 0.3s;
}
.form-group input:focus { border-color: #667eea; }
.login-btn {
  width: 100%; padding: 12px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white; border: none; border-radius: 10px;
  font-size: 16px; font-weight: bold; cursor: pointer; margin-bottom: 16px;
}
.register-link { text-align: center; font-size: 13px; color: #999; }
.register-link a { color: #667eea; text-decoration: none; }
.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.5);
  display: flex; align-items: center; justify-content: center; z-index: 1000;
}
.modal-card {
  background: white; border-radius: 16px; padding: 30px; width: 360px;
}
.modal-card h3 { margin-bottom: 20px; text-align: center; color: #333; }
.modal-buttons { display: flex; gap: 12px; margin-top: 20px; }
.btn-primary {
  flex: 1; padding: 10px; background: linear-gradient(135deg, #667eea, #764ba2);
  color: white; border: none; border-radius: 8px; cursor: pointer;
}
.btn-cancel {
  flex: 1; padding: 10px; background: #f0f0f0;
  color: #333; border: none; border-radius: 8px; cursor: pointer;
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/views/Login.vue
git commit -m "refactor: redesign Login page with gradient bg, logo card, rounded inputs"
```

---

### Task 14: Home.vue — redesign

**Files:**
- Modify: `frontend/src/views/Home.vue`

- [ ] **Step 1: Rewrite Home.vue completely**

```vue
<template>
  <div class="home-page">
    <div class="top-bar">
      <h3 class="title">📚 自习座位预约</h3>
      <div class="top-actions">
        <button class="ai-btn" @click="$router.push('/ai-assistant')">🤖 AI 助手</button>
        <button class="profile-btn" @click="$router.push('/my-reservations')">👤 我的</button>
      </div>
    </div>
    <div class="filter-bar">
      <span>选择区域：</span>
      <select v-model="selectedAreaId" @change="getSeats">
        <option value="">全部区域</option>
        <option v-for="area in areas" :key="area.id" :value="area.id">{{ area.name }} ({{ area.floor }}F)</option>
      </select>
    </div>
    <div class="seat-grid">
      <div v-for="seat in seats" :key="seat.id"
           :class="['seat-item', statusClass(seat.status)]"
           @click="selectSeat(seat)">
        <span class="seat-num">{{ seat.seatNumber }}</span>
        <span class="seat-label">{{ statusText(seat.status) }}</span>
      </div>
    </div>
    <div class="legend">
      <span v-for="item in legendItems" :key="item.text">
        <i :style="{color: item.color}">●</i> {{ item.text }}
      </span>
    </div>
    <div v-if="selectedSeat" class="seat-detail">
      <p><strong>{{ selectedSeat.seatNumber }}</strong> · {{ selectedAreaName }} · {{ statusText(selectedSeat.status) }}</p>
      <button v-if="selectedSeat.status === '空闲'" class="reserve-btn"
              @click="goReserve">预约此座位</button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Home',
  data() {
    return {
      areas: [],
      seats: [],
      selectedAreaId: '',
      selectedSeat: null,
      legendItems: [
        { text: '空闲', color: '#52c41a' },
        { text: '已预约', color: '#fa8c16' },
        { text: '占用', color: '#999' },
        { text: '暂离', color: '#1890ff' },
      ]
    }
  },
  computed: {
    selectedAreaName() {
      const a = this.areas.find(a => a.id === this.selectedAreaId)
      return a ? a.name : ''
    }
  },
  mounted() { this.getAreas() },
  methods: {
    async getAreas() {
      const resp = await fetch('/api/student/areas')
      const r = await resp.json()
      if (r.code === 200) this.areas = r.data
    },
    async getSeats() {
      if (!this.selectedAreaId) { this.seats = []; this.selectedSeat = null; return }
      const resp = await fetch(`/api/student/seats?areaId=${this.selectedAreaId}`)
      const r = await resp.json()
      if (r.code === 200) this.seats = r.data
      this.selectedSeat = null
    },
    selectSeat(seat) { this.selectedSeat = seat },
    goReserve() {
      this.$router.push({
        path: '/seat-selection',
        query: { seatId: this.selectedSeat.id, areaId: this.selectedAreaId }
      })
    },
    statusClass(s) {
      const m = { '空闲':'free', '已预约':'reserved', '占用':'occupied', '暂离':'away', '禁用':'disabled' }
      return m[s] || 'occupied'
    },
    statusText(s) {
      const m = { '空闲':'空闲', '已预约':'已预约', '占用':'占用', '暂离':'暂离', '禁用':'禁用' }
      return m[s] || s
    }
  }
}
</script>

<style scoped>
.home-page { min-height: 100vh; background: #f5f6fa; }
.top-bar {
  background: white; padding: 14px 20px;
  display: flex; justify-content: space-between; align-items: center;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.title { font-size: 18px; color: #333; margin: 0; }
.top-actions { display: flex; gap: 10px; }
.ai-btn {
  padding: 6px 14px; border-radius: 20px; border: none; cursor: pointer;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white; font-size: 13px; font-weight: bold;
}
.profile-btn {
  padding: 6px 14px; border-radius: 20px; border: none; cursor: pointer;
  background: #f0f0f0; font-size: 13px;
}
.filter-bar {
  padding: 16px 20px; display: flex; gap: 10px; align-items: center;
  font-size: 14px; color: #666;
}
.filter-bar select {
  padding: 8px 14px; border: 1.5px solid #e0e0e0; border-radius: 8px;
  font-size: 14px; background: white;
}
.seat-grid {
  padding: 0 20px; display: grid;
  grid-template-columns: repeat(5, 1fr); gap: 10px;
}
.seat-item {
  aspect-ratio: 1; border-radius: 10px; border: 2px solid #eee;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  cursor: pointer; transition: transform 0.2s;
}
.seat-item:hover { transform: scale(1.05); }
.seat-item.free { background: #e6f7ee; border-color: #52c41a; }
.seat-item.reserved { background: #fff1e6; border-color: #fa8c16; }
.seat-item.occupied { background: #f0f0f0; border-color: #d9d9d9; }
.seat-item.away { background: #e6f7ff; border-color: #1890ff; }
.seat-item.disabled { background: #fff2f0; border-color: #ff4d4f; }
.seat-num { font-size: 14px; font-weight: bold; color: #333; }
.seat-label { font-size: 11px; margin-top: 2px; }
.seat-item.free .seat-label { color: #52c41a; }
.seat-item.reserved .seat-label { color: #fa8c16; }
.seat-item.occupied .seat-label { color: #999; }
.seat-item.away .seat-label { color: #1890ff; }
.legend {
  padding: 16px 20px; display: flex; gap: 18px; justify-content: center;
  font-size: 12px; color: #999;
}
.seat-detail {
  margin: 0 20px 20px; background: white; border-radius: 12px;
  padding: 16px; text-align: center;
}
.seat-detail p { font-size: 14px; color: #333; margin-bottom: 10px; }
.reserve-btn {
  padding: 10px 30px; background: linear-gradient(135deg, #667eea, #764ba2);
  color: white; border: none; border-radius: 10px; font-size: 15px;
  font-weight: bold; cursor: pointer;
}
</style>
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/views/Home.vue
git commit -m "refactor: redesign Home page with top nav, AI button, grid layout"
```

---

### Task 15: SeatSelection.vue — redesign

**Files:**
- Modify: `frontend/src/views/SeatSelection.vue`

- [ ] **Step 1: Rewrite SeatSelection.vue completely**

```vue
<template>
  <div class="page">
    <div class="header">
      <button class="back-btn" @click="$router.back()">←</button>
      <h3>确认预约</h3>
    </div>

    <div class="info-card">
      <div class="info-left">
        <span class="label">座位</span>
        <span class="value">{{ selectedSeat?.seatNumber || '--' }}</span>
      </div>
      <div class="info-right">
        <span class="label">区域</span>
        <span class="value">{{ selectedArea?.name || '--' }} · {{ selectedArea?.floor }}F</span>
      </div>
    </div>

    <div class="section">
      <span class="section-label">选择日期</span>
      <div class="date-row">
        <div v-for="d in dates" :key="d.date"
             :class="['date-chip', { active: selectedDate === d.date }]"
             @click="selectedDate = d.date">
          <span>{{ d.day }}</span>
          <strong>{{ d.short }}</strong>
        </div>
      </div>
    </div>

    <div class="section">
      <span class="section-label">选择时段</span>
      <div class="slot-grid">
        <div v-for="s in slots" :key="s.start"
             :class="['slot', { active: selectedSlot === s, disabled: !s.available }]"
             @click="selectSlot(s)">
          {{ s.start }} - {{ s.end }}
        </div>
      </div>
    </div>

    <button class="confirm-btn" :disabled="!canConfirm" @click="confirmReservation">确认预约</button>
  </div>
</template>

<script>
export default {
  name: 'SeatSelection',
  data() {
    return {
      selectedSeat: null,
      selectedArea: null,
      selectedDate: '',
      selectedSlot: null,
      dates: [],
      slots: [
        { start: '08:00', end: '10:00', available: true },
        { start: '10:00', end: '12:00', available: true },
        { start: '14:00', end: '16:00', available: true },
        { start: '16:00', end: '18:00', available: true },
        { start: '18:00', end: '20:00', available: true },
        { start: '20:00', end: '22:00', available: false },
      ]
    }
  },
  computed: {
    canConfirm() { return this.selectedDate && this.selectedSlot && this.selectedSlot.available }
  },
  mounted() { this.initData(); this.genDates() },
  methods: {
    async initData() {
      const seatId = this.$route.query.seatId, areaId = this.$route.query.areaId
      if (seatId) {
        const r = await fetch(`/api/student/seats/${seatId}`)
        const j = await r.json(); if (j.code === 200) this.selectedSeat = j.data
      }
      if (areaId) {
        const r = await fetch(`/api/student/areas`)
        const j = await r.json()
        if (j.code === 200) this.selectedArea = j.data.find(a => a.id == areaId)
      }
    },
    genDates() {
      const days = ['周日','周一','周二','周三','周四','周五','周六']
      const now = new Date()
      for (let i = 0; i < 7; i++) {
        const d = new Date(now); d.setDate(now.getDate() + i)
        this.dates.push({
          day: days[d.getDay()],
          short: `${d.getMonth()+1}/${d.getDate()}`,
          date: d.toISOString().split('T')[0]
        })
      }
      this.selectedDate = this.dates[0].date
    },
    selectSlot(s) { if (s.available) this.selectedSlot = s },
    async confirmReservation() {
      const user = JSON.parse(localStorage.getItem('user'))
      if (!user) { alert('请先登录'); this.$router.push('/'); return }
      const start = `${this.selectedDate}T${this.selectedSlot.start}:00`
      const end = `${this.selectedDate}T${this.selectedSlot.end}:00`
      const r = await fetch('/api/student/reservations', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({ userId: user.id, seatId: this.selectedSeat.id, startTime: start, endTime: end })
      })
      const j = await r.json()
      if (j.code === 200) { alert('预约成功'); this.$router.push('/my-reservations') }
      else alert(j.message)
    }
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f6fa; padding: 0 20px 30px; }
.header { display: flex; align-items: center; padding: 16px 0; }
.back-btn { background: none; border: none; font-size: 18px; cursor: pointer; color: #666; margin-right: 12px; }
.header h3 { font-size: 17px; color: #333; margin: 0; }
.info-card {
  background: white; border-radius: 12px; padding: 18px; margin-bottom: 20px;
  display: flex; justify-content: space-between; box-shadow: 0 1px 4px rgba(0,0,0,0.05);
}
.info-left, .info-right { display: flex; flex-direction: column; }
.label { font-size: 12px; color: #999; }
.value { font-size: 16px; font-weight: bold; color: #333; margin-top: 4px; }
.section { margin-bottom: 20px; }
.section-label { font-size: 13px; color: #666; display: block; margin-bottom: 8px; }
.date-row { display: flex; gap: 8px; overflow-x: auto; }
.date-chip {
  flex-shrink: 0; padding: 10px 14px; background: white; border: 1.5px solid #e0e0e0;
  border-radius: 10px; text-align: center; cursor: pointer; min-width: 54px;
  font-size: 13px;
}
.date-chip strong { display: block; }
.date-chip.active { background: #667eea; color: white; border-color: #667eea; }
.slot-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
.slot {
  padding: 10px; background: white; border: 1.5px solid #e0e0e0;
  border-radius: 8px; text-align: center; font-size: 13px; cursor: pointer;
}
.slot.active { background: #667eea; color: white; border-color: #667eea; font-weight: bold; }
.slot.disabled { background: #f0f0f0; color: #ccc; cursor: not-allowed; }
.confirm-btn {
  width: 100%; padding: 14px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white; border: none; border-radius: 10px; font-size: 16px;
  font-weight: bold; cursor: pointer; margin-top: 8px;
}
.confirm-btn:disabled { background: #ccc; cursor: not-allowed; }
</style>
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/views/SeatSelection.vue
git commit -m "refactor: redesign SeatSelection page with info card, date chips, slot grid"
```

---

### Task 16: MyReservations.vue — redesign

**Files:**
- Modify: `frontend/src/views/MyReservations.vue`

- [ ] **Step 1: Rewrite MyReservations.vue completely**

```vue
<template>
  <div class="page">
    <div class="tab-bar">
      <div v-for="t in tabs" :key="t.key"
           :class="['tab', { active: activeTab === t.key }]"
           @click="activeTab = t.key">{{ t.label }}</div>
    </div>

    <div v-if="activeTab === 'current'" class="content">
      <div v-if="currentReservations.length === 0" class="empty">暂无当前预约</div>
      <div v-for="r in currentReservations" :key="r.id" class="card">
        <div class="card-left">
          <span class="seat-name">{{ seats[r.seatId]?.seatNumber || '未知' }}</span>
          <span :class="['badge', badgeClass(r.status)]">{{ r.status }}</span>
          <span class="time">📅 {{ fmt(r.startTime) }} - {{ fmt(r.endTime) }}</span>
        </div>
        <div class="card-right">
          <button v-if="r.status === '已预约'" class="btn btn-red" @click="cancelReservation(r.id)">取消</button>
          <button v-if="r.status === '已开始'" class="btn btn-blue" @click="leave(r.id)">暂离</button>
          <button v-if="r.status === '暂离'" class="btn btn-green" @click="back(r.id)">返回</button>
          <button v-if="r.status === '已开始' || r.status === '暂离'" class="btn btn-orange" @click="finish(r.id)">结束</button>
        </div>
      </div>
    </div>

    <div v-if="activeTab === 'history'" class="content">
      <div v-if="historyReservations.length === 0" class="empty">暂无历史预约</div>
      <div v-for="r in historyReservations" :key="r.id" class="card">
        <div class="card-left">
          <span class="seat-name">{{ seats[r.seatId]?.seatNumber || '未知' }}</span>
          <span :class="['badge', badgeClass(r.status)]">{{ r.status }}</span>
          <span class="time">📅 {{ fmt(r.startTime) }} - {{ fmt(r.endTime) }}</span>
        </div>
      </div>
    </div>

    <div v-if="activeTab === 'profile'" class="content">
      <div class="profile-card">
        <div class="info-row"><span>学号</span><span>{{ user?.studentId }}</span></div>
        <div class="info-row"><span>姓名</span><span>{{ user?.name }}</span></div>
        <div class="info-row"><span>违规次数</span><span :class="user?.violationCount > 0 ? 'text-red' : 'text-green'">{{ user?.violationCount || 0 }}</span></div>
        <div class="info-row"><span>账号状态</span><span :class="user?.isRestricted ? 'text-red' : 'text-green'">{{ user?.isRestricted ? '已限制' : '正常' }}</span></div>
      </div>
      <button class="logout-btn" @click="logout">退出登录</button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MyReservations',
  data() {
    return {
      activeTab: 'current',
      tabs: [{ key:'current', label:'当前预约' }, { key:'history', label:'历史预约' }, { key:'profile', label:'个人中心' }],
      currentReservations: [], historyReservations: [], user: null, seats: {}
    }
  },
  mounted() { this.user = JSON.parse(localStorage.getItem('user')); if (this.user) { this.loadCurrent(); this.loadHistory() } },
  methods: {
    async loadCurrent() {
      const r = await fetch(`/api/student/reservations/current?userId=${this.user.id}`)
      const j = await r.json()
      if (j.code === 200) { this.currentReservations = j.data; j.data.forEach(res => this.getSeat(res.seatId)) }
    },
    async loadHistory() {
      const r = await fetch(`/api/student/reservations?userId=${this.user.id}`)
      const j = await r.json()
      if (j.code === 200) { this.historyReservations = j.data.filter(res => ['已结束','已取消','爽约'].includes(res.status)); j.data.forEach(res => this.getSeat(res.seatId)) }
    },
    async getSeat(id) {
      if (!this.seats[id]) { const r = await fetch(`/api/student/seats/${id}`); const j = await r.json(); if (j.code === 200) this.seats[id] = j.data }
    },
    badgeClass(s) { const m = { '已预约':'badge-orange', '已开始':'badge-green', '暂离':'badge-blue', '已结束':'badge-gray', '已取消':'badge-red', '爽约':'badge-red' }; return m[s] || 'badge-gray' },
    fmt(d) { return new Date(d).toLocaleString('zh-CN', { month:'2-digit', day:'2-digit', hour:'2-digit', minute:'2-digit' }) },
    async cancelReservation(id) { await this.action(() => fetch(`/api/student/reservations/${id}/cancel`, { method:'POST', headers:{'Content-Type':'application/x-www-form-urlencoded'}, body:new URLSearchParams({userId:this.user.id}) }), '取消成功') },
    async leave(id) { await this.action(() => fetch(`/api/student/reservations/${id}/leave`, { method:'POST', headers:{'Content-Type':'application/x-www-form-urlencoded'}, body:new URLSearchParams({userId:this.user.id}) }), '暂离成功') },
    async back(id) { await this.action(() => fetch(`/api/student/reservations/${id}/back`, { method:'POST', headers:{'Content-Type':'application/x-www-form-urlencoded'}, body:new URLSearchParams({userId:this.user.id}) }), '返回成功') },
    async finish(id) { await this.action(() => fetch(`/api/student/reservations/${id}/finish`, { method:'POST', headers:{'Content-Type':'application/x-www-form-urlencoded'}, body:new URLSearchParams({userId:this.user.id}) }), '结束成功') },
    async action(fn, ok) { const r = await fn(); const j = await r.json(); if (j.code === 200) { alert(ok); this.loadCurrent(); this.loadHistory() } else alert(j.message) },
    logout() { localStorage.removeItem('user'); this.$router.push('/') }
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f5f6fa; }
.tab-bar { display: flex; background: white; border-bottom: 1px solid #eee; }
.tab { flex: 1; text-align: center; padding: 14px; font-size: 14px; color: #999; cursor: pointer; border-bottom: 2px solid transparent; }
.tab.active { color: #667eea; border-bottom-color: #667eea; font-weight: bold; }
.content { padding: 16px; }
.empty { text-align: center; padding: 80px 0; color: #999; font-size: 16px; }
.card { background: white; border-radius: 12px; padding: 16px; margin-bottom: 12px; display: flex; justify-content: space-between; align-items: flex-start; box-shadow: 0 1px 4px rgba(0,0,0,0.05); }
.card-left { display: flex; flex-direction: column; gap: 4px; }
.seat-name { font-weight: bold; font-size: 16px; color: #333; }
.time { font-size: 13px; color: #666; }
.badge { display: inline-block; padding: 2px 10px; border-radius: 10px; font-size: 11px; font-weight: bold; width: fit-content; }
.badge-green { background: #e6f7ee; color: #52c41a; }
.badge-orange { background: #fff1e6; color: #fa8c16; }
.badge-blue { background: #e6f7ff; color: #1890ff; }
.badge-gray { background: #f0f0f0; color: #999; }
.badge-red { background: #fff2f0; color: #ff4d4f; }
.card-right { display: flex; flex-direction: column; gap: 6px; }
.btn { padding: 5px 12px; border: none; border-radius: 6px; font-size: 12px; cursor: pointer; color: white; }
.btn-red { background: #ff4d4f; }
.btn-blue { background: #1890ff; }
.btn-green { background: #52c41a; }
.btn-orange { background: #fa8c16; }
.profile-card { background: white; border-radius: 12px; padding: 20px; margin-bottom: 20px; }
.info-row { display: flex; justify-content: space-between; padding: 10px 0; font-size: 14px; border-bottom: 1px solid #f5f5f5; }
.info-row:last-child { border-bottom: none; }
.info-row span:first-child { color: #999; }
.info-row span:last-child { color: #333; font-weight: bold; }
.text-red { color: #ff4d4f; }
.text-green { color: #52c41a; }
.logout-btn { width: 100%; padding: 14px; background: #ff4d4f; color: white; border: none; border-radius: 10px; font-size: 16px; font-weight: bold; cursor: pointer; }
</style>
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/views/MyReservations.vue
git commit -m "refactor: redesign MyReservations with tab bar, card list, colored badges"
```

---

### Task 17: Admin frontend pages

**Files:**
- Create: `frontend/src/views/admin/AdminLayout.vue`
- Create: `frontend/src/views/admin/Dashboard.vue`
- Create: `frontend/src/views/admin/AreaManage.vue`
- Create: `frontend/src/views/admin/SeatManage.vue`
- Create: `frontend/src/views/admin/UserManage.vue`

- [ ] **Step 1: Create admin directory**

```bash
mkdir -p frontend/src/views/admin
```

- [ ] **Step 2: Write AdminLayout.vue**

```vue
<template>
  <div class="admin-layout">
    <div class="sidebar">
      <div class="sidebar-title">自习座位管理</div>
      <router-link v-for="item in nav" :key="item.path"
                   :to="item.path" class="nav-item"
                   :class="{ active: $route.path.startsWith(item.path) }">
        {{ item.label }}
      </router-link>
      <div class="sidebar-bottom">
        <button class="logout-btn" @click="logout">退出登录</button>
      </div>
    </div>
    <div class="main">
      <router-view />
    </div>
  </div>
</template>

<script>
export default {
  name: 'AdminLayout',
  data() {
    return {
      nav: [
        { label: '仪表盘', path: '/admin/dashboard' },
        { label: '区域管理', path: '/admin/areas' },
        { label: '座位管理', path: '/admin/seats' },
        { label: '用户管理', path: '/admin/users' },
      ]
    }
  },
  methods: {
    logout() { localStorage.removeItem('user'); this.$router.push('/') }
  }
}
</script>

<style scoped>
.admin-layout { display: flex; min-height: 100vh; }
.sidebar { width: 200px; background: #1a1a2e; color: white; display: flex; flex-direction: column; padding-top: 20px; flex-shrink: 0; }
.sidebar-title { padding: 0 20px 20px; border-bottom: 1px solid #333; margin-bottom: 12px; font-size: 16px; font-weight: bold; color: #667eea; }
.nav-item { display: block; padding: 12px 20px; color: #aaa; text-decoration: none; font-size: 14px; margin: 2px 10px; border-radius: 6px; }
.nav-item:hover { color: white; }
.nav-item.active { background: #667eea; color: white; font-weight: bold; }
.sidebar-bottom { margin-top: auto; padding: 20px; }
.logout-btn { width: 100%; padding: 8px; background: #ff4d4f; color: white; border: none; border-radius: 6px; cursor: pointer; font-size: 13px; }
.main { flex: 1; background: #f5f6fa; padding: 24px; }
</style>
```

- [ ] **Step 3: Write Dashboard.vue**

```vue
<template>
  <div>
    <h3 class="page-title">仪表盘</h3>
    <div class="stats">
      <div class="stat-card green"><div class="num">{{ stats.totalSeats }}</div><div class="desc">总座位数</div></div>
      <div class="stat-card blue"><div class="num">{{ stats.availableSeats }}</div><div class="desc">空闲座位</div></div>
      <div class="stat-card orange"><div class="num">{{ stats.todayReservations }}</div><div class="desc">今日预约</div></div>
      <div class="stat-card red"><div class="num">{{ stats.violationUsers }}</div><div class="desc">违规用户</div></div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Dashboard',
  data() { return { stats: {} } },
  mounted() { this.load() },
  methods: {
    async load() {
      const r = await fetch('/api/admin/dashboard', { headers: { 'X-User-Role': 'admin' } })
      const j = await r.json()
      if (j.code === 200) this.stats = j.data
    }
  }
}
</script>

<style scoped>
.page-title { font-size: 20px; color: #333; margin-bottom: 24px; }
.stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.stat-card { border-radius: 12px; padding: 20px; text-align: center; }
.stat-card.green { background: #e6f7ee; }
.stat-card.blue { background: #e6f7ff; }
.stat-card.orange { background: #fff1e6; }
.stat-card.red { background: #fff2f0; }
.num { font-size: 30px; font-weight: bold; }
.stat-card.green .num { color: #52c41a; }
.stat-card.blue .num { color: #1890ff; }
.stat-card.orange .num { color: #fa8c16; }
.stat-card.red .num { color: #ff4d4f; }
.desc { font-size: 13px; color: #666; margin-top: 4px; }
</style>
```

- [ ] **Step 4: Write AreaManage.vue**

```vue
<template>
  <div>
    <div class="page-header">
      <h3>区域管理</h3>
      <button class="add-btn" @click="openModal(null)">+ 新增区域</button>
    </div>
    <table class="table" v-if="areas.length">
      <thead><tr><th>ID</th><th>名称</th><th>楼层</th><th>状态</th><th>操作</th></tr></thead>
      <tbody>
        <tr v-for="a in areas" :key="a.id">
          <td>{{ a.id }}</td><td>{{ a.name }}</td><td>{{ a.floor }}F</td>
          <td><span :class="a.isEnabled ? 'badge-green' : 'badge-red'">{{ a.isEnabled ? '启用' : '禁用' }}</span></td>
          <td>
            <button class="link-btn" @click="openModal(a)">编辑</button>
            <button :class="['link-btn', a.isEnabled ? 'text-red' : 'text-green']" @click="toggleArea(a)">{{ a.isEnabled ? '禁用' : '启用' }}</button>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal-card">
        <h4>{{ editId ? '编辑' : '新增' }}区域</h4>
        <div class="form-group"><label>名称</label><input v-model="form.name" /></div>
        <div class="form-group"><label>楼层</label><input v-model="form.floor" type="number" /></div>
        <div class="modal-btns">
          <button class="btn-primary" @click="save">保存</button>
          <button class="btn-cancel" @click="showModal = false">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AreaManage',
  data() { return { areas: [], showModal: false, editId: null, form: { name: '', floor: 1 } } },
  mounted() { this.load() },
  methods: {
    async load() { const r = await fetch('/api/admin/areas', { headers: { 'X-User-Role': 'admin' } }); const j = await r.json(); if (j.code === 200) this.areas = j.data },
    openModal(a) { if (a) { this.editId = a.id; this.form = { name: a.name, floor: a.floor } } else { this.editId = null; this.form = { name: '', floor: 1 } } this.showModal = true },
    async save() {
      const h = { 'Content-Type': 'application/x-www-form-urlencoded', 'X-User-Role': 'admin' }
      const body = new URLSearchParams({ name: this.form.name, floor: this.form.floor })
      const r = this.editId
        ? await fetch(`/api/admin/areas/${this.editId}`, { method: 'PUT', headers: h, body })
        : await fetch('/api/admin/areas', { method: 'POST', headers: h, body })
      const j = await r.json()
      if (j.code === 200) { alert('保存成功'); this.showModal = false; this.load() } else alert(j.message)
    },
    async toggleArea(a) {
      const r = await fetch(`/api/admin/areas/${a.id}/disable?disabled=${a.isEnabled}`, { method: 'PUT', headers: { 'X-User-Role': 'admin' } })
      const j = await r.json(); if (j.code === 200) this.load(); else alert(j.message)
    }
  }
}
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h3 { font-size: 18px; color: #333; margin: 0; }
.add-btn { padding: 8px 18px; background: #667eea; color: white; border: none; border-radius: 8px; cursor: pointer; font-size: 13px; }
.table { width: 100%; background: white; border-radius: 10px; overflow: hidden; border-collapse: collapse; font-size: 14px; }
.table th { background: #f5f5f5; padding: 10px; text-align: left; }
.table td { padding: 10px; border-bottom: 1px solid #eee; }
.badge-green { background: #e6f7ee; color: #52c41a; padding: 2px 10px; border-radius: 10px; font-size: 12px; }
.badge-red { background: #fff2f0; color: #ff4d4f; padding: 2px 10px; border-radius: 10px; font-size: 12px; }
.link-btn { background: none; border: none; cursor: pointer; font-size: 13px; color: #667eea; margin-right: 8px; }
.text-red { color: #ff4d4f; }
.text-green { color: #52c41a; }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-card { background: white; border-radius: 14px; padding: 28px; width: 380px; }
.modal-card h4 { margin-bottom: 20px; color: #333; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 13px; color: #666; margin-bottom: 6px; }
.form-group input { width: 100%; padding: 9px 12px; border: 1.5px solid #e0e0e0; border-radius: 8px; font-size: 14px; }
.modal-btns { display: flex; gap: 12px; margin-top: 20px; }
.btn-primary { flex: 1; padding: 10px; background: #667eea; color: white; border: none; border-radius: 8px; cursor: pointer; }
.btn-cancel { flex: 1; padding: 10px; background: #f0f0f0; border: none; border-radius: 8px; cursor: pointer; }
</style>
```

- [ ] **Step 5: Write SeatManage.vue**

```vue
<template>
  <div>
    <div class="page-header">
      <div><h3>座位管理</h3><select v-model="filterArea" @change="load" class="area-filter"><option value="">全部区域</option><option v-for="a in areas" :key="a.id" :value="a.id">{{ a.name }}</option></select></div>
      <button class="add-btn" @click="openModal(null)">+ 新增座位</button>
    </div>
    <table class="table" v-if="seats.length">
      <thead><tr><th>座位号</th><th>区域</th><th>状态</th><th>操作</th></tr></thead>
      <tbody>
        <tr v-for="s in seats" :key="s.id">
          <td>{{ s.seatNumber }}</td><td>{{ areaName(s.areaId) }}</td>
          <td><span :class="['badge', statusBadge(s.status)]">{{ s.status }}</span></td>
          <td>
            <button class="link-btn" @click="openModal(s)">编辑</button>
            <button v-if="s.status === '占用' || s.status === '暂离'" class="link-btn text-orange" @click="release(s.id)">释放</button>
            <button :class="['link-btn', s.isEnabled ? 'text-red' : 'text-green']" @click="toggleSeat(s)">{{ s.isEnabled ? '禁用' : '启用' }}</button>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal-card">
        <h4>{{ editId ? '编辑' : '新增' }}座位</h4>
        <div class="form-group"><label>区域</label><select v-model="form.areaId"><option v-for="a in areas" :key="a.id" :value="a.id">{{ a.name }}</option></select></div>
        <div class="form-group"><label>座位号</label><input v-model="form.seatNumber" /></div>
        <div class="modal-btns"><button class="btn-primary" @click="save">保存</button><button class="btn-cancel" @click="showModal = false">取消</button></div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SeatManage',
  data() { return { seats: [], areas: [], filterArea: '', showModal: false, editId: null, form: { areaId: '', seatNumber: '' } } },
  mounted() { this.loadAreas(); this.load() },
  methods: {
    async loadAreas() { const r = await fetch('/api/admin/areas', { headers: { 'X-User-Role': 'admin' } }); const j = await r.json(); if (j.code === 200) this.areas = j.data },
    async load() { const q = this.filterArea ? `?areaId=${this.filterArea}` : ''; const r = await fetch(`/api/admin/seats${q}`, { headers: { 'X-User-Role': 'admin' } }); const j = await r.json(); if (j.code === 200) this.seats = j.data },
    areaName(id) { const a = this.areas.find(x => x.id === id); return a ? a.name : '--' },
    statusBadge(s) { const m = { '空闲':'badge-green', '已预约':'badge-orange', '占用':'badge-gray', '暂离':'badge-blue', '禁用':'badge-red' }; return m[s] || 'badge-gray' },
    openModal(s) { if (s) { this.editId = s.id; this.form = { areaId: s.areaId, seatNumber: s.seatNumber } } else { this.editId = null; this.form = { areaId: '', seatNumber: '' } } this.showModal = true },
    async save() {
      const h = { 'Content-Type': 'application/x-www-form-urlencoded', 'X-User-Role': 'admin' }
      const body = new URLSearchParams({ areaId: this.form.areaId, seatNumber: this.form.seatNumber })
      const r = this.editId
        ? await fetch(`/api/admin/seats/${this.editId}`, { method: 'PUT', headers: h, body })
        : await fetch('/api/admin/seats', { method: 'POST', headers: h, body })
      const j = await r.json(); if (j.code === 200) { alert('保存成功'); this.showModal = false; this.load() } else alert(j.message)
    },
    async release(id) { const r = await fetch(`/api/admin/seats/${id}/release`, { method: 'PUT', headers: { 'X-User-Role': 'admin' } }); const j = await r.json(); if (j.code === 200) this.load(); else alert(j.message) },
    async toggleSeat(s) { const r = await fetch(`/api/admin/seats/${s.id}/disable?disabled=${s.isEnabled}`, { method: 'PUT', headers: { 'X-User-Role': 'admin' } }); const j = await r.json(); if (j.code === 200) this.load(); else alert(j.message) }
  }
}
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h3 { font-size: 18px; color: #333; margin: 0 0 8px 0; }
.area-filter { margin-top: 6px; padding: 6px 12px; border: 1.5px solid #e0e0e0; border-radius: 8px; font-size: 14px; }
.add-btn { padding: 8px 18px; background: #667eea; color: white; border: none; border-radius: 8px; cursor: pointer; font-size: 13px; height: fit-content; }
.table { width: 100%; background: white; border-radius: 10px; overflow: hidden; border-collapse: collapse; font-size: 14px; }
.table th { background: #f5f5f5; padding: 10px; text-align: left; }
.table td { padding: 10px; border-bottom: 1px solid #eee; }
.badge { display: inline-block; padding: 2px 10px; border-radius: 10px; font-size: 11px; font-weight: bold; }
.badge-green { background: #e6f7ee; color: #52c41a; }
.badge-orange { background: #fff1e6; color: #fa8c16; }
.badge-blue { background: #e6f7ff; color: #1890ff; }
.badge-gray { background: #f0f0f0; color: #999; }
.badge-red { background: #fff2f0; color: #ff4d4f; }
.link-btn { background: none; border: none; cursor: pointer; font-size: 13px; color: #667eea; margin-right: 8px; }
.text-red { color: #ff4d4f; }
.text-green { color: #52c41a; }
.text-orange { color: #fa8c16; }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-card { background: white; border-radius: 14px; padding: 28px; width: 380px; }
.modal-card h4 { margin-bottom: 20px; color: #333; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 13px; color: #666; margin-bottom: 6px; }
.form-group input, .form-group select { width: 100%; padding: 9px 12px; border: 1.5px solid #e0e0e0; border-radius: 8px; font-size: 14px; }
.modal-btns { display: flex; gap: 12px; margin-top: 20px; }
.btn-primary { flex: 1; padding: 10px; background: #667eea; color: white; border: none; border-radius: 8px; cursor: pointer; }
.btn-cancel { flex: 1; padding: 10px; background: #f0f0f0; border: none; border-radius: 8px; cursor: pointer; }
</style>
```

- [ ] **Step 6: Write UserManage.vue**

```vue
<template>
  <div>
    <h3 class="page-title">用户管理</h3>
    <table class="table" v-if="users.length">
      <thead><tr><th>学号</th><th>姓名</th><th>违规次数</th><th>状态</th><th>操作</th></tr></thead>
      <tbody>
        <tr v-for="u in users" :key="u.id">
          <td>{{ u.studentId }}</td><td>{{ u.name }}</td>
          <td>{{ u.violationCount }}</td>
          <td><span :class="u.isRestricted ? 'badge-red' : 'badge-green'">{{ u.isRestricted ? '已限制' : '正常' }}</span></td>
          <td>
            <button :class="['link-btn', u.isRestricted ? 'text-green' : 'text-red']" @click="toggleBan(u)">{{ u.isRestricted ? '解封' : '封禁' }}</button>
            <button class="link-btn" @click="resetViolation(u.id)">重置违规</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
export default {
  name: 'UserManage',
  data() { return { users: [] } },
  mounted() { this.load() },
  methods: {
    async load() { const r = await fetch('/api/admin/users', { headers: { 'X-User-Role': 'admin' } }); const j = await r.json(); if (j.code === 200) this.users = j.data },
    async toggleBan(u) {
      if (!confirm(`确定${u.isRestricted ? '解封' : '封禁'}用户 ${u.name} 吗？`)) return
      const r = await fetch(`/api/admin/users/${u.id}/ban?banned=${!u.isRestricted}`, { method: 'PUT', headers: { 'X-User-Role': 'admin' } })
      const j = await r.json(); if (j.code === 200) this.load(); else alert(j.message)
    },
    async resetViolation(id) {
      if (!confirm('确定重置违规次数吗？')) return
      const r = await fetch(`/api/admin/users/${id}/violation-reset`, { method: 'PUT', headers: { 'X-User-Role': 'admin' } })
      const j = await r.json(); if (j.code === 200) this.load(); else alert(j.message)
    }
  }
}
</script>

<style scoped>
.page-title { font-size: 18px; color: #333; margin-bottom: 20px; }
.table { width: 100%; background: white; border-radius: 10px; overflow: hidden; border-collapse: collapse; font-size: 14px; }
.table th { background: #f5f5f5; padding: 10px; text-align: left; }
.table td { padding: 10px; border-bottom: 1px solid #eee; }
.badge-green { background: #e6f7ee; color: #52c41a; padding: 2px 10px; border-radius: 10px; font-size: 12px; }
.badge-red { background: #fff2f0; color: #ff4d4f; padding: 2px 10px; border-radius: 10px; font-size: 12px; }
.link-btn { background: none; border: none; cursor: pointer; font-size: 13px; color: #667eea; margin-right: 8px; }
.text-red { color: #ff4d4f; }
.text-green { color: #52c41a; }
</style>
```

- [ ] **Step 7: Commit all admin pages**

```bash
git add frontend/src/views/admin/
git commit -m "feat: add admin frontend — AdminLayout, Dashboard, Area/Seat/User management pages"
```

---

### Task 18: ChatPage.vue — AI assistant page

**Files:**
- Create: `frontend/src/views/ChatPage.vue`

- [ ] **Step 1: Write ChatPage.vue**

```vue
<template>
  <div class="chat-page">
    <div class="chat-header">
      <button class="back-btn" @click="$router.back()">←</button>
      <div class="ai-avatar">🤖</div>
      <div class="ai-info"><span class="ai-name">自习助手</span><span class="ai-status">在线 · DeepSeek</span></div>
    </div>

    <div class="messages" ref="msgContainer">
      <div v-for="(m, i) in messages" :key="i"
           :class="['msg-row', m.role === 'user' ? 'row-user' : 'row-bot']">
        <div v-if="m.role === 'bot'" class="bot-avatar">🤖</div>
        <div :class="['bubble', m.role === 'user' ? 'bubble-user' : 'bubble-bot']" v-html="formatMsg(m.content)"></div>
      </div>
      <div v-if="loading" class="msg-row row-bot">
        <div class="bot-avatar">🤖</div>
        <div class="bubble bubble-bot typing">对方正在输入...</div>
      </div>
    </div>

    <div class="input-area">
      <input v-model="input" placeholder="输入消息..." @keyup.enter="send" />
      <button class="send-btn" @click="send" :disabled="!input.trim() || loading">→</button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ChatPage',
  data() {
    return {
      messages: [{ role: 'bot', content: '你好！我是校园自习助手 🎓\n\n我可以帮你：\n• 预约自习座位\n• 查询可用座位\n• 规划学习计划\n• 管理你的预约\n\n请问有什么需要？' }],
      input: '',
      loading: false,
      conversationId: crypto.randomUUID(),
    }
  },
  methods: {
    async send() {
      if (!this.input.trim() || this.loading) return
      const msg = this.input.trim()
      this.input = ''
      this.messages.push({ role: 'user', content: msg })
      this.loading = true
      this.$nextTick(() => this.scrollBottom())

      try {
        const user = JSON.parse(localStorage.getItem('user'))
        const r = await fetch(`/api/student/chat?userId=${user.id}`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ message: msg, conversationId: this.conversationId })
        })
        const j = await r.json()
        if (j.code === 200) {
          this.messages.push({ role: 'bot', content: j.data.reply })
        } else {
          this.messages.push({ role: 'bot', content: '抱歉，' + j.message })
        }
      } catch (e) {
        this.messages.push({ role: 'bot', content: '网络错误，请稍后重试' })
      } finally {
        this.loading = false
        this.$nextTick(() => this.scrollBottom())
      }
    },
    formatMsg(t) {
      return t.replace(/\n/g, '<br>')
    },
    scrollBottom() {
      const el = this.$refs.msgContainer
      if (el) el.scrollTop = el.scrollHeight
    }
  }
}
</script>

<style scoped>
.chat-page { height: 100vh; display: flex; flex-direction: column; background: #f5f6fa; }
.chat-header {
  background: linear-gradient(135deg, #667eea, #764ba2); color: white;
  padding: 14px 16px; display: flex; align-items: center; gap: 10px;
  flex-shrink: 0;
}
.back-btn { background: none; border: none; color: white; font-size: 18px; cursor: pointer; }
.ai-avatar { width: 36px; height: 36px; background: rgba(255,255,255,0.2); border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 18px; }
.ai-name { font-weight: bold; font-size: 16px; display: block; }
.ai-status { font-size: 12px; opacity: 0.8; }
.messages { flex: 1; overflow-y: auto; padding: 16px; display: flex; flex-direction: column; gap: 12px; }
.msg-row { display: flex; gap: 8px; align-items: flex-start; max-width: 85%; }
.row-user { align-self: flex-end; }
.row-bot { align-self: flex-start; }
.bot-avatar { width: 32px; height: 32px; background: #667eea; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; font-size: 14px; flex-shrink: 0; }
.bubble { padding: 10px 14px; border-radius: 14px; font-size: 14px; line-height: 1.6; }
.bubble-bot { background: white; color: #333; border-bottom-left-radius: 4px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
.bubble-user { background: #667eea; color: white; border-bottom-right-radius: 4px; }
.typing { color: #999; font-style: italic; }
.input-area { padding: 12px 16px; background: white; border-top: 1px solid #eee; display: flex; gap: 10px; flex-shrink: 0; }
.input-area input { flex: 1; padding: 10px 14px; border: 1.5px solid #e0e0e0; border-radius: 20px; font-size: 14px; outline: none; }
.input-area input:focus { border-color: #667eea; }
.send-btn { width: 40px; height: 40px; background: #667eea; color: white; border: none; border-radius: 50%; cursor: pointer; font-size: 18px; }
.send-btn:disabled { background: #ccc; cursor: not-allowed; }
</style>
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/views/ChatPage.vue
git commit -m "feat: add AI assistant chat page"
```

---

### Task 19: End-to-end verification

- [ ] **Step 1: Start all services**

Terminal 1 (Java):
```bash
cd backend && mvn spring-boot:run
```

Terminal 2 (Python):
```bash
cd ai-service && python main.py
```

Terminal 3 (Frontend):
```bash
cd frontend && npm run dev
```

- [ ] **Step 2: Verify admin flow**

1. Open http://localhost:5173 → login as `admin` / `admin123`
2. Verify redirect to `/admin/dashboard` with stats
3. Navigate to 区域管理 → create a new area → verify in table
4. Navigate to 座位管理 → add a seat to the new area → verify
5. Navigate to 用户管理 → find a student → ban/unban → verify
6. Logout → login as student → verify student sees home page

- [ ] **Step 3: Verify AI chat flow**

1. Login as student → click "AI 助手" button
2. Type "帮我查一下明天下午的空位" → verify AI responds with seat options
3. Type "帮我预约" → verify AI creates reservation

- [ ] **Step 4: Verify student redesign**

1. Check Login page has gradient bg + rounded card
2. Check Home page has top nav + seat grid + legend
3. Click a seat → check SeatSelection has info card + date chips
4. Check MyReservations has tab bar + card list

- [ ] **Step 5: Commit any fixes if needed**
