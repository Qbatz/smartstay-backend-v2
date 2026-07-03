package com.smartstay.smartstay.responses.customer;

public record KycInformations(String status,
                              String aadhaarImage,
                              String aadhaarNumber,
                              String nameInAadhaar,
                              String dateOfBirth,
                              String verifiedAt,
                              String aadhaarDocument,
                              String documentType,
                              KycAddressDetails currentAddress,
                              KycAddressDetails permanentAddress) {
}
