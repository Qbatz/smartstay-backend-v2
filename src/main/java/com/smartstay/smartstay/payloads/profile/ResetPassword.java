package com.smartstay.smartstay.payloads.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPassword(
        @NotBlank(message = "Current password is required") String currentPassword,

        @NotBlank(message = "New password is required") @Size(min = 8, max = 100, message = "Password must be at least 8 characters") String newPassword,

        @NotBlank(message = "Confirm password is required") String confirmPassword) {
}
