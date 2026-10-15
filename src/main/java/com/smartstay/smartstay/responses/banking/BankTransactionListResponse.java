package com.smartstay.smartstay.responses.banking;

import java.util.List;

public record BankTransactionListResponse(
        long totalRecords,
        int currentPage,
        int totalPages,
        int pageSize,
        String hostelId,
        TransactionFilterOptions filterOptions,
        List<PaymentMethodOptionResponse> bankList,
        List<BankTransactionResponse> transactions,
        List<BankV2List> bankV2Lists
) {
}
