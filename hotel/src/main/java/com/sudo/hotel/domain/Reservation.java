package com.sudo.hotel.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* 예약된 객실 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private String guestName;

    @Column(nullable = false)
    private String guestPhone;

    /* 투숙 인원 */
    @Column(nullable = false)
    private int guestCount;

    @Column(nullable = false)
    private LocalDate checkInDate;

    @Column(nullable = false)
    private LocalDate checkOutDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Reservation(){}

    public Reservation(Room room, String guestName, String guestPhone, int guestCount,
                    LocalDate checkInDate, LocalDate checkOutDate) {
        this.room = room;
        this.guestName = guestName;
        this.guestPhone = guestPhone;
        this.guestCount = guestCount;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.status = ReservationStatus.RESERVED;
        this.createdAt = Instant.now();
    }

    /*
        이 예약이 주아진 기간 (checkIn, checkOut) 과 날짜상 겹치는지 판정한다.
     */
    public boolean overlaps(LocalDate checkIn, LocalDate checkOut){
        if(status != ReservationStatus.RESERVED){
            return false;
        }
        return this.checkInDate.isBefore(checkOut) && checkIn.isBefore(this.checkOutDate);
    }

    // 예약을 취소 상태로 변경한다.
    public void cancel(){
        this.status = ReservationStatus.CANCELLED;
    }

    public boolean isCancelled(){
        return status == ReservationStatus.CANCELLED;
    }

    //getter
    public Long getId() {
        return id;
    }

    public Room getRoom() {
        return room;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getGuestPhone() {
        return guestPhone;
    }

    public int getGuestCount() {
        return guestCount;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
