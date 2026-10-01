package com.spacecl.hotel.repository;

import com.spacecl.hotel.domain.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    /**
     * 비관적 쓰기 락(SELECT ... FOR UPDATE)으로 객실을 조회한다.
     *
     * <p>동일 객실에 대한 동시 예약 요청을 직렬화하여, 겹침 검사와 예약 저장이
     * 원자적으로 수행되도록 한다(중복 예약 방지).</p>
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Room r where r.id = :id")
    Optional<Room> findByIdForUpdate(@Param("id") Long id);
}
