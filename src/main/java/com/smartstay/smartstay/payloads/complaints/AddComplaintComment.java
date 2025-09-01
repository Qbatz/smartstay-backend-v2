package com.smartstay.smartstay.payloads.complaints;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddComplaintComment(
        @NotBlank(message = "Message is required")
        String message

        ) {
}
