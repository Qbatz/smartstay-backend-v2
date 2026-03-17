package com.smartstay.smartstay.responses.dashboard;

import java.util.List;

public record DashboardNew(RoomsAndBedInfo roomsBeds,
                Occupancy occupancy,
                TenantsSummary tenantsSummary,
                AdvanceSummary advanceSummary,
                ExpenseSummary expenseSummary,
                OccupancyTrendSummary occupancyTrendSummary,
                BillingSummary billingSummary,
                StatusSummary tenantComplaints,
                StatusSummary tenantRequests,
                FinanceSummary finance,
                RevenueSummary revenueSummary,
                List<RevenueTrendPoint> revenueTrend,
                List<RecentCheckin> checkins,
                List<OverdueInvoice> overdueInvoices,
                List<DashboardRequest> dashboardRequests,
                List<String> filters) {
}
