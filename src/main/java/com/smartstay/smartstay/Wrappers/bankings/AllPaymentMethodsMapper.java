package com.smartstay.smartstay.Wrappers.bankings;

import com.smartstay.smartstay.dao.BankingMethods;
import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.responses.banking.PaymentMethodOptionResponse;

public class AllPaymentMethodsMapper {

    public PaymentMethodOptionResponse cash(BankingV2 bank, String responsiblePerson) {
        return account(bank, responsiblePerson, null);
    }

    public PaymentMethodOptionResponse cash(BankingV2 bank, String responsiblePerson, String lastTransactionDate) {
        return account(bank, responsiblePerson, lastTransactionDate);
    }

    public PaymentMethodOptionResponse bankAccount(BankingV2 bank, String responsiblePerson) {
        return account(bank, responsiblePerson, null);
    }

    public PaymentMethodOptionResponse bankAccount(BankingV2 bank, String responsiblePerson, String lastTransactionDate) {
        return account(bank, responsiblePerson, lastTransactionDate);
    }

    private PaymentMethodOptionResponse account(BankingV2 bank, String responsiblePerson, String lastTransactionDate) {
        return new PaymentMethodOptionResponse(
                bank.getHostelId(),
                bank.getBankId(),
                bank.getAccountHolderName(),
                bank.getAccountNumber(),
                bank.getAccountType(),
                bank.getBalance(),
                bank.getBankAccountType(),
                bank.getBankName(),
                bank.getBranchName(),
                bank.getDisplayName(),
                bank.getIfscCode(),
                bank.isDefaultAccount(),
                bank.getCashAccountType(),
                bank.getResponsiblePerson(),
                responsiblePerson,
                null, null, null, null, null, null, null, null, null,
                lastTransactionDate,
                null);
    }

    public PaymentMethodOptionResponse bankMethod(BankingV2 bank, BankingMethods method,
            String cardNetwork, String upiApp, String qrCardImage, String responsiblePerson) {
        return bankMethod(bank, method, cardNetwork, upiApp, qrCardImage, responsiblePerson, null);
    }

    public PaymentMethodOptionResponse bankMethod(BankingV2 bank, BankingMethods method,
            String cardNetwork, String upiApp, String qrCardImage, String responsiblePerson, String dueDate) {
        return new PaymentMethodOptionResponse(
                bank.getHostelId(),
                bank.getBankId(),
                bank.getAccountHolderName(),
                bank.getAccountNumber(),
                bank.getAccountType(),
                method.getBalance(),
                bank.getBankAccountType(),
                bank.getBankName(),
                bank.getBranchName(),
                method.getDisplayName(),
                bank.getIfscCode(),
                bank.isDefaultAccount(),
                bank.getCashAccountType(),
                bank.getResponsiblePerson(),
                responsiblePerson,
                method.getPaymentMethodId(),
                method.getPaymentMethod() != null ? method.getPaymentMethod().getValue() : null,
                method.getCardNumber(),
                method.getUpiId(),
                method.getCardHolderName(),
                cardNetwork,
                upiApp,
                qrCardImage,
                method.getQrImage(),
                null,
                dueDate);
    }
}
