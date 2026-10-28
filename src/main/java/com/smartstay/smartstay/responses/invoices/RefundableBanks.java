package com.smartstay.smartstay.responses.invoices;

public record RefundableBanks(String bankId,
                              boolean isBank,
                              String bankName,
                              String type,
                              String accountHolderName) {
}
