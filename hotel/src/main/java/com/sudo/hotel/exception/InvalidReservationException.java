package com.sudo.hotel.exception;

public class InvalidReservationException extends RuntimeException{
    public InvalidReservationException(String message){
        super(message);
    }
}
