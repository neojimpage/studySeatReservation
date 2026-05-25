package com.studyseat.service.impl;

import com.studyseat.entity.User;
import com.studyseat.mapper.UserMapper;
import com.studyseat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public User login(String studentId, String password) {
        User user = userMapper.findByStudentId(studentId);
        if (user == null) {
            return null;
        }
        if (password != null && password.equals(user.getPassword())) {
            user.setLastLoginTime(LocalDateTime.now());
            userMapper.updateById(user);
            return user;
        }
        return null;
    }
    
    @Override
    public User register(String studentId, String name) {
        if (studentId == null || studentId.isBlank()) {
            throw new IllegalArgumentException("学号不能为空");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("姓名不能为空");
        }
        User existingUser = userMapper.findByStudentId(studentId);
        if (existingUser != null) {
            return null;
        }
        User user = new User();
        user.setStudentId(studentId);
        user.setName(name);
        int len = studentId.length();
        user.setPassword(studentId.substring(Math.max(0, len - 6)));
        user.setViolationCount(0);
        user.setIsRestricted(false);
        user.setRole("student");
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
        return user;
    }
    
    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user != null && oldPassword.equals(user.getPassword())) {
            user.setPassword(newPassword);
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
        }
    }
    
    @Override
    public User getById(Long userId) {
        return userMapper.selectById(userId);
    }
    
    @Override
    public void updateViolationCount(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setViolationCount(user.getViolationCount() + 1);
            if (user.getViolationCount() >= 3) {
                user.setIsRestricted(true);
            }
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(user);
        }
    }
    
    @Override
    public void checkRestriction(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null && user.getIsRestricted()) {
            // 检查是否过了24小时
            if (LocalDateTime.now().isAfter(user.getUpdateTime().plusHours(24))) {
                user.setIsRestricted(false);
                user.setUpdateTime(LocalDateTime.now());
                userMapper.updateById(user);
            }
        }
    }
}