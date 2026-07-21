package com.smartstay.smartstay.responses.banking;

import java.util.List;


public record BankV2ListResponse(
        long totalRecords,
        int currentPage,
        int totalPages,
        int pageSize,
        List<BankV2Response> banks
) {
}
