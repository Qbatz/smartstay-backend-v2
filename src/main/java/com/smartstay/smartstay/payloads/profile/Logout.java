package com.smartstay.smartstay.payloads.profile;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record Logout(
        @NotEmpty(message = "Source is required")
                @NotNull(message = "Source is required")
        @Pattern(regexp = "WEB|web|Web|Mobile|MOBILE|mobile", message = "source must be either 'Mobile' or 'MOBILE' or 'Web' or 'WEB'")
        String source) {
}
