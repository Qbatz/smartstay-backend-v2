package com.smartstay.smartstay.dto.customer;

public record WalletTransactions(String transactionDate,
                                 Double amount,
                                 String source,
                                 String invoiceStatus,
                                 String billStartDate,
                                 String billEndDate,
                                 boolean isInvoiced) {
}
