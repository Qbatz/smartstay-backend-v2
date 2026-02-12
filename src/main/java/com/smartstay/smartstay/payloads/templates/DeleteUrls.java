package com.smartstay.smartstay.payloads.templates;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record DeleteUrls(
        @NotNull(message = "Type required")
        @NotEmpty(message = "Type required")
        @NotBlank(message = "Type is required")
        @Pattern(regexp = "qrcode|invoice-logo|receipt-logo|invoice-signature|receipt-signature|QRCODE|INVOICE-LOGO|RECEIPT-LOGO|INVOICE-SIGNATURE|RECEIPT-SIGNATURE",
                message = "Type must be either 'qrcode' or 'invoice-logo' or 'receipt-logo' or 'invoice-signature' or 'receipt-signature'")
        String type) {
}
