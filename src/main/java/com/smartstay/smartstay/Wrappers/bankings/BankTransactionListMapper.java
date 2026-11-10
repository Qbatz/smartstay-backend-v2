package com.smartstay.smartstay.Wrappers.bankings;

import com.smartstay.smartstay.dao.BankTransactionsV1;
import com.smartstay.smartstay.dao.BankingMethods;
import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.ennum.BankAccountType;
import com.smartstay.smartstay.responses.banking.BankTransactionResponse;
import com.smartstay.smartstay.util.NameUtils;
import com.smartstay.smartstay.util.Utils;

import java.util.List;
import java.util.function.Function;


public class BankTransactionListMapper implements Function<BankTransactionsV1, BankTransactionResponse> {

    private List<BankingV2> listBanks = null;
    private List<Users> listCreatedBy = null;

    public BankTransactionListMapper(List<BankingV2> listBanks, List<Users> usersList) {
        this.listBanks = listBanks;
        this.listCreatedBy = usersList;
    }
    //    public BankTransactionResponse map(BankTransactionsV1 txn, BankingV2 bank, BankingMethods method,
//            String createdByName, String responsiblePersonName, String cardNetwork, String upiApp) {
//        return new BankTransactionResponse(
//                txn.getCreatedAt() != null ? Utils.dateToTableFormat(txn.getCreatedAt()) : null,
//                txn.getAmount(),
//                txn.getTransactionId(),
//                txn.getAccountBalance(),
//                txn.getBankId(),
//                txn.getCreatedBy(),
//                createdByName,
//                txn.getDescription(),
//                txn.getReferenceNumber(),
//                txn.getSource(),
//                txn.getTransactionNumber(),
//                txn.getType(),
//                txn.getSourceId(),
//                txn.getInvestorName(),
//                bank != null ? bank.getBankAccountType() : null,
//                bank != null ? bank.getAccountNumber() : null,
//                bank != null ? bank.getAccountHolderName() : null,
//                bank != null ? bank.getBankName() : null,
//                bank != null ? bank.getBranchName() : null,
//                bank != null ? bank.getCashAccountType() : null,
//                bank != null ? bank.getDisplayName() : null,
//                bank != null ? bank.getResponsiblePerson() : null,
//                responsiblePersonName,
//                method != null && method.getPaymentMethod() != null ? method.getPaymentMethod() : null,
//                method != null ? method.getCardHolderName() : null,
//                cardNetwork,
//                method != null ? method.getCardNumber() : null,
//                method != null ? method.getDisplayName() : null,
//                method != null ? method.getLinkedUpiId() : null,
//                upiApp);
//    }

    @Override
    public BankTransactionResponse apply(BankTransactionsV1 bankTransactionsV1) {
        String bankAccountType = null;
        String accountType = null;
        String accountHolderName = null;
        String bankName = null;
        String cashAccountType = null;
        String displayName = null;
        String paymentMethod = null;
        String createdBy = null;
        if (listBanks != null) {
            BankingV2 bank = listBanks
                    .stream()
                    .filter(i -> i.getBankId().equalsIgnoreCase(bankTransactionsV1.getBankId()))
                    .findFirst()
                    .orElse(null);
            if (bank != null) {
                bankName = bank.getBankName();
                accountHolderName = bank.getAccountHolderName();
                accountType = bank.getAccountType();
                cashAccountType = bank.getCashAccountType();
                displayName = bank.getDisplayName();
                if (bankTransactionsV1.getPaymentMethodId() != null) {
                    if (bank.getBankingMethods() != null) {
                        BankingMethods bm = bank.getBankingMethods()
                                .stream()
                                .filter(i -> i.getPaymentMethodId().equalsIgnoreCase(bankTransactionsV1.getPaymentMethodId()))
                                .findFirst()
                                .orElse(null);
                        if (bm != null) {
                            bankAccountType = bm.getPaymentMethod();
                            paymentMethod = bm.getPaymentMethod();
                        }
                    }
                }
                else {
                    if (bank.getAccountType().equalsIgnoreCase(BankAccountType.CASH.name())) {
                        paymentMethod = bank.getCashAccountType();
                    }
                }
            }
        }
        if (listCreatedBy != null) {
            Users users = listCreatedBy
                    .stream()
                    .filter(i -> i.getUserId().equalsIgnoreCase(bankTransactionsV1.getCreatedBy()))
                    .findFirst()
                    .orElse(null);
            if (users != null) {
                createdBy = NameUtils.getFullName(users.getFirstName(), users.getLastName());
            }
        }
        return new BankTransactionResponse(Utils.dateToString(bankTransactionsV1.getTransactionDate()),
                Utils.roundOffWithTwoDigit(bankTransactionsV1.getAmount()),
                bankTransactionsV1.getTransactionId(),
                bankTransactionsV1.getAccountBalance(),
                bankTransactionsV1.getBankId(),
                bankTransactionsV1.getCreatedBy(),
                createdBy,
                bankTransactionsV1.getDescription(),
                bankTransactionsV1.getReferenceNumber(),
                bankTransactionsV1.getSource(),
                bankTransactionsV1.getTransactionNumber(),
                bankTransactionsV1.getType(),
                bankTransactionsV1.getSourceId(),
                bankTransactionsV1.getInvestorName(),
                accountType,
                bankAccountType,
                accountHolderName,
                bankName,
                cashAccountType,
                displayName,
                paymentMethod);
    }
}
