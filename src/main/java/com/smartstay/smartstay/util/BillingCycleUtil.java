package com.smartstay.smartstay.util;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

public class BillingCycleUtil {
    public static List<BillingCycle> findBillingCycles(LocalDate lastBillingDate, LocalDate tillDate, int billingStartDay) {
        List<BillingCycle> cycles = new ArrayList<>();

        // Start from the first billing cycle after lastBillingDate
        LocalDate start = lastBillingDate;
        while (start.isBefore(tillDate)) {
            LocalDate nextMonthSameDay = start.plusMonths(1).withDayOfMonth(billingStartDay);

            if (nextMonthSameDay.getMonthValue() != start.plusMonths(1).getMonthValue()) {
                nextMonthSameDay = start.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
            }

            LocalDate end = nextMonthSameDay.minusDays(1);
            cycles.add(new BillingCycle(start, end));

            start = nextMonthSameDay;
        }

        return cycles;
    }
}
