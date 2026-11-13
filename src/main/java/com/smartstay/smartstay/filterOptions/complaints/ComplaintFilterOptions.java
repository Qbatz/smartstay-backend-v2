package com.smartstay.smartstay.filterOptions.complaints;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComplaintFilterOptions {

    private List<FilterItems> status;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FilterItems {
        private String name;
        private String type;
    }
}
