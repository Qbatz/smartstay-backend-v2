package com.smartstay.smartstay.responses.banking;

public record BankingMethodResponse(
        String paymentMethodId,
        String bankId,
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
        String linkedUpiId,
        String qrImage,
        String hostelId,
        String userId,
        Double balance,
        String createdAt,
        String updatedAt,
        String createdBy,
        String updatedBy
) {
}
