package com.spacecl.hotel.domain;

/**
 * 객실 타입.
 */
public enum RoomType {
    SINGLE("싱글", 1),
    DOUBLE("더블", 2),
    TWIN("트윈", 2),
    SUITE("스위트", 4);

    private final String displayName;
    private final int defaultCapacity;

    RoomType(String displayName, int defaultCapacity) {
        this.displayName = displayName;
        this.defaultCapacity = defaultCapacity;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getDefaultCapacity() {
        return defaultCapacity;
    }
}
