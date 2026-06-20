package com.smartstay.smartstay.responses.expenses;

import java.util.List;

public record ExpenseDetailPayment(
        Long id,
        Double paidAmount,
        String paymentMethod,
        String bankId,
        String bankName,
        String paymentDate,
        String transactionId,
        String notes,
        String imageUrl,
        List<String> imageUrls,
        String createdAt,
        String createdBy) {
}
