package com.smartstay.smartstay.payloads.complaints;

public record UpdateComplaint(
        String customerId,
        String complaintType,
        Integer floorId,
        Integer roomId,
        String complaintDate,
        String description
) {
}
