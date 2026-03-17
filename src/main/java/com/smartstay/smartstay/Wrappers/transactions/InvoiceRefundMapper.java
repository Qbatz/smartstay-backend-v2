package com.smartstay.smartstay.Wrappers.transactions;

import com.smartstay.smartstay.dao.BankingV1;
import com.smartstay.smartstay.dao.TransactionV1;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.dto.customer.InvoiceRefundHistory;
import com.smartstay.smartstay.ennum.BankAccountType;
import com.smartstay.smartstay.util.Utils;

import java.util.List;
import java.util.function.Function;

public class InvoiceRefundMapper implements Function<TransactionV1, InvoiceRefundHistory>  {
    List<Users> listAdminUsers = null;
    List<BankingV1> bankIds = null;

    public InvoiceRefundMapper(List<Users> listAdminUsers, List<BankingV1> bankIds) {
        this.listAdminUsers = listAdminUsers;
        this.bankIds = bankIds;
    }

    @Override
    public InvoiceRefundHistory apply(TransactionV1 transactionV1) {
        StringBuilder paidBy = new StringBuilder();
        StringBuilder bank = new StringBuilder();
        double amount = 0;

        if (transactionV1.getPaidAmount() != null) {
            amount = transactionV1.getPaidAmount();
        }

        if (listAdminUsers != null) {
            Users users = listAdminUsers
                    .stream()
                    .filter(i -> i.getUserId().equalsIgnoreCase(transactionV1.getCreatedBy()))
                    .findFirst()
                    .orElse(null);
            if (users != null) {
                if (users.getFirstName() != null ) {
                    paidBy.append(users.getFirstName());
                }
                if (users.getLastName() != null && !users.getLastName().trim().equalsIgnoreCase("")) {
                    paidBy.append(" ");
                    paidBy.append(users.getLastName());
                }
            }
        }

        if (bankIds != null) {
            BankingV1 bankingV1 = bankIds
                    .stream()
                    .filter(i -> i.getBankId().equalsIgnoreCase(transactionV1.getBankId()))
                    .findFirst()
                    .orElse(null);

            if (bankingV1 != null) {
                bank.append(bankingV1.getAccountHolderName());
                if (bankingV1.getAccountType().equalsIgnoreCase(BankAccountType.CASH.name())) {
                    bank.append("-");
                    bank.append("Cash");
                }
                else if(bankingV1.getAccountType().equalsIgnoreCase(BankAccountType.CARD.name())) {
                    if (bankingV1.getAccountHolderName() != null) {

                        bank.append("-");
                        bank.append("Card");
                    }
                }
                else if(bankingV1.getAccountType().equalsIgnoreCase(BankAccountType.BANK.name())) {
                    if (bankingV1.getAccountHolderName() != null) {
                        bank.append("-");
                        bank.append(bankingV1.getBankName());
                    }
                }
                else if(bankingV1.getAccountType().equalsIgnoreCase(BankAccountType.UPI.name())) {
                    if (bankingV1.getAccountHolderName() != null) {
                        bank.append("-");
                        bank.append("Upi");
                    }
                }
            }
        }

        return new InvoiceRefundHistory(transactionV1.getTransactionReferenceId(),
                Utils.dateToString(transactionV1.getPaymentDate()),
                Utils.dateToTime(transactionV1.getPaymentDate()),
                transactionV1.getTransactionMode(),
                amount,
                paidBy.toString(),
                bank.toString());
    }
}
