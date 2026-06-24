package com.smartstay.smartstay.responses.vendor;

/**
 * Minimal vendor representation returned by the lightweight vendor lookup API. Carries only the
 * fields needed by other modules to identify a vendor (id, contact-person name, business name).
 */
public record VendorLookupResponse(
        String vendorId,
        String vendorName,
        String businessName) {
}
