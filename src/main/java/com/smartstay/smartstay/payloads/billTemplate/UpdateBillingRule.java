package com.smartstay.smartstay.payloads.billTemplate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpdateBillingRule(
        @Min(value = 1, message = "Billing start date must be between 1 and 31")
        @Max(value = 31, message = "Billing start date must be between 1 and 31")
        Integer billingStartDate,

        @Min(value = 1, message = "Billing due date must be between 1 and 31")
        @Max(value = 31, message = "Billing due date must be between 1 and 31")
        Integer billingDueDate,

        Integer noticePeriod
) {
}
