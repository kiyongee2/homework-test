package com.spacecl.hotel.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Thymeleaf 예약 폼 바인딩용 객체.
 *
 * <p>REST용 {@link ReservationRequest}와 달리 HTML 폼(input type=date)에서 넘어오는
 * 값을 바인딩하기 위해 {@link DateTimeFormat}을 사용한다.</p>
 */
public class ReservationForm {

    @NotNull(message = "객실을 선택해 주세요.")
    private Long roomId;

    @NotBlank(message = "투숙객 이름을 입력해 주세요.")
    private String guestName;

    @NotBlank(message = "연락처를 입력해 주세요.")
    private String guestPhone;

    @Min(value = 1, message = "투숙 인원은 1명 이상이어야 합니다.")
    private int guestCount = 1;

    @NotNull(message = "체크인 날짜를 선택해 주세요.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkInDate;

    @NotNull(message = "체크아웃 날짜를 선택해 주세요.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkOutDate;

    /** 서비스 계층 요청 객체로 변환. */
    public ReservationRequest toRequest() {
        ReservationRequest req = new ReservationRequest();
        req.setRoomId(roomId);
        req.setGuestName(guestName);
        req.setGuestPhone(guestPhone);
        req.setGuestCount(guestCount);
        req.setCheckInDate(checkInDate);
        req.setCheckOutDate(checkOutDate);
        return req;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getGuestPhone() {
        return guestPhone;
    }

    public void setGuestPhone(String guestPhone) {
        this.guestPhone = guestPhone;
    }

    public int getGuestCount() {
        return guestCount;
    }

    public void setGuestCount(int guestCount) {
        this.guestCount = guestCount;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }
}
