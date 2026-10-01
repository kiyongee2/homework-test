package com.sudo.hotel.controller;

import com.sudo.hotel.domain.Reservation;
import com.sudo.hotel.dto.ReservationRequest;
import com.sudo.hotel.dto.ReservationResponse;
import com.sudo.hotel.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/reservations")
@RestController
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService){
        this.reservationService = reservationService;
    }

    // 예약 생성
    @PostMapping
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody ReservationRequest request){
        Reservation reservation = reservationService.reserve(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReservationResponse.from(reservation));
    }

    // 예약 단건 조회
    @GetMapping("/{id}")
    public ReservationResponse get(@PathVariable Long id){
        return ReservationResponse.from(reservationService.getReservation(id));
    }

    // 예약 목록
    @GetMapping
    public List<ReservationResponse> list(@RequestParam(required = false) String guestPhone){
        List<Reservation> reservations = (guestPhone == null || guestPhone.isBlank())
                ? reservationService.getAllReservations()
                : reservationService.getReservationsByGuestPhone(guestPhone);
        return  reservations.stream().map(ReservationResponse::from).toList();
    }

    // 예약 취소
    @PostMapping("/{id}/cancel")
    public ReservationResponse cancel(@PathVariable Long id){
        return ReservationResponse.from(reservationService.cancel(id));
    }
}
