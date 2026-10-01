package com.sudo.hotel.controller;

import com.sudo.hotel.dto.RoomResponse;
import com.sudo.hotel.service.RoomService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/rooms")
@RestController
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public List<RoomResponse> all(){
        return roomService.getAllRooms().stream().map(RoomResponse::from).toList();
    }
}
