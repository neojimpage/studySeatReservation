package com.studyseat.service.impl;

import com.studyseat.entity.Seat;
import com.studyseat.mapper.SeatMapper;
import com.studyseat.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDateTime;

@Service
public class SeatServiceImpl implements SeatService {
    
    @Autowired
    private SeatMapper seatMapper;
    
    @Override
    public List<Seat> getSeatsByAreaId(Long areaId) {
        return seatMapper.findByAreaId(areaId);
    }

    @Override
    public List<Seat> getAllSeats() {
        return seatMapper.selectList(null);
    }

    @Override
    public Seat getById(Long id) {
        return seatMapper.selectById(id);
    }
    
    @Override
    public void updateStatus(Long seatId, String status) {
        Seat seat = seatMapper.selectById(seatId);
        if (seat != null) {
            seat.setStatus(status);
            seat.setUpdateTime(LocalDateTime.now());
            seatMapper.updateById(seat);
        }
    }
}