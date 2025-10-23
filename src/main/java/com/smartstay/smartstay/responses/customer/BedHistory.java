package com.smartstay.smartstay.responses.customer;

public record BedHistory(
        Long historyId,
        Integer bedId,
        String bedName,
        String roomName,
        String startDate,
        String endDate,
        String reason,
        Double rentAmount,
        String type) {
}
