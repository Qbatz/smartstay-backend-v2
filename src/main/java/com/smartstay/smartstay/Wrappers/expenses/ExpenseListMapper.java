package com.smartstay.smartstay.Wrappers.expenses;

import com.smartstay.smartstay.dto.expenses.ExpenseList;
import com.smartstay.smartstay.ennum.BankAccountType;
import com.smartstay.smartstay.ennum.ExpensePaymentStatus;
import com.smartstay.smartstay.responses.expenses.ExpenseItemResponse;
import com.smartstay.smartstay.responses.expenses.ExpensePaymentResponse;
import com.smartstay.smartstay.util.Utils;

import java.util.List;
import java.util.function.Function;

public class ExpenseListMapper implements Function<ExpenseList, com.smartstay.smartstay.responses.expenses.ExpenseList> {
    @Override
    public com.smartstay.smartstay.responses.expenses.ExpenseList apply(ExpenseList expenseList) {
        return apply(expenseList, List.of(), List.of(), ExpensePaymentStatus.fromString(expenseList.getPaymentStatus()));
    }

    public com.smartstay.smartstay.responses.expenses.ExpenseList apply(ExpenseList expenseList,
                                                                        List<ExpenseItemResponse> expenseItems,
                                                                        List<ExpensePaymentResponse> expensePayments) {
        return apply(expenseList, expenseItems, expensePayments,
                ExpensePaymentStatus.fromString(expenseList.getPaymentStatus()));
    }

    public com.smartstay.smartstay.responses.expenses.ExpenseList apply(ExpenseList expenseList,
                                                                        List<ExpenseItemResponse> expenseItems,
                                                                        List<ExpensePaymentResponse> expensePayments,
                                                                        ExpensePaymentStatus paymentStatus) {
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

        Double totalExpenseAmount = expenseItems.stream()
                .filter(item -> item.totalAmount() != null)
                .mapToDouble(ExpenseItemResponse::totalAmount)
                .sum();
        Double totalExpensePaidAmount = expensePayments.stream()
                .filter(payment -> payment.paidAmount() != null)
                .mapToDouble(ExpensePaymentResponse::paidAmount)
                .sum();

        return new com.smartstay.smartstay.responses.expenses.ExpenseList(expenseList.getExpenseId(),
                expenseList.getNoOfItems(),
                expenseList.getCategoryId(),
                subCategoryId,
                expenseList.getDescription(),
                expenseList.getHostelId(),
                expenseList.getBankId(),
                expenseList.getTotalAmount(),
                Utils.dateToString(expenseList.getTransactionDate()),
                Utils.roundOffWithTwoDigit(expenseList.getUnitPrice()),
                expenseList.getVendorId(),
                expenseList.getReferenceNumber(),
                expenseList.getHolderName(),
                bankName,
                expenseList.getCategoryName(),
                expenseList.getSubCategoryName(),
                expenseList.getTitle(),
                expenseList.getIsVendorExpense(),
                paymentStatus,
                expenseList.getPaidAmount(),
                expenseList.getBalanceAmount(),
                expenseList.getPaymentMethod(),
                expenseList.getNote(),
                totalExpenseAmount,
                totalExpensePaidAmount,
                expenseItems,
                expensePayments);
    }
}
