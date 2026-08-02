package com.smartstay.smartstay.responses.banking;

import java.util.List;

public record OverviewFilterOptions(
        List<FilterOption> dateFilter
) {
}
