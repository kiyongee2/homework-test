package com.spacecl.hotel.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 예약 엔티티.
 *
 * <p>기간은 [checkInDate, checkOutDate) 로 해석한다(호텔 관례: 체크아웃 당일은
 * 점유하지 않으므로 종료일은 배타적). 따라서 어떤 예약의 체크아웃 날짜에
 * 다른 손님이 체크인하는 것은 중복이 아니다.</p>
 */
@Entity
@Table(name = "reservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 예약된 객실 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private String guestName;

    @Column(nullable = false)
    private String guestPhone;

    /** 투숙 인원 */
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

    protected Reservation() {
    }

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

    /**
     * 이 예약이 주어진 기간 [checkIn, checkOut) 과 날짜상 겹치는지 판정한다.
     *
     * <p>두 반열린구간 [a, b) 와 [c, d) 는 (a &lt; d) && (c &lt; b) 일 때 겹친다.
     * 취소된 예약은 객실을 점유하지 않으므로 겹치지 않는 것으로 본다.</p>
     */
    public boolean overlaps(LocalDate checkIn, LocalDate checkOut) {
        if (status != ReservationStatus.RESERVED) {
            return false;
        }
        return this.checkInDate.isBefore(checkOut) && checkIn.isBefore(this.checkOutDate);
    }

    /** 예약을 취소 상태로 변경한다. */
    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }

    public boolean isCancelled() {
        return status == ReservationStatus.CANCELLED;
    }

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
