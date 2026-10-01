package com.spacecl.hotel.domain;

/**
 * 예약 상태.
 */
public enum ReservationStatus {
    /** 예약 확정 (객실 점유) */
    RESERVED,
    /** 예약 취소됨 (객실 점유하지 않음) */
    CANCELLED
}
