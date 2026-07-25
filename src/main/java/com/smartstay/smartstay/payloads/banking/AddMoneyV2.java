package com.smartstay.smartstay.payloads.banking;

public record AddMoneyV2(
        String bankId,
        String paymentMethodId,
        Double amount
) {
}
