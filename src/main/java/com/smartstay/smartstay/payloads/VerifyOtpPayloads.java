package com.smartstay.smartstay.payloads;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record VerifyOtpPayloads(@NotEmpty @NotNull String userId, @NotNull @NotEmpty Integer otp) {
}
