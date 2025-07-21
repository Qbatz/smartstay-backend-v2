package com.smartstay.smartstay.payloads.rooms;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record AddRoom(
        @NotNull(message = "Room name is required") @NotEmpty(message = "Room name is required") String roomName,
        @NotNull
        int floorId

) {
}
