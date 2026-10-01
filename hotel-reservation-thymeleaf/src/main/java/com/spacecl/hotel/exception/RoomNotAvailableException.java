package com.spacecl.hotel.exception;

/** 요청한 기간에 객실이 이미 예약되어 있을 때 (409 Conflict). */
public class RoomNotAvailableException extends RuntimeException {
    public RoomNotAvailableException(String message) {
        super(message);
    }
}
