package com.smartstay.smartstay.payloads.customer;

public record JobDetails(
    String employmentStatus,
    String companyName,
    String collegeName,
    String jobRole,
    String workLocation,
    String shiftType,
    String shiftFrom,
    String shiftTo
) {
}
