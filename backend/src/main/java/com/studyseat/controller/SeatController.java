package com.studyseat.controller;

import com.studyseat.entity.Seat;
import com.studyseat.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class SeatController {
    
    @Autowired
    private SeatService seatService;
    
    @GetMapping("/seats")
    public Map<String, Object> getSeats(@RequestParam Long areaId) {
        Map<String, Object> result = new HashMap<>();
        List<Seat> seats = seatService.getSeatsByAreaId(areaId);
        result.put("code", 200);
        result.put("message", "获取成功");
        result.put("data", seats);
        return result;
    }
    
    @GetMapping("/seats/{id}")
    public Map<String, Object> getSeat(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        Seat seat = seatService.getById(id);
        if (seat != null) {
            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", seat);
        } else {
            result.put("code", 404);
            result.put("message", "座位不存在");
            result.put("data", null);
        }
        return result;
    }
}