package com.smartstay.smartstay.responses.expenses;

import com.smartstay.smartstay.ennum.ExpensePaymentStatus;

import java.util.List;

public record ExpenseList(String expenseId,
                          int itemsCount,
                          long categoryId,
                          long subCategoryId,
                          String description,
                          String hostelId,
                          String bankId,
                          Double totalAmount,
                          String transactionDate,
                          Double unitPrice,
                          String vendorId,
                          String referenceNumber,
                          String accountHolderName,
                          String bankName,
                          String categoryName,
                          String subCategoryName,
                          String title,
                          Boolean isVendorExpense,
                          ExpensePaymentStatus paymentStatus,
                          Double paidAmount,
                          Double balanceAmount,
                          String paymentMethod,
                          String note,
                          Double totalExpenseAmount,
                          Double totalExpensePaidAmount,
                          List<ExpenseItemResponse> expenseItems,
                          List<ExpensePaymentResponse> expensePayments) {
}
