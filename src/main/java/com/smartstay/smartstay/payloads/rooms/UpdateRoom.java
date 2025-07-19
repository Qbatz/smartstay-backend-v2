package com.smartstay.smartstay.payloads.rooms;

public record UpdateRoom(
        String roomName,
        Boolean isActive,
        Boolean isDeleted
) {
}
