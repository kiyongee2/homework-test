package com.spacecl.hotel.controller;

import com.spacecl.hotel.domain.Reservation;
import com.spacecl.hotel.dto.ReservationForm;
import com.spacecl.hotel.exception.InvalidReservationException;
import com.spacecl.hotel.exception.ReservationNotFoundException;
import com.spacecl.hotel.exception.RoomNotAvailableException;
import com.spacecl.hotel.exception.RoomNotFoundException;
import com.spacecl.hotel.service.ReservationService;
import com.spacecl.hotel.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/**
 * Thymeleaf 화면 컨트롤러.
 *
 * <ul>
 *   <li>GET  /                         : 객실 검색 페이지로 이동</li>
 *   <li>GET  /rooms                    : 객실/예약 가능 객실 조회</li>
 *   <li>GET  /reservations/new         : 예약 폼</li>
 *   <li>POST /reservations             : 예약 생성</li>
 *   <li>GET  /reservations             : 예약 목록</li>
 *   <li>GET  /reservations/{id}        : 예약 상세</li>
 *   <li>POST /reservations/{id}/cancel : 예약 취소</li>
 * </ul>
 */
@Controller
public class WebController {

    private final RoomService roomService;
    private final ReservationService reservationService;

    public WebController(RoomService roomService, ReservationService reservationService) {
        this.roomService = roomService;
        this.reservationService = reservationService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/rooms";
    }

    /** 객실 목록 / 예약 가능 객실 조회. */
    @GetMapping("/rooms")
    public String rooms(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            Model model) {

        boolean searched = checkIn != null && checkOut != null;
        if (searched) {
            try {
                model.addAttribute("rooms", roomService.getAvailableRooms(checkIn, checkOut));
            } catch (InvalidReservationException e) {
                searched = false;
                model.addAttribute("error", e.getMessage());
                model.addAttribute("rooms", roomService.getAllRooms());
            }
        } else {
            model.addAttribute("rooms", roomService.getAllRooms());
        }
        model.addAttribute("searched", searched);
        model.addAttribute("checkIn", checkIn);
        model.addAttribute("checkOut", checkOut);
        return "rooms";
    }

    /** 예약 폼 표시. */
    @GetMapping("/reservations/new")
    public String newReservation(
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            Model model) {

        ReservationForm form = new ReservationForm();
        form.setRoomId(roomId);
        form.setCheckInDate(checkIn != null ? checkIn : LocalDate.now().plusDays(1));
        form.setCheckOutDate(checkOut != null ? checkOut : LocalDate.now().plusDays(2));
        model.addAttribute("form", form);
        model.addAttribute("rooms", roomService.getAllRooms());
        return "reservation-form";
    }

    /** 예약 생성. */
    @PostMapping("/reservations")
    public String createReservation(@Valid @ModelAttribute("form") ReservationForm form,
                                    BindingResult bindingResult,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("rooms", roomService.getAllRooms());
            return "reservation-form";
        }
        try {
            Reservation reservation = reservationService.reserve(form.toRequest());
            redirectAttributes.addFlashAttribute("message",
                    reservation.getRoom().getRoomNumber() + "호 예약이 완료되었습니다.");
            return "redirect:/reservations/" + reservation.getId();
        } catch (RoomNotAvailableException | InvalidReservationException | RoomNotFoundException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("rooms", roomService.getAllRooms());
            return "reservation-form";
        }
    }

    /** 예약 목록. */
    @GetMapping("/reservations")
    public String reservations(Model model) {
        model.addAttribute("reservations", reservationService.getAllReservations());
        return "reservations";
    }

    /** 예약 상세. */
    @GetMapping("/reservations/{id}")
    public String reservationDetail(@PathVariable Long id, Model model,
                                    RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("reservation", reservationService.getReservation(id));
            return "reservation-detail";
        } catch (ReservationNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reservations";
        }
    }

    /** 예약 취소. */
    @PostMapping("/reservations/{id}/cancel")
    public String cancelReservation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reservationService.cancel(id);
            redirectAttributes.addFlashAttribute("message", "예약이 취소되었습니다.");
        } catch (ReservationNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reservations";
    }
}
