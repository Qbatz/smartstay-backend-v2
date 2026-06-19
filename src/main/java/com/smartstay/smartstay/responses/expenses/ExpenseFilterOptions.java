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

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FilterItems {
        private String name;
        private String type;
    }
}
