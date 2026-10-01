package com.spacecl.hotel;

import com.spacecl.hotel.domain.Reservation;
import com.spacecl.hotel.domain.Room;
import com.spacecl.hotel.domain.RoomType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 기간 중복 판정 로직(Reservation.overlaps)에 대한 순수 단위 테스트.
 * Spring 컨텍스트 없이 도메인 규칙만 검증한다.
 */
@DisplayName("예약 기간 중복 판정 테스트")
class ReservationOverlapTest {

    private final Room room = new Room("101", RoomType.SINGLE, 1);

    private Reservation reservationOf(String checkIn, String checkOut) {
        return new Reservation(room, "홍길동", "010-0000-0000", 1,
                LocalDate.parse(checkIn), LocalDate.parse(checkOut));
    }

    @Test
    @DisplayName("완전히 겹치는 기간은 중복이다")
    void fullOverlap() {
        Reservation r = reservationOf("2026-01-10", "2026-01-15");
        assertThat(r.overlaps(LocalDate.parse("2026-01-11"), LocalDate.parse("2026-01-14"))).isTrue();
    }

    @Test
    @DisplayName("부분적으로 겹치는 기간은 중복이다")
    void partialOverlap() {
        Reservation r = reservationOf("2026-01-10", "2026-01-15");
        assertThat(r.overlaps(LocalDate.parse("2026-01-14"), LocalDate.parse("2026-01-20"))).isTrue();
        assertThat(r.overlaps(LocalDate.parse("2026-01-05"), LocalDate.parse("2026-01-11"))).isTrue();
    }

    @Test
    @DisplayName("기존 예약을 완전히 포함하는 기간은 중복이다")
    void enclosingOverlap() {
        Reservation r = reservationOf("2026-01-10", "2026-01-15");
        assertThat(r.overlaps(LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-31"))).isTrue();
    }

    @Test
    @DisplayName("체크아웃 당일에 다른 손님이 체크인하는 것은 중복이 아니다 (종료일 배타)")
    void checkoutDayIsNotOverlap() {
        Reservation r = reservationOf("2026-01-10", "2026-01-15");
        // 기존 예약: 10~15, 신규: 15~18 → 15일에 이어서 입실, 중복 아님
        assertThat(r.overlaps(LocalDate.parse("2026-01-15"), LocalDate.parse("2026-01-18"))).isFalse();
        // 신규가 먼저 끝나고 기존이 시작: 07~10, 기존 10~15 → 중복 아님
        assertThat(r.overlaps(LocalDate.parse("2026-01-07"), LocalDate.parse("2026-01-10"))).isFalse();
    }

    @Test
    @DisplayName("전혀 겹치지 않는 기간은 중복이 아니다")
    void noOverlap() {
        Reservation r = reservationOf("2026-01-10", "2026-01-15");
        assertThat(r.overlaps(LocalDate.parse("2026-02-01"), LocalDate.parse("2026-02-05"))).isFalse();
    }

    @Test
    @DisplayName("취소된 예약은 어떤 기간과도 중복되지 않는다")
    void cancelledDoesNotOverlap() {
        Reservation r = reservationOf("2026-01-10", "2026-01-15");
        r.cancel();
        assertThat(r.overlaps(LocalDate.parse("2026-01-11"), LocalDate.parse("2026-01-14"))).isFalse();
    }
}
