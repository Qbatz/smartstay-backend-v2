package com.smartstay.smartstay.responses.subscriptions;

public record PaymentSessionResponse(String sessionId,
                                     String amount,
                                     String apiKey,
                                     String accountId,
                                     String environment) {
}
