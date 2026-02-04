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
    private String startDate;
    private String endDate;
    private int totalExpenses;
    private Double totalAmount;
    private List<ExpenseDetail> expenseLists;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ExpenseDetail {
        private String date;
        private String expenseCategory;
        private String expenseSubCategory;
        private String description;
        private int counts;
        private String assetsName;
        private String vendorName;
        private String account;
    }
}
