package com.studyseat.service;

import com.studyseat.entity.Area;
import com.studyseat.entity.Seat;
import com.studyseat.entity.User;

import java.util.List;
import java.util.Map;

public interface AdminService {
    Map<String, Object> getDashboard();
    List<Area> getAllAreas();
    Area createArea(String name, Integer floor);
    Area updateArea(Long id, String name, Integer floor);
    void toggleArea(Long id, boolean disabled);
    List<Seat> getSeats(Long areaId);
    Seat createSeat(Long areaId, String seatNumber);
    Seat updateSeat(Long id, Long areaId, String seatNumber);
    void toggleSeat(Long id, boolean disabled);
    void releaseSeat(Long id);
    List<User> getAllUsers();
    void banUser(Long id, boolean banned);
    void resetViolation(Long id);
}
