package com.smartstay.smartstay.payloads;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record VerifyOtpPayloads(
        @NotBlank
        String userId,
        @NotNull
        @NotEmpty
        Integer otp) {
}
