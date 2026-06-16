package com.smartstay.smartstay.payloads.expense;

public record ExpenseItemPayload(
        String item,
        Integer quantity,
        Integer unitId,
        String unit,
        Double unitPrice,
        Double totalAmount) {
}
