package com.smartstay.smartstay.payloads.expense;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UpdateExpenseCategory(
        @NotEmpty(message = "Category name is required")
        @NotNull(message = "Category name is required")
        String newCategoryName) {
}
