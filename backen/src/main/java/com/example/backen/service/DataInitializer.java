package com.example.backen.service;

import com.example.backen.mapper.AreaMapper;
import com.example.backen.mapper.SeatMapper;
import com.example.backen.model.Area;
import com.example.backen.model.Seat;
import com.example.backen.model.SeatStatus;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class DataInitializer {

    private final AreaMapper areaMapper;
    private final SeatMapper seatMapper;

    public DataInitializer(AreaMapper areaMapper, SeatMapper seatMapper) {
        this.areaMapper = areaMapper;
        this.seatMapper = seatMapper;
    }

    @PostConstruct
    public void init() {
        // create sample areas and seats
        Area a1 = new Area();
        a1.setName("图书馆一楼");
        areaMapper.insert(a1);

        Area a2 = new Area();
        a2.setName("图书馆二楼");
        areaMapper.insert(a2);

        long id = 1L;
        for (long areaId = 1L; areaId <= 2L; areaId++) {
            for (int i = 1; i <= 20; i++) {
                Seat s = new Seat();
                s.setAreaId(areaId);
                s.setSeatNo(String.format("%02d", i));
                s.setStatus(SeatStatus.FREE);
                seatMapper.insert(s);
            }
        }
    }
}
