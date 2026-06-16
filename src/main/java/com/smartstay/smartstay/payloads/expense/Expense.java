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
        @NotNull(message = "Count is required")
        @Positive(message = "Count is required")
        @Min(value = 1, message = "Count is required")
        Integer count,
        @NotNull(message = "Total amount required")
        @Positive(message = "Total amount required")
        Double totalAmount,
        @NotNull(message = "Bank id required")
        @NotBlank(message = "Bank id required")
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
        List<ExpenseItemPayload> expenseItems) {
}
