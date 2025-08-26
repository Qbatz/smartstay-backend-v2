package com.smartstay.smartstay.payloads.complaints;

public record UpdateComplaint(
        String customerId,
        Integer complaintTypeId,
        Integer floorId,
        Integer roomId,
        String complaintDate,
        String description
) {
}
