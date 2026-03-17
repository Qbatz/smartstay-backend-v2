package com.smartstay.smartstay.responses.expenseForReport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpenseReportResponse {
    private String hostelId;
    private FiltersData filtersData;
    private Summary summary;
    private Pagination pagination;
    private List<ExpenseDetail> expenseLists;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class FiltersData {
        private List<CategoryFilter> category;
        private List<SubCategoryFilter> subCategory;
        private List<String> paymentMode;
        private List<UserFilter> createdBy;
        private List<FilterItem> period;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CategoryFilter {
        private Long categoryId;
        private String categoryName;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SubCategoryFilter {
        private Long subCategoryId;
        private String subCategoryName;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class UserFilter {
        private String userId;
        private String userName;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Summary {
        private long totalExpenses;
        private Double totalAmount;
        private String startDate;
        private String endDate;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Pagination {
        private int currentPage;
        private int pageSize;
        private int totalPages;
        private long totalRecords;
        private boolean hasNext;
        private boolean hasPrevious;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ExpenseDetail {
        private String expenseId;
        private String date;
        private String expenseCategory;
        private String expenseSubCategory;
        private String description;
        private int counts;
        private String assetName;
        private String vendorName;
        private String paymentMode;
        private String account;
        private Double amount;
        private String createdBy;
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
