package com.studyseat.service.impl;

import com.studyseat.entity.Area;
import com.studyseat.mapper.AreaMapper;
import com.studyseat.service.AreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AreaServiceImpl implements AreaService {
    
    @Autowired
    private AreaMapper areaMapper;
    
    @Override
    public List<Area> getAllAreas() {
        return areaMapper.selectList(null);
    }
    
    @Override
    public Area getById(Long id) {
        return areaMapper.selectById(id);
    }
}