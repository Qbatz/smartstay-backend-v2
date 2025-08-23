package com.smartstay.smartstay.payloads.complaints;

public record AddComplaints(
        String customerId,
        String complaintType,
        Integer floorId,
        Integer roomId,
        String complaintDate,
        String description,
        String hostelId
) {
}
