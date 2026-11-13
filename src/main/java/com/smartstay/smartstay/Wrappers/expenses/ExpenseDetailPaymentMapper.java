package com.smartstay.smartstay.Wrappers.expenses;

import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.dao.ExpensePayment;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.responses.expenses.ExpenseDetailPayment;
import com.smartstay.smartstay.util.BankUtils;
import com.smartstay.smartstay.util.NameUtils;
import com.smartstay.smartstay.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ExpenseDetailPaymentMapper implements Function<ExpensePayment, ExpenseDetailPayment> {

    Map<String, BankingV2> bankMap = null;
    List<Users> users = null;

    public ExpenseDetailPaymentMapper(Map<String, BankingV2> bankMap, List<Users> users) {
        this.bankMap = bankMap;
        this.users = users;
    }

    @Override
    public ExpenseDetailPayment apply(ExpensePayment p) {
        BankingV2 paymentBank = bankMap.get(p.getBankId());

        String bankName = null;
        if (paymentBank != null) {
            bankName = BankUtils.getBankName(paymentBank, p.getPaymentMethod()).toString();
        }
        String userName = null;
        if (users != null && users.size() > 0) {

            Users user = users
                    .stream()
                    .filter(i -> i.getUserId().equalsIgnoreCase(p.getCreatedBy()))
                    .findFirst()
                    .orElse(null);
            if (user != null) {
                userName = NameUtils.getFullName(user.getFirstName(), user.getLastName());
            }
        }
        return new ExpenseDetailPayment(
                p.getId(), p.getPaidAmount(),
                p.getPaymentMethod(),
                p.getBankId(),
                bankName,
                paymentBank != null ? paymentBank.getAccountType() : null,
                p.getPaymentDate() != null ? Utils.dateToString(p.getPaymentDate()) : null,
                p.getTransactionId(), p.getNotes(), p.getImageUrl(), p.getImageUrls(),
                p.getCreatedAt() != null ? Utils.dateToString(p.getCreatedAt()) : null,
                userName);
    }
}
