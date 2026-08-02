package com.smartstay.smartstay.responses.banking;

import java.util.List;

public record TransactionFilterOptions(
        List<FilterOption> dateFilter,
        List<FilterOption> source
) {
}
