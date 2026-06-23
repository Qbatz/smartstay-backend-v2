package com.smartstay.smartstay.payloads.vendor;

public record UpdateVendor(
        String firstName,

        String lastName,

        String countryCode,

        String mobile,

        String mailId, String houseNo, String landmark, String area,

        Integer pinCode,
        Integer vendorId,

        String city,

        String state,
        Long country,

        String businessName,

        Integer vendorCategory,
        String contactPerson,
        String contactPersonMobile,
        String businessMobileCode,
        String contactPersonMobileCode,
        String description,
        String vendorCode,
        String gst,
        String pan,
        Boolean allowCredit,
        Double creditLimit,
        Integer creditPeriod
) {
}
