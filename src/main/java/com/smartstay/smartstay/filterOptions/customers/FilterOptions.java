package com.smartstay.smartstay.filterOptions.customers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilterOptions {

    private List<FilterItems> status;
    private List<FilterItems> periods;
    private List<FilterItems> sharingType;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FilterItems {
        private String name;
        private String type;
    }
}
