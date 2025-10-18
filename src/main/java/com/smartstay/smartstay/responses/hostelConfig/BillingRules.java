package com.smartstay.smartstay.responses.hostelConfig;

import java.util.Date;

public record BillingRules(Integer billStartDate,
                           Integer billDueDate,
                           Integer noticePeriod,
                           String startsFrom) {
}
