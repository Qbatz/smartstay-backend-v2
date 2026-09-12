package com.smartstay.smartstay.dto.customer;

public record CustomerJob(String hostelId,
                          String customerId,
                          String employmentStatus,
                          String organizationName,
                          String role,
                          String workLocation,
                          String shiftType,
                          String shiftStartsFrom,
                          String shiftEndsAt) {

    public boolean hasJobFields() {
        return isFilled(employmentStatus) || isFilled(organizationName) || isFilled(role) || isFilled(workLocation)
                || isFilled(shiftType) || isFilled(shiftStartsFrom) || isFilled(shiftEndsAt);
    }

    private static boolean isFilled(String value) {
        return value != null && !value.isBlank();
    }
}
