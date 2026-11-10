package com.smartstay.smartstay.responses.expenses;

import com.smartstay.smartstay.ennum.ExpensePaymentStatus;

public record ExpenseItemResponse(
        Long id,
        String item,
        Integer quantity,
        Integer unitId,
        String unit,
        Double unitPrice,
        Double totalAmount,
        String paymentStatus,
        Double paidAmount) {
}
