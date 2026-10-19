package com.smartstay.smartstay.responses.banking;

import com.smartstay.smartstay.payloads.banking.CreditCardPayment;

import java.util.List;

public record CreditCardInitializeResponse(
        String hostelId,
        List<CreditCardInitialization> availableCreditCards,
        List<PaymentMethodOptionResponse> creditCards,
        List<PaymentMethodOptionResponse> otherPaymentMethods) {
}
