package com.smartstay.smartstay.responses.settlement;

public record HeaderInfo(String invoiceNo,
                         String houseNo,
                         String street,
                         String city,
                         String state,
                         Integer pincode,
                         String gstNumber,
                         String phoneNumber,
                         String countryCode,
                         String hostelImage,
                         String emailId) {
}
