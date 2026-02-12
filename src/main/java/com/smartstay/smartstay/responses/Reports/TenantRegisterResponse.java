package com.smartstay.smartstay.responses.Reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TenantRegisterResponse {
    private boolean status;
    private String message;
    private DateRange dateRange;
    private Summary summary;
    private Filters filters;
    private List<TenantDetail> tenants;
    private Pagination pagination;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DateRange {
        private String from;
        private String to;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Summary {
        private int totalTenants;
        private SegmentSummary activeTenants;
        private SegmentSummary noticePeriod;
        private SegmentSummary checkoutMTD;
        private SegmentSummary inactive;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SegmentSummary {
        private double count;
        private int trend;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class TenantDetail {
        private String tenantId;
        private String name;
        private String mobileNo;
        private String sharing;
        private String checkInDate;
        private String checkOutDate;
        private double checkInAmount;
        private double checkOutAmount;
        private String stayDuration;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Pagination {
        private int currentPage;
        private int pageSize;
        private long totalRecords;
        private int totalPages;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Filters {
        private List<FilterItem> tenantStatus;
        private List<FilterItem> period;
        private List<FilterItem> floor;
        private List<FilterItem> room;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class FilterItem {
        private Object id;
        private String label;
    }
}
