package com.smartstay.smartstay.Wrappers;

import com.smartstay.smartstay.dao.VendorV1;
import com.smartstay.smartstay.responses.vendor.VendorResponse;

import java.util.function.Function;

public class VendorMapper implements Function<VendorV1, VendorResponse> {
    @Override
    public VendorResponse apply(VendorV1 vendorV1) {
        return new VendorResponse(vendorV1.getVendorId(), vendorV1.getFirstName() + vendorV1.getLastName(), vendorV1.getBusinessName(),
                vendorV1.getMobileNumber(), vendorV1.getEmailId(), vendorV1.getProfilePic()
                );
    }
}
