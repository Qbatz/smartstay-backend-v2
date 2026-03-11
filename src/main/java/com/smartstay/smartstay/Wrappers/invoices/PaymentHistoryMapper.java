package com.smartstay.smartstay.Wrappers.invoices;

import com.smartstay.smartstay.dao.BankingV1;
import com.smartstay.smartstay.dao.TransactionV1;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.dto.customer.InvoiceRefundHistory;

import java.util.List;
import java.util.function.Function;

public class PaymentHistoryMapper implements Function<TransactionV1, InvoiceRefundHistory> {

    List<BankingV1> listBankings = null;
    List<Users> listUsers = null;
    String tag = null;

    public PaymentHistoryMapper(List<BankingV1> listBankings, List<Users> listUsers, String tag) {
        this.listBankings = listBankings;
        this.listUsers = listUsers;
        this.tag = tag;
    }

    @Override
    public InvoiceRefundHistory apply(TransactionV1 transactionV1) {
        return null;
    }
}
