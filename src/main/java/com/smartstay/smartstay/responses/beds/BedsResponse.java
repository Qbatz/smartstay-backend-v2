package com.smartstay.smartstay.responses.beds;

public record BedsResponse(int id,
                           String name,
                           int roomId,
                           boolean isOccupied,
                           boolean onNotice,
                           boolean isBooked,
                           String nextAvailableFrom,
                           double rentAmount) {
}
