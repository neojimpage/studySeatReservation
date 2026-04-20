package com.example.backen.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.backen.dto.ReservationRequest;
import com.example.backen.mapper.AreaMapper;
import com.example.backen.mapper.ReservationMapper;
import com.example.backen.mapper.SeatMapper;
import com.example.backen.model.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class ReservationService {

    private final AreaMapper areaMapper;
    private final SeatMapper seatMapper;
    private final ReservationMapper reservationMapper;

    public ReservationService(AreaMapper areaMapper, SeatMapper seatMapper, ReservationMapper reservationMapper) {
        this.areaMapper = areaMapper;
        this.seatMapper = seatMapper;
        this.reservationMapper = reservationMapper;
    }

    public List<Area> listAreas() {
        return areaMapper.selectList(null);
    }

    public List<Seat> listSeats(Long areaId) {
        QueryWrapper<Seat> qw = new QueryWrapper<>();
        if (areaId != null) qw.eq("area_id", areaId);
        qw.orderByAsc("id");
        return seatMapper.selectList(qw);
    }

    public Reservation createReservation(ReservationRequest req) {
        if (req.getSeatId() == null || req.getUserId() == null || req.getStartTime() == null || req.getEndTime() == null) {
            throw new IllegalArgumentException("参数不完整");
        }
        Seat seat = seatMapper.selectById(req.getSeatId());
        if (seat == null) throw new IllegalArgumentException("座位不存在");
        LocalDateTime now = LocalDateTime.now();
        if (req.getEndTime().isBefore(req.getStartTime()) || req.getEndTime().isBefore(now)) {
            throw new IllegalArgumentException("时间范围不合法");
        }

        // check overlap: fetch existing reservations for seat
        QueryWrapper<Reservation> qw = new QueryWrapper<>();
        qw.eq("seat_id", req.getSeatId());
        List<Reservation> exists = reservationMapper.selectList(qw);
        for (Reservation r : exists) {
            if (r.getStatus() == ReservationStatus.CANCELLED || r.getStatus() == ReservationStatus.FINISHED) continue;
            if (req.getStartTime().isBefore(r.getEndTime()) && req.getEndTime().isAfter(r.getStartTime())) {
                throw new IllegalArgumentException("座位在该时段已被占用/预约");
            }
        }

        Reservation r = new Reservation();
        r.setSeatId(req.getSeatId());
        r.setUserId(req.getUserId());
        r.setStartTime(req.getStartTime());
        r.setEndTime(req.getEndTime());
        r.setStatus(ReservationStatus.RESERVED);
        reservationMapper.insert(r);

        seat.setStatus(SeatStatus.RESERVED);
        seatMapper.updateById(seat);

        return r;
    }

    public List<Reservation> currentReservations(String userId) {
        LocalDateTime now = LocalDateTime.now();
        QueryWrapper<Reservation> qw = new QueryWrapper<>();
        qw.eq("user_id", userId)
                .gt("end_time", now)
                .in("status", ReservationStatus.RESERVED.name(), ReservationStatus.OCCUPIED.name(), ReservationStatus.LEFT.name())
                .orderByAsc("start_time");
        return reservationMapper.selectList(qw);
    }

    public List<Reservation> listReservations(String userId) {
        QueryWrapper<Reservation> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).orderByDesc("start_time");
        return reservationMapper.selectList(qw);
    }

    public Reservation findById(Long id) {
        return reservationMapper.selectById(id);
    }

    public Reservation cancelReservation(Long id, String userId) {
        Reservation r = reservationMapper.selectById(id);
        if (r == null) throw new IllegalArgumentException("预约不存在");
        if (!Objects.equals(r.getUserId(), userId)) throw new IllegalArgumentException("无权限");
        LocalDateTime now = LocalDateTime.now();
        if (!now.isBefore(r.getStartTime().minusMinutes(30))) {
            throw new IllegalArgumentException("预约开始前30分钟内不可取消");
        }
        r.setStatus(ReservationStatus.CANCELLED);
        reservationMapper.updateById(r);
        Seat seat = seatMapper.selectById(r.getSeatId());
        if (seat != null) {
            seat.setStatus(SeatStatus.FREE);
            seatMapper.updateById(seat);
        }
        return r;
    }

    public Reservation leave(Long id, String userId) {
        Reservation r = reservationMapper.selectById(id);
        if (r == null) throw new IllegalArgumentException("预约不存在");
        if (!Objects.equals(r.getUserId(), userId)) throw new IllegalArgumentException("无权限");
        r.setStatus(ReservationStatus.LEFT);
        r.setLeaveStart(LocalDateTime.now());
        reservationMapper.updateById(r);
        Seat seat = seatMapper.selectById(r.getSeatId());
        if (seat != null) {
            seat.setStatus(SeatStatus.LEFT);
            seatMapper.updateById(seat);
        }
        return r;
    }

    public Reservation back(Long id, String userId) {
        Reservation r = reservationMapper.selectById(id);
        if (r == null) throw new IllegalArgumentException("预约不存在");
        if (!Objects.equals(r.getUserId(), userId)) throw new IllegalArgumentException("无权限");
        r.setStatus(ReservationStatus.OCCUPIED);
        r.setLeaveStart(null);
        reservationMapper.updateById(r);
        Seat seat = seatMapper.selectById(r.getSeatId());
        if (seat != null) {
            seat.setStatus(SeatStatus.OCCUPIED);
            seatMapper.updateById(seat);
        }
        return r;
    }

    public Reservation finish(Long id, String userId) {
        Reservation r = reservationMapper.selectById(id);
        if (r == null) throw new IllegalArgumentException("预约不存在");
        if (!Objects.equals(r.getUserId(), userId)) throw new IllegalArgumentException("无权限");
        r.setStatus(ReservationStatus.FINISHED);
        reservationMapper.updateById(r);
        Seat seat = seatMapper.selectById(r.getSeatId());
        if (seat != null) {
            seat.setStatus(SeatStatus.FREE);
            seatMapper.updateById(seat);
        }
        return r;
    }
}
