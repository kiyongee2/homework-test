package com.sudo.hotel.service;

import com.sudo.hotel.domain.Reservation;
import com.sudo.hotel.domain.ReservationStatus;
import com.sudo.hotel.domain.Room;
import com.sudo.hotel.dto.ReservationRequest;
import com.sudo.hotel.exception.InvalidReservationException;
import com.sudo.hotel.exception.ReservationNotFoundException;
import com.sudo.hotel.exception.RoomNotAvailableException;
import com.sudo.hotel.exception.RoomNotFoundException;
import com.sudo.hotel.repository.ReservationRepository;
import com.sudo.hotel.repository.RoomRepository;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationService {
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final Clock clock;

    public ReservationService(RoomRepository roomRepository,
                              ReservationRepository reservationRepository,
                              Clock clock){
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
        this.clock = clock;
    }

    /*
        예약을 생성한다.
        동시성 - 대상 객실 행에 비관적 쓰기 락을 걸어 동일 객실에 대한 동시 예약
     */
    @Transactional
    public Reservation reserve(ReservationRequest request) {
        LocalDate checkIn = request.getCheckInDate();
        LocalDate checkOut = request.getCheckOutDate();
        validatePeriod(checkIn, checkOut);

        //비관적 락으로 객실 조회(동일 객실 동시 예약 적절화)
        Room room = roomRepository.findByIdForUpdate(request.getRoomId())
                .orElseThrow(() -> new RoomNotFoundException(request.getRoomId()));
        if (request.getGuestCount() > room.getCapacity()) {
            throw new InvalidReservationException(
                    "투숙 인원(" + request.getGuestCount() + "명)이 객실 최대 수용 인원("
                        + room.getCapacity() + "명)을 초과합니다.");
        }

        //락을 확보한 상태에서 해당 객실의 확정 예약과 기간 중복 여부 검사
        List<Reservation> active =
             reservationRepository.findByRoomIdAndStatus(room.getId(), ReservationStatus.RESERVED);
        boolean overlapped = active.stream().anyMatch(r -> r.overlaps(checkIn, checkOut));
        if(overlapped){
            throw new RoomNotAvailableException(
                    "해당 기간(" + checkIn + " ~ " + checkOut + ")에는 "
                    + room.getRoomNuber() + "호가 이미 예약되어 있습니다.");
        }

        Reservation reservation = new Reservation(
                room, request.getGuestName(), request.getGuestPhone(),
                request.getGuestCount(), checkIn, checkOut);
        return reservationRepository.save(reservation);
    }

    /* 예약 단건 조회 */
    @Transactional(readOnly = true)
    public Reservation getReservation(Long id){
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));
    }

    /* 전체 예약 목록(최근순) */
    @Transactional(readOnly = true)
    public List<Reservation> getAllReservations(){
        return reservationRepository.findAllByOrderByCreatedAtDesc();
    }

    /* 투숙객 전화번호로 예약 목록 조회  */
    @Transactional(readOnly = true)
    public List<Reservation> getReservationsByGuestPhone(String guestPhone){
        return reservationRepository.findByGuestPhoneOrderByCreatedAtDesc(guestPhone);
    }

    /* 예약을 취소한다 */
    @Transactional
    public Reservation cancel(Long id){
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));
        reservation.cancel();
        return reservation;
    }

    /* 체그인/체크아웃 날짜 유효성 검증  */
    private void validatePeriod(LocalDate checkIn, LocalDate checkOut){
        if(checkIn == null || checkOut == null){
            throw new InvalidReservationException("체크인/체크아웃 날짜는 필수입니다.");
        }
        LocalDate today = LocalDate.now(clock);
        if(checkIn.isBefore(today)){
            throw new InvalidReservationException("체크인 날짜는 오늘 이후여야 합니다: " + checkIn);
        }
        if(!checkOut.isAfter(checkIn)){
            throw new InvalidReservationException("체크아웃 날짜는 체크인 날짜보다 뒤여야 합니다. (checkIn=" +
                    checkIn + ", checkOut=" + checkOut + ")");
        }
    }
}
