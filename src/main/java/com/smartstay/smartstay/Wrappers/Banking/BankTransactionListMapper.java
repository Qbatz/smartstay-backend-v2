package com.smartstay.smartstay.Wrappers.Banking;

import com.smartstay.smartstay.dao.BankTransactionsV1;
import com.smartstay.smartstay.dao.BankingMethods;
import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.responses.banking.BankTransactionResponse;
import com.smartstay.smartstay.util.Utils;


public class BankTransactionListMapper {

    public BankTransactionResponse map(BankTransactionsV1 txn, BankingV2 bank, BankingMethods method,
            String createdByName, String responsiblePersonName, String cardNetwork, String upiApp) {
        return new BankTransactionResponse(
                txn.getCreatedAt() != null ? Utils.dateToTableFormat(txn.getCreatedAt()) : null,
                txn.getAmount(),
                txn.getTransactionId(),
                txn.getAccountBalance(),
                txn.getBankId(),
                txn.getCreatedBy(),
                createdByName,
                txn.getDescription(),
                txn.getReferenceNumber(),
                txn.getSource(),
                txn.getTransactionNumber(),
                txn.getType(),
                txn.getSourceId(),
                txn.getInvestorName(),
                bank != null ? bank.getBankAccountType() : null,
                bank != null ? bank.getAccountNumber() : null,
                bank != null ? bank.getAccountHolderName() : null,
                bank != null ? bank.getBankName() : null,
                bank != null ? bank.getBranchName() : null,
                bank != null ? bank.getCashAccountType() : null,
                bank != null ? bank.getDisplayName() : null,
                bank != null ? bank.getResponsiblePerson() : null,
                responsiblePersonName,
                method != null && method.getPaymentMethod() != null ? method.getPaymentMethod().getValue() : null,
                method != null ? method.getCardHolderName() : null,
                cardNetwork,
                method != null ? method.getCardNumber() : null,
                method != null ? method.getDisplayName() : null,
                method != null ? method.getLinkedUpiId() : null,
                upiApp);
    }
}
