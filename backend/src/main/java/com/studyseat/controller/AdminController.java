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
