package com.smartstay.smartstay.payloads.vendor;

public record UpdateVendor(
        String firstName,

        String lastName,

        String mobile,

        String mailId, String houseNo, String landmark, String area,

        Integer pinCode,
        Integer vendorId,

        String city,

        String state,
        Long country,

        String businessName
) {
}
