package com.smartstay.smartstay.Wrappers.transactions;

import com.smartstay.smartstay.dao.BankingV1;
import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.dao.TransactionV1;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.dto.customer.InvoiceRefundHistory;
import com.smartstay.smartstay.ennum.BankAccountType;
import com.smartstay.smartstay.util.BankUtils;
import com.smartstay.smartstay.util.Utils;

import java.util.List;
import java.util.function.Function;

public class InvoiceRefundMapper implements Function<TransactionV1, InvoiceRefundHistory>  {
    List<Users> listAdminUsers = null;
    List<BankingV2> bankIds = null;

    public InvoiceRefundMapper(List<Users> listAdminUsers, List<BankingV2> bankIds) {
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
            BankingV2 bankingV2 = bankIds
                    .stream()
                    .filter(i -> i.getBankId().equalsIgnoreCase(transactionV1.getBankId()))
                    .findFirst()
                    .orElse(null);

            if (bankingV2 != null) {
                bank = BankUtils.getBankName(bankingV2, transactionV1.getPaymentMethodId());
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
