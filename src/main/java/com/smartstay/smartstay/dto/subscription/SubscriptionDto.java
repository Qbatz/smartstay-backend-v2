package com.smartstay.smartstay.dto.subscription;

import java.util.Date;

public record SubscriptionDto(Date startDate,
                              Date endDate,
                              Date nextBillingDate,
                              boolean isValid,
                              Integer endsIn) {
}
