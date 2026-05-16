package com.studyseat.service.impl;

import com.studyseat.entity.Reservation;
// Seat 实体类未在此文件中使用，可以删除此导入
// 如果后续需要使用 Seat 类，请取消注释下面这行
// import com.studyseat.entity.Seat;
import com.studyseat.mapper.ReservationMapper;
import com.studyseat.service.ReservationService;
import com.studyseat.service.SeatService;
import com.studyseat.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.util.List;
import java.time.LocalDateTime;

@Service
public class ReservationServiceImpl implements ReservationService {
    
    @Autowired
    private ReservationMapper reservationMapper;
    
    @Autowired
    private SeatService seatService;
    
    @Autowired
    private UserService userService;
    
    @Override
    public Reservation createReservation(Long userId, Long seatId, LocalDateTime startTime, LocalDateTime endTime) {
        // 检查座位是否可用
        List<Reservation> existingReservations = reservationMapper.findBySeatIdAndTimeRange(seatId, startTime, endTime);
        if (!existingReservations.isEmpty()) {
            return null;
        }
        
        // 检查用户是否被限制
        userService.checkRestriction(userId);
        if (userService.getById(userId).getIsRestricted()) {
            return null;
        }
        
        // 检查用户当日预约次数
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime todayEnd = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        List<Reservation> todayReservations = reservationMapper.selectList(
            new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getUserId, userId)
                .ge(Reservation::getStartTime, todayStart)
                .le(Reservation::getStartTime, todayEnd)
        );
        if (todayReservations.size() >= 2) {
            return null;
        }
        
        // 创建预约
        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setSeatId(seatId);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);
        reservation.setStatus("已预约");
        reservation.setCreateTime(LocalDateTime.now());
        reservation.setUpdateTime(LocalDateTime.now());
        reservationMapper.insert(reservation);
        
        // 更新座位状态
        seatService.updateStatus(seatId, "已预约");
        
        return reservation;
    }
    
    @Override
    public List<Reservation> getCurrentReservations(Long userId) {
        return reservationMapper.findCurrentByUserId(userId);
    }
    
    @Override
    public List<Reservation> getReservations(Long userId) {
        return reservationMapper.findByUserId(userId);
    }
    
    @Override
    public void cancelReservation(Long reservationId, Long userId) {
        Reservation reservation = reservationMapper.selectById(reservationId);
        if (reservation == null) {
            throw new RuntimeException("预约不存在");
        }
        if (!reservation.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此预约");
        }
        if (!"已预约".equals(reservation.getStatus())) {
            throw new RuntimeException("只能取消状态为'已预约'的预约");
        }
        // 检查是否在预约开始前30分钟
        if (LocalDateTime.now().isBefore(reservation.getStartTime().minusMinutes(30))) {
            reservation.setStatus("已取消");
            reservation.setUpdateTime(LocalDateTime.now());
            reservationMapper.updateById(reservation);
            
            // 更新座位状态
            seatService.updateStatus(reservation.getSeatId(), "空闲");
        } else {
            throw new RuntimeException("只能在预约开始前30分钟取消预约");
        }
    }
    
    @Override
    public void leave(Long reservationId, Long userId) {
        Reservation reservation = reservationMapper.selectById(reservationId);
        if (reservation == null) {
            throw new RuntimeException("预约不存在");
        }
        if (!reservation.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此预约");
        }
        if (!"已开始".equals(reservation.getStatus())) {
            throw new RuntimeException("只能对状态为'已开始'的预约进行暂离操作");
        }
        reservation.setStatus("暂离");
        reservation.setLeaveTime(LocalDateTime.now());
        reservation.setUpdateTime(LocalDateTime.now());
        reservationMapper.updateById(reservation);
        
        // 更新座位状态
        seatService.updateStatus(reservation.getSeatId(), "暂离");
    }
    
    @Override
    public void back(Long reservationId, Long userId) {
        Reservation reservation = reservationMapper.selectById(reservationId);
        if (reservation == null) {
            throw new RuntimeException("预约不存在");
        }
        if (!reservation.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此预约");
        }
        if (!"暂离".equals(reservation.getStatus())) {
            throw new RuntimeException("只能对状态为'暂离'的预约进行返回操作");
        }
        // 检查是否超时
        if (LocalDateTime.now().isBefore(reservation.getLeaveTime().plusMinutes(30))) {
            reservation.setStatus("已开始");
            reservation.setBackTime(LocalDateTime.now());
            reservation.setUpdateTime(LocalDateTime.now());
            reservationMapper.updateById(reservation);
            
            // 更新座位状态
            seatService.updateStatus(reservation.getSeatId(), "占用");
        } else {
            // 超时，自动结束
            reservation.setStatus("已结束");
            reservation.setUpdateTime(LocalDateTime.now());
            reservationMapper.updateById(reservation);
            
            // 更新座位状态
            seatService.updateStatus(reservation.getSeatId(), "空闲");
        }
    }
    
    @Override
    public void finish(Long reservationId, Long userId) {
        Reservation reservation = reservationMapper.selectById(reservationId);
        if (reservation == null) {
            throw new RuntimeException("预约不存在");
        }
        if (!reservation.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此预约");
        }
        if (!"已开始".equals(reservation.getStatus()) && !"暂离".equals(reservation.getStatus())) {
            throw new RuntimeException("只能对状态为'已开始'或'暂离'的预约进行结束操作");
        }
        reservation.setStatus("已结束");
        reservation.setUpdateTime(LocalDateTime.now());
        reservationMapper.updateById(reservation);
        
        // 更新座位状态
        seatService.updateStatus(reservation.getSeatId(), "空闲");
    }
    
    @Override
    public void checkTimeout() {
        // 检查未开始的预约是否超时
        List<Reservation> reservations = reservationMapper.selectList(
            new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getStatus, "已预约")
                .lt(Reservation::getStartTime, LocalDateTime.now())
        );
        for (Reservation reservation : reservations) {
            reservation.setStatus("爽约");
            reservation.setUpdateTime(LocalDateTime.now());
            reservationMapper.updateById(reservation);
            
            // 更新用户违规次数
            userService.updateViolationCount(reservation.getUserId());
            
            // 更新座位状态
            seatService.updateStatus(reservation.getSeatId(), "空闲");
        }
        
        // 检查暂离是否超时
        List<Reservation> leaveReservations = reservationMapper.selectList(
            new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getStatus, "暂离")
                .lt(Reservation::getLeaveTime, LocalDateTime.now().minusMinutes(30))
        );
        for (Reservation reservation : leaveReservations) {
            reservation.setStatus("已结束");
            reservation.setUpdateTime(LocalDateTime.now());
            reservationMapper.updateById(reservation);
            
            // 更新座位状态
            seatService.updateStatus(reservation.getSeatId(), "空闲");
        }
    }
}