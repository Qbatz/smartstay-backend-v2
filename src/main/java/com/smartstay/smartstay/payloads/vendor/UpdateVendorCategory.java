package com.smartstay.smartstay.payloads.vendor;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for renaming a vendor category. Only the category name is updateable; the target
 * category and its owning hostel are identified by the path/query parameters.
 */
public record UpdateVendorCategory(
        @NotNull(message = "Category name is required")
        @NotEmpty(message = "Category name is required")
        String categoryName) {
}
