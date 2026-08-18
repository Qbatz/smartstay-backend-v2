package com.smartstay.smartstay.payloads.banking;

public record CreditCardPayment(
        String creditCardAccount,
        String paymentMethod,
        String transactionId,
        Double amount,
        String settlementDate,
        String description
) {
}
