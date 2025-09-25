package com.smartstay.smartstay.responses.rooms;

public record RoomInfoForEB(String roomName,
                            String floorName,
                            String hostelId,
                            Integer roomId,
                            Integer floorId,
                            Integer noOfTenants) {
}
