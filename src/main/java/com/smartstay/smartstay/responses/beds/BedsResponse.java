package com.smartstay.smartstay.responses.beds;

public record BedsResponse(int id,
                           String bedName,
                           int roomId,
                           boolean isOccupied,
                           boolean onNotice,
                           boolean isBooked,
                           String nextAvailableFrom,
                           double rentAmount) {
}
