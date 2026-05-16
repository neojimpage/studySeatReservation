# Admin Panel Design Spec

## Overview

Add admin-side functionality to the campus study seat reservation system. A single preset admin account can manage areas, seats, and users through a web UI integrated into the existing Vue 3 frontend.

**Scope**: backend API + frontend pages. The AI chat agent is a separate spec.

## Data Model

### User table — add `role` field

```sql
ALTER TABLE user ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'student';
```

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| role | VARCHAR(20) | `student` | `student` or `admin` |

### Preset admin account

Inserted via `DataInitializer` at startup if not exists:

- studentId: `admin`
- password: `admin123` (plaintext, matching existing password handling)
- name: `管理员`
- role: `admin`

## Backend API

### Auth interceptor / role check

All `/api/admin/*` endpoints require the request to carry a logged-in user session with `role = admin`. Requests without admin role return `{ code: 401, message: "无管理员权限" }`.

Implementation: an `AdminAuthInterceptor` registered via Spring MVC `WebMvcConfigurer`, matching `/api/admin/**` paths.

### Login modification

Modify `POST /api/student/login` response `data` to include `role` field so the frontend can route to admin or student home.

### Admin endpoints

Base path: `/api/admin`

#### Area management

| Method | Path | Body/Params | Response |
|--------|------|-------------|----------|
| GET | `/areas` | — | List of all areas (including disabled) |
| POST | `/areas` | `name`, `floor` | Created area |
| PUT | `/areas/{id}` | `name`, `floor` | Updated area |
| PUT | `/areas/{id}/disable` | `disabled` (boolean) | Toggle area enabled/disabled |

#### Seat management

| Method | Path | Body/Params | Response |
|--------|------|-------------|----------|
| GET | `/seats` | `areaId` (optional) | List of seats, filtered by area if provided |
| POST | `/seats` | `areaId`, `seatNumber` | Created seat |
| PUT | `/seats/{id}` | `areaId`, `seatNumber` | Updated seat |
| PUT | `/seats/{id}/disable` | `disabled` (boolean) | Toggle seat enabled/disabled |
| PUT | `/seats/{id}/release` | — | Set seat status to `空闲`, cancel any active reservation on it |

Release logic: if the seat has an active reservation with status `已开始` or `暂离`, set that reservation to `已结束`. Then set seat status to `空闲`.

#### User management

| Method | Path | Body/Params | Response |
|--------|------|-------------|----------|
| GET | `/users` | — | List of all users (excluding admin), with violationCount and isRestricted |
| PUT | `/users/{id}/ban` | `banned` (boolean) | Set user `isRestricted` to true/false |
| PUT | `/users/{id}/violation-reset` | — | Set user `violationCount` to 0, set `isRestricted` to false |

### Reuse: Area/Seat admin endpoints consolidate logic in AdminAreaService / AdminSeatService

These extend the existing AreaService/SeatService where possible. Controller: `AdminController`.

## Frontend

### Router additions

Add admin routes in `frontend/src/router/index.js`:

```
/admin/dashboard  → AdminLayout + Dashboard
/admin/areas       → AdminLayout + AreaManage
/admin/seats       → AdminLayout + SeatManage
/admin/users       → AdminLayout + UserManage
/admin             → redirect to /admin/dashboard
```

### Login routing

In `Login.vue`, after successful login, check `result.data.role`:
- `admin` → `this.$router.push('/admin/dashboard')`
- otherwise → `this.$router.push('/home')`

### AdminLayout component

A wrapper component with:
- Left sidebar (200px, dark background `#1a1a2e`, matching app purple theme)
- Navigation items: 仪表盘, 区域管理, 座位管理, 用户管理 — active item highlighted with `#667eea`
- Logout button at sidebar bottom — calls same logout logic as student side
- Right content area renders `<router-view />`

### Dashboard page

4 stat cards in a grid: total seats, available seats, today's reservations, users with violations.

Stats fetched from a single endpoint: `GET /api/admin/dashboard` returning `{ totalSeats, availableSeats, todayReservations, violationUsers }`.

### Area management page

- Table: ID, name, floor, seat count, status (enabled/disabled), actions
- "新增区域" button opens a modal with name + floor fields
- "编辑" opens same modal pre-filled; "禁用"/"启用" toggles the area

### Seat management page

- Area filter dropdown at top
- Table: seat number, area, status (with colored badges matching student-side colors), actions
- Actions column: always shows 编辑; conditionally shows 释放 (when occupied/away), 禁用/启用
- "新增座位" button opens modal: select area + enter seat number

### User management page

- Table: student ID, name, violation count, restriction status badge, actions
- Actions: 封禁/解封 toggle, 重置违规
- Confirm dialog for ban/reset actions to prevent misclicks

### Data fetching

All admin pages call `/api/admin/*` endpoints via `fetch`. User session (id, role) stored in `localStorage.user` — admin pages check role on mount; redirect to `/` if not admin.

## Error Handling

- Backend: consistent `{ code, message, data }` response format, same as existing student API
- 401 returned when non-admin user accesses `/api/admin/*`
- 404 returned when entity not found
- 400 returned on validation failure (e.g., duplicate seat number in same area)
- Frontend: `alert(result.message)` on error, same pattern as existing pages

## Files to Create / Modify

### New files

| File | Purpose |
|------|---------|
| `backend/.../controller/AdminController.java` | Admin REST endpoints |
| `backend/.../service/AdminService.java` | Admin business logic interface |
| `backend/.../service/impl/AdminServiceImpl.java` | Admin business logic |
| `backend/.../config/AdminAuthInterceptor.java` | Role check interceptor |
| `backend/.../config/WebMvcConfig.java` | Register interceptor (or modify existing) |
| `frontend/src/views/admin/AdminLayout.vue` | Admin wrapper with sidebar |
| `frontend/src/views/admin/Dashboard.vue` | Dashboard with stats |
| `frontend/src/views/admin/AreaManage.vue` | Area CRUD page |
| `frontend/src/views/admin/SeatManage.vue` | Seat CRUD page |
| `frontend/src/views/admin/UserManage.vue` | User management page |

### Modified files

| File | Change |
|------|--------|
| `backend/.../entity/User.java` | Add `role` field |
| `backend/.../controller/UserController.java` | Include `role` in login response |
| `backend/.../service/DataInitializer.java` | Insert preset admin account |
| `backend/.../config/WebConfig.java` | Register admin auth interceptor |
| `frontend/src/router/index.js` | Add admin routes |
| `frontend/src/views/Login.vue` | Role-based redirect after login |
| `frontend/src/App.vue` | Keep existing gradient background for student side; admin layout handles its own background |

## Testing

- Unit tests: AdminService methods (create/update area, release seat, ban user, reset violations)
- Integration tests: AdminController endpoints verify 401 for non-admin, correct CRUD responses
- Manual test flow: login as admin → dashboard displays stats → create area → add seat to area → ban a user → login as that user and verify restriction
