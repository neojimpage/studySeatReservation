package com.studyseat.controller;

import com.studyseat.entity.Reservation;
import com.studyseat.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/student")
public class ReservationController {
    
    @Autowired
    private ReservationService reservationService;
    
    @PostMapping("/reservations")
    public Map<String, Object> createReservation(@RequestParam Long userId, @RequestParam Long seatId, @RequestParam String startTime, @RequestParam String endTime) {
        Map<String, Object> result = new HashMap<>();
        try {
            LocalDateTime start = LocalDateTime.parse(startTime);
            LocalDateTime end = LocalDateTime.parse(endTime);
            Reservation reservation = reservationService.createReservation(userId, seatId, start, end);
            if (reservation != null) {
                result.put("code", 200);
                result.put("message", "预约成功");
                result.put("data", reservation);
            } else {
                result.put("code", 400);
                result.put("message", "预约失败，座位已被占用或超过预约次数");
                result.put("data", null);
            }
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", "预约失败，时间格式错误");
            result.put("data", null);
        }
        return result;
    }
    
    @GetMapping("/reservations/current")
    public Map<String, Object> getCurrentReservations(@RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        List<Reservation> reservations = reservationService.getCurrentReservations(userId);
        result.put("code", 200);
        result.put("message", "获取成功");
        result.put("data", reservations);
        return result;
    }
    
    @GetMapping("/reservations")
    public Map<String, Object> getReservations(@RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        List<Reservation> reservations = reservationService.getReservations(userId);
        result.put("code", 200);
        result.put("message", "获取成功");
        result.put("data", reservations);
        return result;
    }
    
    @PostMapping("/reservations/{id}/cancel")
    public Map<String, Object> cancelReservation(@PathVariable Long id, @RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            reservationService.cancelReservation(id, userId);
            result.put("code", 200);
            result.put("message", "取消成功");
            result.put("data", null);
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
            result.put("data", null);
        }
        return result;
    }
    
    @PostMapping("/reservations/{id}/leave")
    public Map<String, Object> leave(@PathVariable Long id, @RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            reservationService.leave(id, userId);
            result.put("code", 200);
            result.put("message", "暂离成功");
            result.put("data", null);
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
            result.put("data", null);
        }
        return result;
    }
    
    @PostMapping("/reservations/{id}/back")
    public Map<String, Object> back(@PathVariable Long id, @RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            reservationService.back(id, userId);
            result.put("code", 200);
            result.put("message", "返回成功");
            result.put("data", null);
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
            result.put("data", null);
        }
        return result;
    }
    
    @PostMapping("/reservations/{id}/finish")
    public Map<String, Object> finish(@PathVariable Long id, @RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            reservationService.finish(id, userId);
            result.put("code", 200);
            result.put("message", "结束成功");
            result.put("data", null);
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
            result.put("data", null);
        }
        return result;
    }
}