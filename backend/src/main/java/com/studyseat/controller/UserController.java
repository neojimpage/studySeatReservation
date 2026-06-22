package com.studyseat.controller;

import com.studyseat.entity.User;
import com.studyseat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/login")
    public Map<String, Object> login(@RequestParam String studentId, @RequestParam String password) {
        Map<String, Object> result = new HashMap<>();
        User user = userService.login(studentId, password);
        if (user != null) {
            result.put("code", 200);
            result.put("message", "登录成功");
            result.put("data", user);
        } else {
            result.put("code", 401);
            result.put("message", "学号或姓名错误");
            result.put("data", null);
        }
        return result;
    }
    
    @PostMapping("/register")
    public Map<String, Object> register(@RequestParam String studentId, @RequestParam String name) {
        Map<String, Object> result = new HashMap<>();
        try {
            User user = userService.register(studentId, name);
            if (user != null) {
                result.put("code", 200);
                result.put("message", "注册成功");
                result.put("data", user);
            } else {
                result.put("code", 400);
                result.put("message", "学号已存在");
                result.put("data", null);
            }
        } catch (IllegalArgumentException e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
            result.put("data", null);
        }
        return result;
    }
    
    @PostMapping("/updatePassword")
    public Map<String, Object> updatePassword(@RequestParam Long userId, @RequestParam String oldPassword, @RequestParam String newPassword) {
        Map<String, Object> result = new HashMap<>();
        try {
            userService.updatePassword(userId, oldPassword, newPassword);
            result.put("code", 200);
            result.put("message", "密码更新成功");
            result.put("data", null);
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", "密码更新失败");
            result.put("data", null);
        }
        return result;
    }
    
    @GetMapping("/user/{id}")
    public Map<String, Object> getUser(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        User user = userService.getById(id);
        if (user != null) {
            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", user);
        } else {
            result.put("code", 404);
            result.put("message", "用户不存在");
            result.put("data", null);
        }
        return result;
    }
}