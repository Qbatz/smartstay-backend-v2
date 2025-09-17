package com.smartstay.smartstay.responses.beds;

public record InitializeBedBooking(Integer bedId,
                                   String bedName,
                                   String floorName,
                                   String roomName,
                                   Integer floorId,
                                   Integer roomId,
                                   Double rentAmount,
                                   String currentStatus) {
}
