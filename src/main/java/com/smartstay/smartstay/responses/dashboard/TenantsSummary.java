package com.smartstay.smartstay.responses.dashboard;

public record TenantsSummary(Integer totalTenants, Integer checkInTenants, Integer noticePeriod, String nextCheckout) {
}
