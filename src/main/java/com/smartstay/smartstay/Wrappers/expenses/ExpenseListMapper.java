package com.smartstay.smartstay.Wrappers.expenses;

import com.smartstay.smartstay.dto.expenses.ExpenseList;
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
                expenseList.getBankName(),
                expenseList.getCategoryName(),
                expenseList.getSubCategoryName());
    }
}
