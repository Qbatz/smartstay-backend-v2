package com.smartstay.smartstay.payloads.expense;

import jakarta.validation.constraints.*;

import java.util.List;

public record Expense(
        @NotNull(message = "Category id required")
        @Positive(message = "Category id required")
        Long categoryId,
        Long subCategory,
        @NotEmpty(message = "Purchase date is required")
        @NotNull(message = "Purchase date is required")
        String purchaseDate,
        // Optional; defaults to 1 in the service when absent or non-positive.
        Integer count,
        @NotNull(message = "Total amount required")
        @Positive(message = "Total amount required")
        Double totalAmount,
        // Mandatory for every payment status except PENDING; enforced conditionally in the service layer.
        String bankId,
        String description,

        String title,
        Boolean isVendorExpense,
        Integer vendorId,
        String paymentStatus,
        Double paidAmount,
        Double balanceAmount,
        String paymentMethod,
        String note,
        String transactionId,
        Double tax,
        Double discount,
        // Optional discount percentage; used to derive the discount amount when `discount` is absent.
        Double discountPercentage,
        // Optional explicit credit period (days); when > 0 it overrides the vendor-level credit period.
        Integer creditPeriod,
        List<ExpenseItemPayload> expenseItems) {
}
