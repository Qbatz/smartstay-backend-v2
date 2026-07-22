package com.smartstay.smartstay.payloads.customer;

public record UpdateCustomerJob(String employmentStatus,
                                String organizationName,
                                String role,
                                String workLocation,
                                String shiftType,
                                String shiftStartsFrom,
                                String shiftEndsAt) {
}
