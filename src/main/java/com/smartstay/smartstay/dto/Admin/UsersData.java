package com.smartstay.smartstay.dto.Admin;

public record UsersData(String userId,
                             String firstName,
                             String lastName,
                             String mobileNo,
                             String mailId,
                             int roleId,
                             String roleName,
                             Long countryId,
                             boolean email_authentication_status,
                             boolean sms_authentication_status,
                             boolean two_step_verification_status,
                             String countryName,
                             String currency, String countryCode,
                             int pincode, String city, String houseNo,
                             String landmark, String state, String street, String profilePic) {
}
