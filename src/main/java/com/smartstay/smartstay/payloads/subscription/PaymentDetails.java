package com.smartstay.smartstay.payloads.subscription;

public record PaymentDetails(String paymentLink, String paymentLinkId, String paymentType,
                             String channel,
                             String id) {
}
