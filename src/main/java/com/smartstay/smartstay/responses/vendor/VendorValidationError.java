package com.smartstay.smartstay.responses.vendor;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Structured validation response for the Add Vendor API. Carries every field-level uniqueness
 * failure at once so the caller can fix them in a single attempt. Only the fields that actually
 * failed are serialized (null entries are omitted).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record VendorValidationError(
        String emailError,
        String mobileError) {
}
