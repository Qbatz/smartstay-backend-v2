package com.smartstay.smartstay.util;

import java.time.LocalDate;

public class BillingCycle {

    private final LocalDate startDate;
    private final LocalDate endDate;

    public BillingCycle(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }

    @Override
    public String toString() {
        return startDate + " → " + endDate;
    }
}
