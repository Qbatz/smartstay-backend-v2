package com.smartstay.smartstay.payloads.expense;

public record ExpenseCategory(String categoryName,
                              Long categoryId,
                              String subCategory) {
}
