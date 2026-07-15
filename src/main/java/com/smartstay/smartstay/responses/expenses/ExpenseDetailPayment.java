package com.smartstay.smartstay.responses.expenses;

import java.util.List;

public record ExpenseDetailPayment(
        Long id,
        Double paidAmount,
        String paymentMethod,
        String bankId,
        String bankName,
        // bankingv1.account_type of the payment's bank; null when the payment has no bank.
        String paymentMode,
        String paymentDate,
        String transactionId,
        String notes,
        String imageUrl,
        List<String> imageUrls,
        String createdAt,
        String createdBy) {
}
