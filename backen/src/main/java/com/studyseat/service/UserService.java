package com.studyseat.service;

import com.studyseat.entity.User;

public interface UserService {
    User login(String studentId, String password);
    User register(String studentId, String name);
    void updatePassword(Long userId, String oldPassword, String newPassword);
    User getById(Long userId);
    void updateViolationCount(Long userId);
    void checkRestriction(Long userId);
}