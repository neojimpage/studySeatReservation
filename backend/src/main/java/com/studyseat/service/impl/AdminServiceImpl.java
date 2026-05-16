package com.studyseat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studyseat.entity.*;
import com.studyseat.mapper.*;
import com.studyseat.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired private AreaMapper areaMapper;
    @Autowired private SeatMapper seatMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private ReservationMapper reservationMapper;

    @Override
    public Map<String, Object> getDashboard() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSeats", seatMapper.selectCount(null));
        stats.put("availableSeats",
            seatMapper.selectCount(new LambdaQueryWrapper<Seat>().eq(Seat::getStatus, "空闲")));
        stats.put("todayReservations",
            reservationMapper.selectCount(new LambdaQueryWrapper<Reservation>()
                .ge(Reservation::getStartTime, LocalDateTime.now().withHour(0).withMinute(0))
                .le(Reservation::getStartTime, LocalDateTime.now().withHour(23).withMinute(59))));
        stats.put("violationUsers",
            userMapper.selectCount(new LambdaQueryWrapper<User>().ge(User::getViolationCount, 3)));
        return stats;
    }

    @Override
    public List<Area> getAllAreas() {
        return areaMapper.selectList(null);
    }

    @Override
    public Area createArea(String name, Integer floor) {
        Area area = new Area();
        area.setName(name);
        area.setFloor(floor);
        area.setIsEnabled(true);
        area.setCreateTime(LocalDateTime.now());
        area.setUpdateTime(LocalDateTime.now());
        areaMapper.insert(area);
        return area;
    }

    @Override
    public Area updateArea(Long id, String name, Integer floor) {
        Area area = areaMapper.selectById(id);
        if (area == null) throw new RuntimeException("区域不存在");
        area.setName(name);
        area.setFloor(floor);
        area.setUpdateTime(LocalDateTime.now());
        areaMapper.updateById(area);
        return area;
    }

    @Override
    public void toggleArea(Long id, boolean disabled) {
        Area area = areaMapper.selectById(id);
        if (area == null) throw new RuntimeException("区域不存在");
        area.setIsEnabled(!disabled);
        area.setUpdateTime(LocalDateTime.now());
        areaMapper.updateById(area);
    }

    @Override
    public List<Seat> getSeats(Long areaId) {
        if (areaId != null) return seatMapper.findByAreaId(areaId);
        return seatMapper.selectList(null);
    }

    @Override
    public Seat createSeat(Long areaId, String seatNumber) {
        Seat seat = new Seat();
        seat.setAreaId(areaId);
        seat.setSeatNumber(seatNumber);
        seat.setStatus("空闲");
        seat.setIsEnabled(true);
        seat.setCreateTime(LocalDateTime.now());
        seat.setUpdateTime(LocalDateTime.now());
        seatMapper.insert(seat);
        return seat;
    }

    @Override
    public Seat updateSeat(Long id, Long areaId, String seatNumber) {
        Seat seat = seatMapper.selectById(id);
        if (seat == null) throw new RuntimeException("座位不存在");
        seat.setAreaId(areaId);
        seat.setSeatNumber(seatNumber);
        seat.setUpdateTime(LocalDateTime.now());
        seatMapper.updateById(seat);
        return seat;
    }

    @Override
    public void toggleSeat(Long id, boolean disabled) {
        Seat seat = seatMapper.selectById(id);
        if (seat == null) throw new RuntimeException("座位不存在");
        seat.setIsEnabled(!disabled);
        seat.setUpdateTime(LocalDateTime.now());
        seatMapper.updateById(seat);
    }

    @Override
    public void releaseSeat(Long id) {
        Seat seat = seatMapper.selectById(id);
        if (seat == null) throw new RuntimeException("座位不存在");
        seat.setStatus("空闲");
        seat.setUpdateTime(LocalDateTime.now());
        seatMapper.updateById(seat);

        // End any active reservation on this seat
        List<Reservation> active = reservationMapper.selectList(
            new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getSeatId, id)
                .in(Reservation::getStatus, List.of("已开始", "暂离"))
        );
        for (Reservation r : active) {
            r.setStatus("已结束");
            r.setUpdateTime(LocalDateTime.now());
            reservationMapper.updateById(r);
        }
    }

    @Override
    public List<User> getAllUsers() {
        return userMapper.selectList(
            new LambdaQueryWrapper<User>().ne(User::getRole, "admin"));
    }

    @Override
    public void banUser(Long id, boolean banned) {
        User user = userMapper.selectById(id);
        if (user == null) throw new RuntimeException("用户不存在");
        user.setIsRestricted(banned);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public void resetViolation(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new RuntimeException("用户不存在");
        user.setViolationCount(0);
        user.setIsRestricted(false);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }
}
