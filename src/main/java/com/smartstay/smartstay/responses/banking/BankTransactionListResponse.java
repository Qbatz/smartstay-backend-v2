package com.smartstay.smartstay.responses.banking;

import java.util.List;

public record BankTransactionListResponse(
        long totalRecords,
        int currentPage,
        int totalPages,
        int pageSize,
        List<BankTransactionResponse> transactions
) {
}
