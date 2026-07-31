package com.smartstay.smartstay.responses.banking;

import java.util.List;

public record TransferInitializeResponse(
        PaymentMethodOptionResponse fromBank,
        List<PaymentMethodOptionResponse> toBanks
) {
}
