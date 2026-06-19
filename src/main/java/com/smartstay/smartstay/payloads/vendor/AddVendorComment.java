package com.smartstay.smartstay.payloads.vendor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AddVendorComment(
        @NotNull(message = "Vendor id is required")
        @Positive(message = "Vendor id is required")
        Integer vendorId,

        @NotNull(message = "Comment is required")
        @NotBlank(message = "Comment is required")
        @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
        String comment) {
}
