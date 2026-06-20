package com.smartstay.smartstay.responses.expenses;

public record ExpenseDetailItem(
        Long id,
        String item,
        Integer quantity,
        String unit,
        Double unitPrice,
        Double totalAmount,
        String createdAt,
        String createdBy) {
}
