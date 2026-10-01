package com.spacecl.hotel;

import com.spacecl.hotel.domain.Reservation;
import com.spacecl.hotel.domain.ReservationStatus;
import com.spacecl.hotel.domain.Room;
import com.spacecl.hotel.domain.RoomType;
import com.spacecl.hotel.dto.ReservationRequest;
import com.spacecl.hotel.exception.InvalidReservationException;
import com.spacecl.hotel.exception.RoomNotAvailableException;
import com.spacecl.hotel.repository.ReservationRepository;
import com.spacecl.hotel.repository.RoomRepository;
import com.spacecl.hotel.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("예약 서비스 통합 테스트")
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    private Room room;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
        roomRepository.deleteAll();
        room = roomRepository.save(new Room("201", RoomType.DOUBLE, 2));
    }

    private ReservationRequest request(Long roomId, int guestCount, String in, String out) {
        ReservationRequest req = new ReservationRequest();
        req.setRoomId(roomId);
        req.setGuestName("홍길동");
        req.setGuestPhone("010-1234-5678");
        req.setGuestCount(guestCount);
        req.setCheckInDate(LocalDate.parse(in));
        req.setCheckOutDate(LocalDate.parse(out));
        return req;
    }

    private String plusDays(int d) {
        return LocalDate.now().plusDays(d).toString();
    }

    @Test
    @DisplayName("정상 예약이 생성된다")
    void reserveSuccess() {
        Reservation r = reservationService.reserve(request(room.getId(), 2, plusDays(1), plusDays(3)));
        assertThat(r.getId()).isNotNull();
        assertThat(r.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        assertThat(r.getRoom().getId()).isEqualTo(room.getId());
    }

    @Test
    @DisplayName("같은 기간에 겹치는 예약은 409(RoomNotAvailable)로 거절된다")
    void overlappingReservationRejected() {
        reservationService.reserve(request(room.getId(), 2, plusDays(1), plusDays(5)));
        assertThatThrownBy(() ->
                reservationService.reserve(request(room.getId(), 2, plusDays(3), plusDays(7))))
                .isInstanceOf(RoomNotAvailableException.class);
    }

    @Test
    @DisplayName("체크아웃 당일에 이어지는 예약은 허용된다")
    void adjacentReservationAllowed() {
        reservationService.reserve(request(room.getId(), 2, plusDays(1), plusDays(3)));
        Reservation next = reservationService.reserve(request(room.getId(), 2, plusDays(3), plusDays(5)));
        assertThat(next.getId()).isNotNull();
    }

    @Test
    @DisplayName("예약 취소 후 같은 기간을 다시 예약할 수 있다")
    void reReserveAfterCancel() {
        Reservation first = reservationService.reserve(request(room.getId(), 2, plusDays(1), plusDays(4)));
        reservationService.cancel(first.getId());
        Reservation again = reservationService.reserve(request(room.getId(), 2, plusDays(1), plusDays(4)));
        assertThat(again.getId()).isNotEqualTo(first.getId());
    }

    @Test
    @DisplayName("과거 날짜 체크인은 400(Invalid)")
    void pastCheckInRejected() {
        assertThatThrownBy(() ->
                reservationService.reserve(request(room.getId(), 2, plusDays(-1), plusDays(2))))
                .isInstanceOf(InvalidReservationException.class);
    }

    @Test
    @DisplayName("체크아웃이 체크인보다 앞서거나 같으면 400(Invalid)")
    void invalidPeriodRejected() {
        assertThatThrownBy(() ->
                reservationService.reserve(request(room.getId(), 2, plusDays(3), plusDays(3))))
                .isInstanceOf(InvalidReservationException.class);
    }

    @Test
    @DisplayName("투숙 인원이 객실 수용 인원을 초과하면 400(Invalid)")
    void capacityExceededRejected() {
        assertThatThrownBy(() ->
                reservationService.reserve(request(room.getId(), 3, plusDays(1), plusDays(2))))
                .isInstanceOf(InvalidReservationException.class);
    }

    @Test
    @DisplayName("동시에 같은 객실/기간을 예약하면 정확히 1건만 성공한다 (동시성/중복예약 방지)")
    void concurrentReservationsOnlyOneSucceeds() throws InterruptedException {
        int threads = 20;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger conflict = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    reservationService.reserve(request(room.getId(), 2, plusDays(10), plusDays(12)));
                    success.incrementAndGet();
                } catch (RoomNotAvailableException e) {
                    conflict.incrementAndGet();
                } catch (Exception ignored) {
                    // 락 경합 등으로 인한 기타 예외도 실패로 간주 (성공 카운트에 미포함)
                } finally {
                    // no-op
                }
            });
        }
        ready.await(5, TimeUnit.SECONDS);
        start.countDown();
        pool.shutdown();
        assertThat(pool.awaitTermination(30, TimeUnit.SECONDS)).isTrue();

        assertThat(success.get()).isEqualTo(1);
        // 성공 1건 외 나머지는 예약 불가로 거절되어야 한다.
        long confirmed = reservationRepository.findByRoomIdAndStatus(room.getId(), ReservationStatus.RESERVED).size();
        assertThat(confirmed).isEqualTo(1);
    }
}
