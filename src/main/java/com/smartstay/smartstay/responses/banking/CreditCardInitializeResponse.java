package com.smartstay.smartstay.responses.banking;

import java.util.List;

public record CreditCardInitializeResponse(
        List<PaymentMethodOptionResponse> creditCards,
        List<PaymentMethodOptionResponse> otherPaymentMethods) {
}
