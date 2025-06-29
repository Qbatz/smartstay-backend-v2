package com.smartstay.smartstay.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZohoSubscription {

    @JsonProperty("code")
    int code;
    @JsonProperty("message")
    String message;
    @JsonProperty("subscription")
    Subscription subscription;
    @Data
    public static class Subscription {
        @JsonProperty("subscription_id")
        String subscriptionId;
        @JsonProperty("name")
        String planName;
        @JsonProperty("subscription_number")
        String subscriptionNumber;
        @JsonProperty("status")
        String status;
        @JsonProperty("sub_total")
        int subTotal;
        @JsonProperty("amount")
        int amount;
        @JsonProperty("created_at")
        String createdAt;
        @JsonProperty("activated_at")
        String activatedAt;
        @JsonProperty("trial_starts_at")
        String trialStartsAt;
        @JsonProperty("trial_ends_at")
        String trialEndsAt;
        @JsonProperty("trial_remaining_days")
        int trialRemainingDays;
        @JsonProperty("current_term_starts_at")
        String currentTermStartsAt;
        @JsonProperty("current_term_ends_at")
        String currentTermEndsAt;
        @JsonProperty("next_billing_at")
        String nextBillingAt;
        @JsonProperty("plan")
        Plan plan;
        @JsonProperty("taxes")
        List<Taxes> taxes;
    }

    @Data
    public static class Plan {
        @JsonProperty("plan_id")
        String planId;
        @JsonProperty("plan_code")
        String planName;
        @JsonProperty("price")
        int price;
        @JsonProperty("total")
        int total;
        @JsonProperty("discount")
        int discount;
        @JsonProperty("discount_amount")
        int discountAmount;
    }

    @Data
    public static class Taxes {
        @JsonProperty("tax_id")
        String taxId;
        @JsonProperty("tax_name")
        String taxName;
        @JsonProperty("tax_amount")
        int taxAmount;
    }
}
