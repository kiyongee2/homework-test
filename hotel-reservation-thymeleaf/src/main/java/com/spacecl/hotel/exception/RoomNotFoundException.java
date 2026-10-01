package com.spacecl.hotel.exception;

/** 객실을 찾을 수 없을 때 (404). */
public class RoomNotFoundException extends RuntimeException {
    public RoomNotFoundException(Long roomId) {
        super("존재하지 않는 객실입니다: id=" + roomId);
    }
}
