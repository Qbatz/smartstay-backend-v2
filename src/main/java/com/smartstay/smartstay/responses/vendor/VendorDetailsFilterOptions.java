package com.smartstay.smartstay.responses.vendor;

import java.util.List;

public record VendorDetailsFilterOptions(List<PeriodFilter> periods) {

    public record PeriodFilter(String name, String type) {
    }
}
