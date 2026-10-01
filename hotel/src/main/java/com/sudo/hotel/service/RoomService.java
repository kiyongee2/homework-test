package com.sudo.hotel.service;

import com.sudo.hotel.domain.Room;
import com.sudo.hotel.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {
    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository){
        this.roomRepository = roomRepository;
    }

    /* 전체 객실 목록 */
    public List<Room> getAllRooms(){
        return roomRepository.findAll();
    }
}
