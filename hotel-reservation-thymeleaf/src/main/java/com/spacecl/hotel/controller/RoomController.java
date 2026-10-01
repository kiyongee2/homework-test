package com.spacecl.hotel.controller;

import com.spacecl.hotel.dto.RoomResponse;
import com.spacecl.hotel.service.RoomService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 객실 API.
 *
 * <ul>
 *   <li>GET /api/rooms                                   : 전체 객실 목록</li>
 *   <li>GET /api/rooms/available?checkIn=&checkOut=      : 특정 기간 예약 가능 객실</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public List<RoomResponse> all() {
        return roomService.getAllRooms().stream().map(RoomResponse::from).toList();
    }

    @GetMapping("/available")
    public List<RoomResponse> available(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return roomService.getAvailableRooms(checkIn, checkOut).stream()
                .map(RoomResponse::from)
                .toList();
    }
}
