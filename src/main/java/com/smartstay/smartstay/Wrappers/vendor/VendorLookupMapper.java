package com.smartstay.smartstay.Wrappers.vendor;

import com.smartstay.smartstay.dto.vendor.VendorLookupProjection;
import com.smartstay.smartstay.responses.vendor.VendorLookupResponse;
import com.smartstay.smartstay.util.NameUtils;

import java.util.function.Function;

/**
 * Maps a lightweight {@link VendorLookupProjection} to the {@link VendorLookupResponse} returned by
 * the vendor lookup API. The contact-person name is composed via {@link NameUtils#getFullName} so it
 * matches the full-name formatting used elsewhere in the project.
 */
public class VendorLookupMapper implements Function<VendorLookupProjection, VendorLookupResponse> {

    @Override
    public VendorLookupResponse apply(VendorLookupProjection vendor) {
        return new VendorLookupResponse(
                String.valueOf(vendor.getVendorId()),
                NameUtils.getFullName(vendor.getFirstName(), vendor.getLastName()),
                vendor.getBusinessName());
    }
}
