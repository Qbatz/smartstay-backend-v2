package com.smartstay.smartstay.payloads.complaints;

public record UpdateComplaintType(
        String complaintTypeName,
        Boolean isActive
) {
}
