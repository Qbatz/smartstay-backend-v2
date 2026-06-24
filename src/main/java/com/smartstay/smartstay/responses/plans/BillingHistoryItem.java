package com.smartstay.smartstay.responses.plans;

public record BillingHistoryItem(
        Long historyId,
        String subscriptionNumber,
        String planName,
        String planCode,
        Double planAmount,
        Double discountAmount,
        Double totalAmount,
        String orderStatus,
        String paymentType,
        String paymentMethod,
        String paidById,
        String paidByName,
        String createdAt
) {
}
