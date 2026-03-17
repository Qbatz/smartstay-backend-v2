package com.smartstay.smartstay.payloads.hostel;

import jakarta.validation.constraints.*;

import java.util.List;

public record BillRules(
        Integer startDate,
        Integer dueDate,
        Integer noticeDays,
        Integer gracePeriodDays,
        @Pattern(regexp = "fixed|FIXED|JOINING_DATE_BASED|joining_date_based", message = "Type must be either 'fixed' or 'joining_date_based'")
        String calculationType,
        List<@NotNull Integer> reminderDays) {
}
