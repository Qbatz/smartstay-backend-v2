package com.smartstay.smartstay.Wrappers.bankings;

import com.smartstay.smartstay.dao.BankingMethods;
import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.ennum.BankAccountType;
import com.smartstay.smartstay.ennum.PaymentMethod;
import com.smartstay.smartstay.responses.banking.BankV2List;
import com.smartstay.smartstay.util.NameUtils;
import com.smartstay.smartstay.util.Utils;

import java.util.List;
import java.util.function.Function;

public class BankingV2ListMapper implements Function<BankingV2, BankV2List> {

    List<BankingV2> bankingList;
    List<Users> listUsers;

    public BankingV2ListMapper(List<BankingV2> bankingList, List<Users> listUsers) {
        this.bankingList = bankingList;
        this.listUsers = listUsers;
    }

    @Override
    public BankV2List apply(BankingV2 bankingV2) {
        String lastTransactionDate = null;
        String cardNumber = null;
        String cardType = null;

        Double creditLimit = 0.0;
        String cardNetwork = null;
        String responsiblePerson = null;
        String linkedBank = null;

        boolean isDeleted = false;
        if (bankingV2.getResponsiblePerson() != null && listUsers != null) {
            Users user = listUsers
                    .stream()
                    .filter(i -> i.getUserId().equalsIgnoreCase(bankingV2.getResponsiblePerson()))
                    .findFirst()
                    .orElse(null);
            if (user != null) {
                responsiblePerson = NameUtils.getFullName(user.getFirstName(), user.getLastName());
            }
        }

        if (bankingV2.getAccountType().equalsIgnoreCase(BankAccountType.CARD.name())) {
            if (bankingV2.getLinkedBankId() != null && bankingList != null) {
                BankingV2 linkedBankAccount = bankingList
                        .stream()
                        .filter(i -> i.getBankId().equalsIgnoreCase(bankingV2.getLinkedBankId()))
                        .findFirst()
                        .orElse(null);
                if (linkedBankAccount != null) {
                    linkedBank = linkedBankAccount.getBankName();
                }
            }
            if (bankingV2.getBankingMethods() != null) {
                BankingMethods bm = bankingV2.getBankingMethods()
                        .stream()
                        .findFirst()
                        .orElse(null);
                if (bm != null) {
                    if (bm.getPaymentMethod().equalsIgnoreCase(PaymentMethod.CREDIT_CARD.name())) {
                        cardType = PaymentMethod.CREDIT_CARD.name();
                        cardNumber = bm.getCardNumber();
                        creditLimit = bm.getCreditLimit();
                        cardNetwork = bm.getCardNetwork();
                    }
                }
            }

        }
        if (bankingV2.getLastTransaction() != null) {
            lastTransactionDate = Utils.dateToString(bankingV2.getLastTransaction());
        }
        Double lastTransactionAmount = 0.0;
        if (bankingV2.getLastTransactionAmount() != null) {
            lastTransactionAmount = Utils.roundOffWithTwoDigit(bankingV2.getLastTransactionAmount());
        }
        if (!bankingV2.isActive()) {
            isDeleted = true;
        }


        return new BankV2List(bankingV2.getBankId(),
                bankingV2.getAccountType(),
                bankingV2.getCashAccountType(),
                bankingV2.getBankName(),
                bankingV2.getDisplayName(),
                bankingV2.getAccountNumber(),
                cardNumber,
                bankingV2.getBranchName(),
                cardType,
                bankingV2.getBalance(),
                bankingV2.getAccountHolderName(),
                bankingV2.getBankAccountType(),
                bankingV2.getIfscCode(),
                responsiblePerson,
                creditLimit,
                linkedBank,
                cardNetwork,
                lastTransactionDate,
                lastTransactionAmount,
                isDeleted);
    }
}
