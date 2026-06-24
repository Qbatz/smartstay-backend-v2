package com.smartstay.smartstay.dto.vendor;

/**
 * Lightweight projection for the vendor lookup API. Only the columns needed to identify a vendor are
 * selected (no full {@code VendorV1} entity is loaded), keeping the payload and query minimal for
 * dropdowns and cross-module lookups.
 */
public interface VendorLookupProjection {
    Integer getVendorId();
    String getFirstName();
    String getLastName();
    String getBusinessName();
}
