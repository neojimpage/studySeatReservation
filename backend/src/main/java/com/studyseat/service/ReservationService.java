package com.studyseat.service;

import com.studyseat.entity.Reservation;
import java.util.List;
import java.time.LocalDateTime;

public interface ReservationService {
    Reservation createReservation(Long userId, Long seatId, LocalDateTime startTime, LocalDateTime endTime);
    List<Reservation> getCurrentReservations(Long userId);
    List<Reservation> getReservations(Long userId);
    void cancelReservation(Long reservationId, Long userId);
    void leave(Long reservationId, Long userId);
    void back(Long reservationId, Long userId);
    void finish(Long reservationId, Long userId);
    void checkTimeout();
}