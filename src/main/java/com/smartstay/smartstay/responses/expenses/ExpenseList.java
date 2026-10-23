package com.smartstay.smartstay.responses.expenses;

import com.smartstay.smartstay.ennum.ExpensePaymentStatus;

import java.util.List;

public record ExpenseList(String expenseId,
                          int itemsCount,
                          Long categoryId,
                          Long subCategoryId,
                          String description,
                          String bankId,
                          Double totalAmount,
                          String transactionDate,
                          Double unitPrice,
                          Integer vendorId,
                          String referenceNumber,
                          String accountHolderName,
                          String accountType,
                          String cashAccountType,
                          String bankName,
                          String categoryName,
                          String subCategoryName,
                          String title,
                          Boolean isVendorExpense,
                          String paymentStatus,
                          Double paidAmount,
                          Double balanceAmount,
                          String paymentMethod,
                          String note,
                          Double totalExpenseAmount,
                          Double totalExpensePaidAmount) {
}
