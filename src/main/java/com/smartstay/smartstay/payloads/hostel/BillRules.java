package com.smartstay.smartstay.payloads.hostel;

import jakarta.validation.constraints.*;

public record BillRules(
        @Min(value = 1, message = "Start date must be at least 1")
        @Max(value = 28, message = "Start date must be at most 28")
        Integer startDate,
        @Min(value = 1, message = "Due date must be at least 1")
        @Max(value = 28, message = "Due date must be at most 28")
        Integer dueDate,
        @Positive(message = "Invalid notice days")
        Integer noticeDays,
        @Min(value = 1, message = "Grace period must be at least 1")
        @Max(value = 28, message = "Grace period must be at most 28")
        Integer gracePeriodDays,
        @Pattern(regexp = "fixed|FIXED|JOINING_DATE_BASED|joining_date_based", message = "Type must be either 'fixed' or 'joining_date_based'")
        String calculationType) {
}
