package com.spacecl.hotel.dto;

import com.spacecl.hotel.domain.Reservation;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 예약 정보 응답.
 */
public record ReservationResponse(
        Long id,
        Long roomId,
        String roomNumber,
        String guestName,
        String guestPhone,
        int guestCount,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        long nights,
        String status,
        Instant createdAt
) {
    public static ReservationResponse from(Reservation r) {
        long nights = r.getCheckOutDate().toEpochDay() - r.getCheckInDate().toEpochDay();
        return new ReservationResponse(
                r.getId(),
                r.getRoom().getId(),
                r.getRoom().getRoomNumber(),
                r.getGuestName(),
                r.getGuestPhone(),
                r.getGuestCount(),
                r.getCheckInDate(),
                r.getCheckOutDate(),
                nights,
                r.getStatus().name(),
                r.getCreatedAt()
        );
    }
}
