package com.smartstay.smartstay.responses.vendor;

/**
 * Flat, key-value vendor representation returned to mobile clients (as opposed to the dynamic
 * column-based rows used by the web listing). Mirrors {@link VendorResponse} plus the denormalized
 * financial summary stored on the vendor row.
 */
public record VendorMobileResponse(
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
        String contactPersonMobile,
        String description,
        String vendorCode,
        String gst,
        String pan,
        Boolean allowCredit,
        Double creditLimit,
        Integer creditPeriod,
        String createdAt,
        String paymentStatus,
        double totalExpenseAmount,
        double totalPaidAmount,
        double totalBalance,
        String businessMobileCode,
        String contactPersonMobileCode,
        String vendorAddress) {
}
