package com.smartstay.smartstay.responses.banking;

import java.util.List;

public record BankTransactionListResponse(
        long totalRecords,
        int currentPage,
        int totalPages,
        int pageSize,
        String hostelId,
        TransactionFilterOptions filterOptions,
        BankListSummary bankListSummary,
        List<BankTransactionResponse> transactions,
        List<BankV2List> bankV2Lists
) {
}
