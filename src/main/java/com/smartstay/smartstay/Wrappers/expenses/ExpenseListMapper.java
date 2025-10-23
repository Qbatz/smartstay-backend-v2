package com.smartstay.smartstay.Wrappers.expenses;

import com.smartstay.smartstay.dto.expenses.ExpenseList;
import com.smartstay.smartstay.ennum.BankAccountType;
import com.smartstay.smartstay.util.Utils;

import java.util.function.Function;

public class ExpenseListMapper implements Function<ExpenseList, com.smartstay.smartstay.responses.expenses.ExpenseList> {
    @Override
    public com.smartstay.smartstay.responses.expenses.ExpenseList apply(ExpenseList expenseList) {
        Integer subCategoryId = 0;
        if (expenseList.getSubCategoryId() == null) {
            subCategoryId = 0;
        }
        else {
            subCategoryId = expenseList.getSubCategoryId().intValue();
        }
        String bankName = null;


        if (expenseList.getAccountType().equalsIgnoreCase(BankAccountType.CASH.name())) {
            bankName = "Cash";
        }
        if (expenseList.getAccountType().equalsIgnoreCase(BankAccountType.CARD.name())) {
            bankName = "Card";
        }
        if (expenseList.getAccountType().equalsIgnoreCase(BankAccountType.UPI.name())) {
            bankName = "Upi";
        }

        if (expenseList.getBankName() != null && !expenseList.getBankName().equalsIgnoreCase("")) {
            bankName = expenseList.getBankName();
        }

        return new com.smartstay.smartstay.responses.expenses.ExpenseList(expenseList.getExpenseId(),
                expenseList.getNoOfItems(),
                expenseList.getCategoryId(),
                subCategoryId,
                expenseList.getDescription(),
                expenseList.getHostelId(),
                expenseList.getBankId(),
                expenseList.getTotalAmount(),
                Utils.dateToString(expenseList.getTransactionDate()),
                expenseList.getUnitPrice(),
                expenseList.getVendorId(),
                expenseList.getReferenceNumber(),
                expenseList.getHolderName(),
                bankName,
                expenseList.getCategoryName(),
                expenseList.getSubCategoryName());
    }
}
