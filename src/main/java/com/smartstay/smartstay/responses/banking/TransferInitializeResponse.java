package com.smartstay.smartstay.responses.banking;

import java.util.List;

public record TransferInitializeResponse(
        String hostelId,
        PaymentMethodOptionResponse fromBank,
        List<PaymentMethodOptionResponse> toBanks
) {
}
