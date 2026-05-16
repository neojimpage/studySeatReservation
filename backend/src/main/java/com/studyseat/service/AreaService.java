package com.studyseat.service;

import com.studyseat.entity.Area;
import java.util.List;

public interface AreaService {
    List<Area> getAllAreas();
    Area getById(Long id);
}