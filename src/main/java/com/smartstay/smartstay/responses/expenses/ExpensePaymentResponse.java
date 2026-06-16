package com.smartstay.smartstay.responses.expenses;

public record ExpensePaymentResponse(
        Long id,
        Double paidAmount,
        String paymentMethod,
        String bankId,
        String paymentDate,
        String transactionId,
        String notes,
        String imageUrl) {
}
