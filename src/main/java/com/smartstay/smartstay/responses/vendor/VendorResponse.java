package com.smartstay.smartstay.responses.vendor;

public record VendorResponse(
        int id,
        String firstName,
        String lastName,
        String fullName,
        String businessName,
        String mobile,
        String emailId,
        String profilePic,
        String houseNo,
        String area,
        String landMark,
        String city,
        int pinCode,
        String state,
        String countryCode,
        String country,
        Long countryId,
        Integer vendorCategoryId,
        String vendorCategoryName,
        String contactPerson,
        String description,
        String vendorCode,
        String gst,
        String pan,
        Boolean allowCredit,
        Double creditLimit,
        Integer creditPeriod
) {
}
