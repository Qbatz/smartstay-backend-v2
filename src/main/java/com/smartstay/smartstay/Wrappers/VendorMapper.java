package com.smartstay.smartstay.Wrappers;

import com.smartstay.smartstay.dao.VendorV1;
import com.smartstay.smartstay.responses.vendor.VendorResponse;

import java.util.function.Function;

public class VendorMapper implements Function<VendorV1, VendorResponse> {
    @Override
    public VendorResponse apply(VendorV1 vendorV1) {
        return new VendorResponse(
                vendorV1.getVendorId(),
                vendorV1.getFirstName(),
                vendorV1.getLastName(),
                vendorV1.getFirstName() + " " + vendorV1.getLastName(),
                vendorV1.getBusinessName(),
                vendorV1.getMobile(),
                vendorV1.getEmailId(),
                vendorV1.getProfilePic(),
                vendorV1.getHouseNo(),
                vendorV1.getArea(),
                vendorV1.getLandMark(),
                vendorV1.getCity(),
                vendorV1.getPinCode(),
                vendorV1.getState(),
                vendorV1.getCountryCode(),
                "",
                1L,
                vendorV1.getVendorCategory(),
                null,
                vendorV1.getContactPerson(),
                vendorV1.getDescription(),
                vendorV1.getVendorCode(),
                vendorV1.getGst(),
                vendorV1.getPan(),
                vendorV1.getAllowCredit(),
                vendorV1.getCreditLimit(),
                vendorV1.getCreditPeriod()
        );
    }
}
