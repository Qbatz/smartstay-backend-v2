package com.smartstay.smartstay.payloads.banking;

public record AddBankingMethod(
        String paymentMethod,
        String upiId,
        String upiApp,
        String displayName,
        String description,
        String cardNumber,
        String cardNetwork,
        String cardHolderName,
        Double creditLimit,
        String billingCycle,
        String linkedUpiId
) {
}
