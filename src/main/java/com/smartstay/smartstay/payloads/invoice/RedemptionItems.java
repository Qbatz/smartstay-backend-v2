package com.smartstay.smartstay.payloads.invoice;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record RedemptionItems(String invoiceId, Double amount) {
}
