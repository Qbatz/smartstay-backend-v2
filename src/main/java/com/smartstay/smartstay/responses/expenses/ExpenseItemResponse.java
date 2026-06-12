package com.smartstay.smartstay.responses.expenses;

public record ExpenseItemResponse(
        Long id,
        String item,
        Integer quantity,
        Integer unitId,
        String unit,
        Double unitPrice,
        Double totalAmount) {
}
