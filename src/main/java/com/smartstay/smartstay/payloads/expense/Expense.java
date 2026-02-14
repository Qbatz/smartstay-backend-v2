package com.smartstay.smartstay.payloads.expense;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record Expense(
        @NotNull(message = "Category id required")
        @Positive(message = "Category id required")
        Long categoryId,
        Long subCategory,
        @NotEmpty(message = "Purchase date is required")
        @NotNull(message = "Purchase date is required")
        String purchaseDate,
        @NotNull(message = "Count is required")
        @NotEmpty(message = "Count is required")
        @Positive(message = "Count is required")
        Integer count,
        @NotNull(message = "Total amount required")
        @Positive(message = "Total amount required")
        Double totalAmount,
        @NotNull(message = "Bank id required")
        @NotEmpty(message = "Bank id required")
        @NotBlank(message = "Bank id required")
        String bankId,
        String description) {
}
