package com.studyseat.controller;

import com.studyseat.entity.Area;
import com.studyseat.service.AreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class AreaController {
    
    @Autowired
    private AreaService areaService;
    
    @GetMapping("/areas")
    public Map<String, Object> getAreas() {
        Map<String, Object> result = new HashMap<>();
        List<Area> areas = areaService.getAllAreas();
        result.put("code", 200);
        result.put("message", "获取成功");
        result.put("data", areas);
        return result;
    }
    
    @GetMapping("/areas/{id}")
    public Map<String, Object> getArea(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        Area area = areaService.getById(id);
        if (area != null) {
            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", area);
        } else {
            result.put("code", 404);
            result.put("message", "区域不存在");
            result.put("data", null);
        }
        return result;
    }
}