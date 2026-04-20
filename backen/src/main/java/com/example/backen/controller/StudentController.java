package com.example.backen.controller;

import com.example.backen.dto.ReservationRequest;
import com.example.backen.model.Area;
import com.example.backen.model.Reservation;
import com.example.backen.model.Seat;
import com.example.backen.service.ReservationService;
import com.example.backen.util.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final ReservationService reservationService;

    public StudentController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/areas")
    public ApiResponse<List<Area>> areas() {
        return ApiResponse.ok(reservationService.listAreas());
    }

    @GetMapping("/seats")
    public ApiResponse<List<Seat>> seats(@RequestParam(required = false) Long areaId) {
        return ApiResponse.ok(reservationService.listSeats(areaId));
    }

    @PostMapping("/reservations")
    public ApiResponse<Reservation> createReservation(@RequestBody ReservationRequest req) {
        try {
            Reservation r = reservationService.createReservation(req);
            return ApiResponse.ok(r);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/reservations/current")
    public ApiResponse<List<Reservation>> current(@RequestParam String userId) {
        return ApiResponse.ok(reservationService.currentReservations(userId));
    }

    @GetMapping("/reservations")
    public ApiResponse<List<Reservation>> reservations(@RequestParam String userId) {
        return ApiResponse.ok(reservationService.listReservations(userId));
    }

    @PostMapping("/reservations/{id}/cancel")
    public ApiResponse<?> cancel(@PathVariable Long id, @RequestParam String userId) {
        try {
            reservationService.cancelReservation(id, userId);
            return ApiResponse.ok();
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/reservations/{id}/leave")
    public ApiResponse<?> leave(@PathVariable Long id, @RequestParam String userId) {
        try {
            reservationService.leave(id, userId);
            return ApiResponse.ok();
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/reservations/{id}/back")
    public ApiResponse<?> back(@PathVariable Long id, @RequestParam String userId) {
        try {
            reservationService.back(id, userId);
            return ApiResponse.ok();
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/reservations/{id}/finish")
    public ApiResponse<?> finish(@PathVariable Long id, @RequestParam String userId) {
        try {
            reservationService.finish(id, userId);
            return ApiResponse.ok();
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
