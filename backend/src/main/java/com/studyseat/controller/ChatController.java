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
