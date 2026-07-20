package com.smartstay.smartstay.payloads.retainer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record LoadBalance(String relationId,
                          String paymentDate,
                          String mobile,
                          String relationName,
                          @NotNull(message = "Invoice type required")
                          @NotEmpty(message = "Invoice type required")
                          @NotBlank(message = "Invoice type is required")
                          @Pattern(regexp = "ADVANCE_HOLDING|advance_holding|EB_HOLDING|eb_holding", message = "Type must be either 'advance_holding' or 'eb_holding'")
                          String invoiceType,
                          Double amount,
                          String bankId,
                          String referenceNumber) {
}
