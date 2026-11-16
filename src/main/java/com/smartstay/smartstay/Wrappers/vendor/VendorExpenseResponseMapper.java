package com.smartstay.smartstay.Wrappers.vendor;

import com.smartstay.smartstay.dao.BankingV1;
import com.smartstay.smartstay.dao.BankingV2;
import com.smartstay.smartstay.dao.ExpensePayment;
import com.smartstay.smartstay.responses.vendor.VendorExpensePaymentResponse;
import com.smartstay.smartstay.util.BankUtils;
import com.smartstay.smartstay.util.Utils;

import java.util.List;
import java.util.function.Function;

public class VendorExpenseResponseMapper implements Function<ExpensePayment, VendorExpensePaymentResponse> {

    private List<BankingV2> listBanks = null;

    public VendorExpenseResponseMapper(List<BankingV2> listBanks) {
        this.listBanks = listBanks;
    }

    @Override
    public VendorExpensePaymentResponse apply(ExpensePayment expensePayment) {
        String bankName = null;
        if (listBanks != null) {
            BankingV2 bankingV2 = listBanks
                    .stream()
                    .filter(i -> i.getBankId().equalsIgnoreCase(expensePayment.getBankId()))
                    .findFirst()
                    .orElse(null);
            if (bankingV2 != null) {
                bankName = BankUtils.getBankName(bankingV2, expensePayment.getPaymentMethod()).toString();
            }
        }
        return new VendorExpensePaymentResponse(
                expensePayment.getId(),
                expensePayment.getPaidAmount(),
                null,
                expensePayment.getExpenseId(),
                expensePayment.getBankId(),
                bankName,
                expensePayment.getHostelId(),
                Utils.dateToString(expensePayment.getPaymentDate()),
                expensePayment.getTransactionId(),
                expensePayment.getNotes(),
                expensePayment.getImageUrl());
    }
}
