package com.smartstay.smartstay.dto.customer;

public record Summary(int totalTenantsCount,
                      int vacatedCount,
                      int bookedCounts,
                      int settlementGenerated,
                      int noticePeriodCounts,
                      int checkedInCounts) {
}
