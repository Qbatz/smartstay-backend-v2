package com.smartstay.smartstay.responses.customer;

public record BedHistory(
        Long historyId,
        Integer bedId,
        Integer roomId,
        String bedName,
        String roomName,
        String startDate,
        String endDate,
        String reason,
        Double rentAmount,
        String type) {
}
