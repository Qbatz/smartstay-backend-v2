package com.smartstay.smartstay.responses.rooms;

public record RoomInfoForEB(Integer floorId,
                            Integer roomId,
                            String roomName,
                            String floorName,
                            String hostelId,
                            Long noOfTenants) {
}
