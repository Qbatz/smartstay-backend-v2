package com.smartstay.smartstay.payloads.subscription;

public record PaymentStatus(String paymentLink, String paymentLinkId, String paymentType,
                            String channel,
                            String id) {
}
