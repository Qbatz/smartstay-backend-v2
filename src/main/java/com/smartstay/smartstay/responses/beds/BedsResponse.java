package com.smartstay.smartstay.responses.beds;

public record BedsResponse(int id,
                           String bedName,
                           int roomId,
                           String roomName,
                           String floorName,
                           Integer floorId,
                           boolean isOccupied,
                           boolean onNotice,
                           boolean isBooked,
                           double rentAmount) {
}
