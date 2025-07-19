package com.smartstay.smartstay.payloads.floor;

public record UpdateFloor(
        String floorName,
        Boolean isActive,
        Boolean isDeleted
) {
}
