package com.smartstay.smartstay.payloads.expense;

public record SettleExpensePayment(
        String paymentDate,
        String bankId,
        String paymentMethodId,
        String paymentMethod,
        String transactionId,
        String notes,
        Double paidAmount) {
}
