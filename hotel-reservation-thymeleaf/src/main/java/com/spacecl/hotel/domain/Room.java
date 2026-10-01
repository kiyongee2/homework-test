package com.spacecl.hotel.domain;

import jakarta.persistence.*;

/**
 * 객실 엔티티.
 */
@Entity
@Table(name = "room")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 객실 번호 (예: "101") */
    @Column(nullable = false, unique = true)
    private String roomNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType type;

    /** 최대 수용 인원 */
    @Column(nullable = false)
    private int capacity;

    protected Room() {
    }

    public Room(String roomNumber, RoomType type, int capacity) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.capacity = capacity;
    }

    public Long getId() {
        return id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public RoomType getType() {
        return type;
    }

    public int getCapacity() {
        return capacity;
    }
}
