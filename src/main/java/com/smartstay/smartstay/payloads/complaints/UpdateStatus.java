package com.smartstay.smartstay.payloads.complaints;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UpdateStatus(
        @NotNull(message = "Status is required")
        @NotEmpty(message = "Status is required")
        @Pattern(regexp = "^(INPROGRESS|PENDING|RESOLVED|inprogress|pending|resolved)?$", message = "Status must be either 'in progress' or 'pending' or 'resolved'")
        String status
) {
}
