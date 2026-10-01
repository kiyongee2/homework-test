package com.sudo.hotel.exception;

public class RoomNotFoundException extends RuntimeException{
    public RoomNotFoundException(Long roomId){
        super("존재하지 않는 객실입니다.: id=" + roomId);
    }
}
