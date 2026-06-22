package com.smartstay.smartstay.responses.expenses;

import com.smartstay.smartstay.ennum.ExpensePaymentStatus;

import java.util.List;

public record ExpenseDetailResponse(
        String expenseId,
        Integer itemsCount,
        Long categoryId,
        long subCategoryId,
        String description,
        String hostelId,
        String bankId,
        String bankName,
        Double actualTotalPrice,
        Double totalAmount,
        String transactionDate,
        Double unitPrice,
        String vendorId,
        String vendorAddress,
        String referenceNumber,
        String accountHolderName,
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
        Integer creditPeriod,
        Double discount,
        Double tax,
        String transactionId,
        String createdAt,
        String createdBy,
        List<String> images,
        List<ExpenseDetailItem> expenseItems,
        List<ExpenseDetailPayment> expensePayments) {
}
