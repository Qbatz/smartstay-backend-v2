package com.smartstay.smartstay.responses.vendor;

public record VendorExpensePaymentResponse(
        Long id,
        Double paidAmount,
        String paymentMethod,
        String expenseId,
        String bankId,
        String bankName,
        String hostelId,
        String paymentDate,
        String transactionId,
        String notes,
        String imageUrl) {
}
