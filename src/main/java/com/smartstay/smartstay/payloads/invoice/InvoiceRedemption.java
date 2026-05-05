package com.smartstay.smartstay.payloads.invoice;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record InvoiceRedemption(String reason,
                                String date,
                                Double amount,
                                @NotNull(message = "target invoice id cannot be null")
                                @NotEmpty(message = "target invoice id cannot be null")
                                String targetInvoiceId) {

}
