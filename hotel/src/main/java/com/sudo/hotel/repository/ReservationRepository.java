package com.sudo.hotel.repository;

import com.sudo.hotel.domain.Reservation;
import com.sudo.hotel.domain.ReservationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    /*
        단건 조회시 연관된 객실(room)을 함께 로딩한다.
     */

    @EntityGraph(attributePaths = "room")
    @Override
    Optional<Reservation> findById(Long id);

    /* 특정 객실의 특정 상태 예약 목록 */
    List<Reservation> findByRoomIdAndStatus(Long roomId, ReservationStatus status);

    /* 투숙객 전화번호로 예약 목록 조회 */
    @EntityGraph(attributePaths = "room")
    List<Reservation> findByGuestPhoneOrderByCreatedAtDesc(String guestPhone);

    /* 전체 예약을 최근 생성순으로 조회(객실 함께 로딩) */
    @EntityGraph(attributePaths = "room")
    List<Reservation> findAllByOrderByCreatedAtDesc();

}
