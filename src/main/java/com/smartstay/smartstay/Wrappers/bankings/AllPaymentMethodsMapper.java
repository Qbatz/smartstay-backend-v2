package com.smartstay.smartstay.Wrappers.bankings;

import com.smartstay.smartstay.dao.BankingMethods;
import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.ennum.BankAccountType;
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
        String bankAccountType = null;
        if (bank.getAccountType().equalsIgnoreCase(BankAccountType.CASH.name())) {
            bankAccountType = bank.getCashAccountType();
        }
        else if (bank.getAccountType().equalsIgnoreCase(BankAccountType.BANK.name())) {
            bankAccountType = bank.getBankAccountType();
        }
        else {
            bankAccountType = bank.getBankAccountType();
        }
        return new PaymentMethodOptionResponse(
                bank.getBankId(),
                bank.getAccountHolderName(),
                bank.getAccountNumber(),
                bank.getAccountType(),
                bankAccountType,
                bank.getBalance(),
                bank.getBankName(),
                bank.getBranchName(),
                bank.getDisplayName(),
                bank.getIfscCode(),
                responsiblePerson,
                bank.getResponsiblePerson());
    }

    public PaymentMethodOptionResponse bankMethod(BankingV2 bank, BankingMethods method,
            String cardNetwork, String upiApp, String qrCardImage, String responsiblePerson) {
        return bankMethod(bank, method, cardNetwork, upiApp, qrCardImage, responsiblePerson, null);
    }

    public PaymentMethodOptionResponse bankMethod(BankingV2 bank, BankingMethods method,
            String cardNetwork, String upiApp, String qrCardImage, String responsiblePerson, String dueDate) {
        String bankAccountType = null;
        if (bank.getAccountType().equalsIgnoreCase(BankAccountType.CASH.name())) {
            bankAccountType = bank.getCashAccountType();
        }
        else if (bank.getAccountType().equalsIgnoreCase(BankAccountType.BANK.name())) {
            bankAccountType = bank.getBankAccountType();
        }
        else {
            bankAccountType = bank.getBankAccountType();
        }
        return new PaymentMethodOptionResponse(bank.getBankId(),
                bank.getAccountHolderName(),
                bank.getAccountNumber(),
                bank.getAccountType(),
                bankAccountType,
                method.getBalance(),
                bank.getBankName(),
                bank.getBranchName(),
                method.getDisplayName(),
                bank.getIfscCode(),
                bank.getResponsiblePerson(),
                responsiblePerson);
    }
}
