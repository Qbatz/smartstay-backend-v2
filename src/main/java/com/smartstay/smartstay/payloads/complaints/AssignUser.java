package com.smartstay.smartstay.payloads.complaints;

import jakarta.validation.constraints.NotBlank;

public record AssignUser(
        @NotBlank
        String userId
) {
}
