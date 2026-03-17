package com.smartstay.smartstay.dto.wallet;

public record WalletTransactions(double amount,
                                 String source,
                                 String sourceId,
                                 String billStartDate,
                                 String billEndDate) {
}
