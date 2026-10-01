package com.spacecl.hotel.exception;

/** 예약 요청 값이 유효하지 않을 때 (400). */
public class InvalidReservationException extends RuntimeException {
    public InvalidReservationException(String message) {
        super(message);
    }
}
