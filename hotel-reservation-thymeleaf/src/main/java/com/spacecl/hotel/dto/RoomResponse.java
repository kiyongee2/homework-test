package com.spacecl.hotel.dto;

import com.spacecl.hotel.domain.Room;

/**
 * 객실 정보 응답.
 */
public record RoomResponse(
        Long id,
        String roomNumber,
        String type,
        String typeName,
        int capacity
) {
    public static RoomResponse from(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getRoomNumber(),
                room.getType().name(),
                room.getType().getDisplayName(),
                room.getCapacity()
        );
    }
}
