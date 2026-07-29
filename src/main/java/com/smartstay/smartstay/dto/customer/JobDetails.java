package com.smartstay.smartstay.dto.customer;

public record JobDetails(String employmentStatus,
                         String organizationName,
                         String role,
                         String workLocation,
                         String shiftType,
                         String shiftStartTime,
                         String shiftEndTime) {
}
