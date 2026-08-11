package com.smartstay.smartstay.payloads.retainer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record LoadBalance(String relationId,
                          String paymentDate,
                          String mobile,
                          String relationName,
                          String description,
                          String detailedDescription,
                          @NotNull(message = "Invoice type required")
                          @NotEmpty(message = "Invoice type required")
                          @NotBlank(message = "Invoice type is required")
                          @Pattern(regexp = "AMOUNT_HOLDING|amount_holding|EB_HOLDING|eb_holding", message = "Type must be either 'amount_holding' or 'eb_holding'")
                          String invoiceType,
                          Double amount,
                          String bankId,
                          String referenceNumber) {
}
