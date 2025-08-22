package com.smartstay.smartstay.payloads.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public record ResetPasswordRequest(
        @NotBlank(message = "Password is required and cannot be blank")
        @Size(min = 8, max = 100, message = "Password must be at least 8 characters and less than 100")
        String password,
        @NotNull(message = "User id required")
        @NotEmpty(message = "User id required")
        String userId,

        @NotNull
        Integer otp

) {
}
