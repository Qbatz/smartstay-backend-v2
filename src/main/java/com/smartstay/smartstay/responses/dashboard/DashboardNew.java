package com.smartstay.smartstay.responses.dashboard;

import java.util.List;

public record DashboardNew(RoomsAndBedInfo roomsBeds,
                List<OccupancyPoint> occupancyTrend,
                BillingSummary billingSummary,
                StatusSummary tenantComplaints,
                StatusSummary tenantRequests,
                FinanceSummary finance,
                List<RecentCheckin> checkins,
                List<OverdueInvoice> overdueInvoices,
                List<RecentComplaint> complaints,
                List<RecentRequest> request,
                List<String> filters) {
}
