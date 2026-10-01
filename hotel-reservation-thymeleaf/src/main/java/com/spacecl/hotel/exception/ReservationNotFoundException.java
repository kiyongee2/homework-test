package com.spacecl.hotel.exception;

/** 예약을 찾을 수 없을 때 (404). */
public class ReservationNotFoundException extends RuntimeException {
    public ReservationNotFoundException(Long reservationId) {
        super("존재하지 않는 예약입니다: id=" + reservationId);
    }
}
