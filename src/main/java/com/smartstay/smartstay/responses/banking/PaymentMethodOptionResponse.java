package com.smartstay.smartstay.responses.banking;

public record PaymentMethodOptionResponse(
        //from bankingv2
        String hostelId,
        String bankId,
        String accountHolderName,
        String accountNumber,
        String accountType,
        Double balance,
        String bankAccountType,
        String bankName,
        String branchName,
        String displayName,
        String ifscCode,
        boolean isDefaultAccount,
        String cashAccountType,
        String responsiblePersonId,
        String responsiblePerson,
        //from banking_methods (null for CASH)
        String paymentMethodId,
        String paymentMethod,
        String cardNumber,
        String upiId,
        String cardHolderName,
        String cardNetwork,
        String upiApp,
        String qrCardImage,
        String qrImage
) {
}
