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

    public boolean hasJobFields() {
        return isFilled(employmentStatus) || isFilled(companyName) || isFilled(collegeName) || isFilled(jobRole)
                || isFilled(workLocation) || isFilled(shiftType) || isFilled(shiftFrom) || isFilled(shiftTo);
    }

    private static boolean isFilled(String value) {
        return value != null && !value.isBlank();
    }
}
