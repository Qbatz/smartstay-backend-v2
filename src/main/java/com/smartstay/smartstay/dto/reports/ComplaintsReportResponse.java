package com.smartstay.smartstay.dto.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ComplaintsReportResponse {
    private boolean status;
    private String message;
    private DateRange dateRange;
    private Summary summary;
    private FilterValues filters;
    private List<ComplaintDetail> complaints;
    private Pagination pagination;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DateRange {
        private String from;
        private String to;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Summary {
        private int total;
        private int resolved;
        private int inprogress;
        private int completed;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FilterValues {
        private List<LabelValue> complaintCategories;
        private List<LabelValue> period;
        private List<LabelValue> status;
        private List<UserFilter> raisedBy;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LabelValue {
        private String label;
        private Object value;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserFilter {
        private String user_id;
        private String user_name;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Pagination {
        private int currentPage;
        private int pageSize;
        private long totalRecords;
        private int totalPages;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ComplaintDetail {
        private Integer complaintId;
        private String complaintType;
        private String raisedBy;
        private String status;
        private String assignedTo;
        private String complaintDate;
        private String roomName;
        private String bedName;
        private String floorName;
    }
}
