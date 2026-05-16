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
