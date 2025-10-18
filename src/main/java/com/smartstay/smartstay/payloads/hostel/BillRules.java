package com.smartstay.smartstay.payloads.hostel;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

public record BillRules(
        @Min(value = 1, message = "Start date must be at least 1")
        @Max(value = 28, message = "Start date must be at most 28")
        Integer startDate,
        @Min(value = 1, message = "Due date must be at least 1")
        @Max(value = 28, message = "Due date must be at most 28")
        Integer dueDate,
        @Positive(message = "Invalid notice days")
        Integer noticeDays) {
}
