package com.smartstay.smartstay.responses.expenses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseFilterOptions {

    private List<FilterItems> category;
    private List<SubCategoryItems> subCategory;
    private List<FilterItems> vendor;
    private List<PaymentModeItems> paymentMode;
    private List<FilterItems> createdBy;
    private List<FilterItems> status;
    private List<FilterItems> period;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FilterItems {
        private String name;
        private String type;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SubCategoryItems {
        private String name;
        private String type;
        private String categoryId;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PaymentModeItems {
        private String paymentMethod;
        private String bankId;
        private String paymentMode;
        private String accountName;
    }
}
