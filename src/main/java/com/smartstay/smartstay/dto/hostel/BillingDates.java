package com.smartstay.smartstay.dto.hostel;

import java.util.Date;

public record BillingDates(Date currentBillStartDate,
                           Date currentBillEndDate,
                           Date dueDate,
                           Integer dueDays,
                           boolean hasGracePeriod,
                           Integer gracePeriodDays,
                           String typeOfBilling,
                           String billingModel) {
}
