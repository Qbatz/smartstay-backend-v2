package com.smartstay.smartstay.payloads.subscription;

public record PaymentStatusCardType(String paymentLink, String paymentLinkId, String paymentType,
                                    String cardType,
                                    String lastFourDigits,
                                    String issuer,
                                    String cardHolderName) {
}
