package com.spacecl.hotel.service;

import com.spacecl.hotel.domain.Room;
import com.spacecl.hotel.exception.InvalidReservationException;
import com.spacecl.hotel.repository.ReservationRepository;
import com.spacecl.hotel.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * 객실 조회 및 가용성 조회.
 */
@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    public RoomService(RoomRepository roomRepository, ReservationRepository reservationRepository) {
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
    }

    /** 전체 객실 목록. */
    @Transactional(readOnly = true)
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    /**
     * 주어진 기간 [checkIn, checkOut) 에 예약 가능한(중복 예약이 없는) 객실 목록.
     */
    @Transactional(readOnly = true)
    public List<Room> getAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new InvalidReservationException(
                    "조회 기간이 올바르지 않습니다. 체크아웃은 체크인보다 뒤여야 합니다.");
        }
        Set<Long> occupied = Set.copyOf(
                reservationRepository.findOccupiedRoomIds(checkIn, checkOut));
        return roomRepository.findAll().stream()
                .filter(room -> !occupied.contains(room.getId()))
                .toList();
    }
}
