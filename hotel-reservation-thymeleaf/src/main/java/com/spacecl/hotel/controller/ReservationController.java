package com.spacecl.hotel.controller;

import com.spacecl.hotel.domain.Reservation;
import com.spacecl.hotel.dto.ReservationRequest;
import com.spacecl.hotel.dto.ReservationResponse;
import com.spacecl.hotel.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 예약 API.
 *
 * <ul>
 *   <li>POST   /api/reservations              : 예약 생성</li>
 *   <li>GET    /api/reservations/{id}         : 예약 단건 조회</li>
 *   <li>GET    /api/reservations              : 예약 목록 (guestPhone 파라미터로 필터 가능)</li>
 *   <li>POST   /api/reservations/{id}/cancel  : 예약 취소</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody ReservationRequest request) {
        Reservation reservation = reservationService.reserve(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReservationResponse.from(reservation));
    }

    @GetMapping("/{id}")
    public ReservationResponse get(@PathVariable Long id) {
        return ReservationResponse.from(reservationService.getReservation(id));
    }

    @GetMapping
    public List<ReservationResponse> list(@RequestParam(required = false) String guestPhone) {
        List<Reservation> reservations = (guestPhone == null || guestPhone.isBlank())
                ? reservationService.getAllReservations()
                : reservationService.getReservationsByGuestPhone(guestPhone);
        return reservations.stream().map(ReservationResponse::from).toList();
    }

    @PostMapping("/{id}/cancel")
    public ReservationResponse cancel(@PathVariable Long id) {
        return ReservationResponse.from(reservationService.cancel(id));
    }
}
