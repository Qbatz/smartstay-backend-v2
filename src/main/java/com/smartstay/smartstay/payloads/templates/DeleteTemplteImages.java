package com.smartstay.smartstay.payloads.templates;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record DeleteTemplteImages(
        @NotNull(message = "Type is required")
        @NotEmpty(message = "Type is required")
        @Pattern(regexp = "LOGO|logo|Logo|Signature|SIGNATURE|signature", message = "source must be either 'signature' or 'SIGNATURE' or 'LOGO' or 'logo'")
        String type) {
}
