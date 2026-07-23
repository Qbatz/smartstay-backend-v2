package com.smartstay.smartstay.payloads.banking;

public record AddBankingMethod(
        String paymentMethod,
        String upiId,
        Integer upiApp,
        String displayName,
        String description,
        String cardNumber,
        Integer cardNetwork,
        String cardHolderName,
        Double creditLimit,
        String billingCycle,
        String linkedUpiId
) {
}
