package com.smartstay.smartstay.responses.vendor;

public record VendorResponse(
        int id, String vendorName,String businessName,String mobileNumber, String emailId, String profilePic
) {
}
