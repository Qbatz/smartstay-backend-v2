package com.smartstay.smartstay.payloads.complaints;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UpdateStatus(
        @NotNull(message = "Status is required")
        @NotEmpty(message = "Status is required")
        @Pattern(regexp = "^(ASSIGNED|PENDING|RESOLVED|assigned|pending|resolved)?$", message = "Status must be either 'assigned' or 'pending' or 'resolved'")
        String status
) {
}
