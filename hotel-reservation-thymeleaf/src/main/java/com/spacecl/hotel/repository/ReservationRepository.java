package com.spacecl.hotel.repository;

import com.spacecl.hotel.domain.Reservation;
import com.spacecl.hotel.domain.ReservationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * 단건 조회 시 연관된 객실(room)을 함께 로딩한다.
     *
     * <p>Reservation.room 은 지연 로딩(LAZY)이고 open-in-view=false 이므로,
     * 트랜잭션 종료 후 응답 매핑 단계에서 room 에 접근하면 LazyInitializationException 이
     * 발생한다. @EntityGraph 로 조회 시점에 함께 가져와 이를 방지한다.</p>
     */
    @Override
    @EntityGraph(attributePaths = "room")
    Optional<Reservation> findById(Long id);

    /** 특정 객실의 특정 상태 예약 목록. (중복 검사용 - room 을 별도로 직렬화하지 않음) */
    List<Reservation> findByRoomIdAndStatus(Long roomId, ReservationStatus status);

    /** 투숙객 전화번호로 예약 목록 조회 (최근 생성순, 객실 함께 로딩). */
    @EntityGraph(attributePaths = "room")
    List<Reservation> findByGuestPhoneOrderByCreatedAtDesc(String guestPhone);

    /** 전체 예약을 최근 생성순으로 조회 (객실 함께 로딩). */
    @EntityGraph(attributePaths = "room")
    List<Reservation> findAllByOrderByCreatedAtDesc();

    /**
     * 주어진 기간 [checkIn, checkOut) 과 겹치는 '확정' 예약이 있는 객실 ID 목록.
     * 예약 가능 객실 필터링에 사용한다.
     *
     * <p>반열린구간 겹침 조건: existing.checkIn &lt; checkOut AND existing.checkOut &gt; checkIn</p>
     */
    @Query("""
            select distinct res.room.id from Reservation res
            where res.status = com.spacecl.hotel.domain.ReservationStatus.RESERVED
              and res.checkInDate < :checkOut
              and res.checkOutDate > :checkIn
            """)
    List<Long> findOccupiedRoomIds(@Param("checkIn") LocalDate checkIn,
                                   @Param("checkOut") LocalDate checkOut);
}
