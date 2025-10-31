package com.smartstay.smartstay.responses.subscriptions;

public record Subscription(Long id,
                           String currentPlanStartedAt,
                           String currentPlanEndingAt,
                           String subscriptionNumber,
                           String planName,
                           String planCode) {
}
