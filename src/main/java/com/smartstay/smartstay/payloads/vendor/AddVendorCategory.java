package com.smartstay.smartstay.payloads.vendor;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record AddVendorCategory(
        @NotNull(message = "Category name is required") @NotEmpty(message = "Category name is required") String categoryName,

        @NotNull(message = "HostelId is required") @NotEmpty(message = "HostelId is required") String hostelId) {
}
