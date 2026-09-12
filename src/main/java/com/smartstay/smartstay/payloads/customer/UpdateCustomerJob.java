package com.smartstay.smartstay.payloads.customer;

import com.smartstay.smartstay.dto.customer.CustomerJob;

import java.util.List;

public record UpdateCustomerJob(String employmentStatus,
                                String organizationName,
                                String role,
                                String workLocation,
                                String shiftType,
                                String shiftStartsFrom,
                                String shiftEndsAt,
                                List<CustomerJob> customerJobs) {

    public boolean hasJobFields() {
        return isFilled(employmentStatus) || isFilled(organizationName) || isFilled(role) || isFilled(workLocation)
                || isFilled(shiftType) || isFilled(shiftStartsFrom) || isFilled(shiftEndsAt);
    }

    private static boolean isFilled(String value) {
        return value != null && !value.isBlank();
    }
}
