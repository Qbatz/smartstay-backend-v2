package com.smartstay.smartstay.responses.banking;

public record PaymentMethodOptionResponse(
        String bankId,
        String accountHolderName,
        String accountNumber,
        String accountType,
        Double balance,
        String bankName,
        String branchName,
        String displayName,
        String ifscCode,
        String responsiblePersonId,
        String responsiblePerson,
        //from banking_methods (null for CASH)
        String paymentMethodId,
        String cardNumber,
        String upiId,
        String cardHolderName,
        String cardNetwork,
        String upiApp,
        String upiAppIcon
) {
}
