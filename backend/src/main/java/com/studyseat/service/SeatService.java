package com.studyseat.service;

import com.studyseat.entity.Seat;
import java.util.List;

public interface SeatService {
    List<Seat> getSeatsByAreaId(Long areaId);
    List<Seat> getAllSeats();
    Seat getById(Long id);
    void updateStatus(Long seatId, String status);
}