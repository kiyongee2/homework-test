package com.spacecl.hotel.service;

import com.spacecl.hotel.domain.Reservation;
import com.spacecl.hotel.domain.ReservationStatus;
import com.spacecl.hotel.domain.Room;
import com.spacecl.hotel.dto.ReservationRequest;
import com.spacecl.hotel.exception.InvalidReservationException;
import com.spacecl.hotel.exception.ReservationNotFoundException;
import com.spacecl.hotel.exception.RoomNotAvailableException;
import com.spacecl.hotel.exception.RoomNotFoundException;
import com.spacecl.hotel.repository.ReservationRepository;
import com.spacecl.hotel.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * 예약 핵심 비즈니스 로직.
 */
@Service
public class ReservationService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final Clock clock;

    public ReservationService(RoomRepository roomRepository,
                              ReservationRepository reservationRepository,
                              Clock clock) {
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
        this.clock = clock;
    }

    /**
     * 예약을 생성한다.
     *
     * <p>동시성: 대상 객실 행에 비관적 쓰기 락을 걸어 동일 객실에 대한 동시 예약
     * 요청을 직렬화한 뒤, 기간 중복을 검사하고 저장한다. 이렇게 해서 같은 기간에
     * 두 건이 동시에 들어와도 하나만 성공하고 나머지는 409로 거절된다.</p>
     */
    @Transactional
    public Reservation reserve(ReservationRequest request) {
        LocalDate checkIn = request.getCheckInDate();
        LocalDate checkOut = request.getCheckOutDate();
        validatePeriod(checkIn, checkOut);

        // 비관적 락으로 객실 조회 (동일 객실 동시 예약 직렬화)
        Room room = roomRepository.findByIdForUpdate(request.getRoomId())
                .orElseThrow(() -> new RoomNotFoundException(request.getRoomId()));

        if (request.getGuestCount() > room.getCapacity()) {
            throw new InvalidReservationException(
                    "투숙 인원(" + request.getGuestCount() + "명)이 객실 최대 수용 인원("
                            + room.getCapacity() + "명)을 초과합니다.");
        }

        // 락을 확보한 상태에서 해당 객실의 확정 예약과 기간 중복 여부 검사
        List<Reservation> active =
                reservationRepository.findByRoomIdAndStatus(room.getId(), ReservationStatus.RESERVED);
        boolean overlapped = active.stream().anyMatch(r -> r.overlaps(checkIn, checkOut));
        if (overlapped) {
            throw new RoomNotAvailableException(
                    "해당 기간(" + checkIn + " ~ " + checkOut + ")에는 "
                            + room.getRoomNumber() + "호가 이미 예약되어 있습니다.");
        }

        Reservation reservation = new Reservation(
                room, request.getGuestName(), request.getGuestPhone(),
                request.getGuestCount(), checkIn, checkOut);
        return reservationRepository.save(reservation);
    }

    /** 예약 단건 조회. */
    @Transactional(readOnly = true)
    public Reservation getReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));
    }

    /** 전체 예약 목록 (최근순). */
    @Transactional(readOnly = true)
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAllByOrderByCreatedAtDesc();
    }

    /** 투숙객 전화번호로 예약 목록 조회. */
    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByGuestPhone(String guestPhone) {
        return reservationRepository.findByGuestPhoneOrderByCreatedAtDesc(guestPhone);
    }

    /**
     * 예약을 취소한다. 이미 취소된 예약은 멱등하게 그대로 취소 상태를 유지한다.
     */
    @Transactional
    public Reservation cancel(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));
        reservation.cancel();
        return reservation;
    }

    /** 체크인/체크아웃 날짜 유효성 검증. */
    private void validatePeriod(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new InvalidReservationException("체크인/체크아웃 날짜는 필수입니다.");
        }
        LocalDate today = LocalDate.now(clock);
        if (checkIn.isBefore(today)) {
            throw new InvalidReservationException("체크인 날짜는 오늘 이후여야 합니다: " + checkIn);
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new InvalidReservationException(
                    "체크아웃 날짜는 체크인 날짜보다 뒤여야 합니다. (checkIn=" + checkIn + ", checkOut=" + checkOut + ")");
        }
    }
}
