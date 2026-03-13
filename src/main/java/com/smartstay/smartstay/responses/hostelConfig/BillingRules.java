package com.smartstay.smartstay.responses.hostelConfig;


import java.util.List;

public record BillingRules(Integer billStartDate,
                           Integer billDueDate,
                           Integer noticePeriod,
                           String typeOfBilling,
                           Integer gracePeriod,
                           boolean hasGracePeriod,
                           List<Integer> reminderDays) {
}
