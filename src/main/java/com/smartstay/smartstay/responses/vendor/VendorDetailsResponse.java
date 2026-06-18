package com.smartstay.smartstay.responses.vendor;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Vendor details response. The existing {@link VendorResponse} fields are flattened to the top level
 * via {@link JsonUnwrapped} so the contract is fully preserved, with the new {@code filterOptions}
 * and {@code summary} blocks appended.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VendorDetailsResponse {

    @JsonUnwrapped
    private VendorResponse vendor;

    private String createdAt;

    private VendorDetailsFilterOptions filterOptions;

    private VendorFinancialSummary summary;
}
