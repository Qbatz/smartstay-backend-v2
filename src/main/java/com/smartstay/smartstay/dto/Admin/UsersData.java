package com.smartstay.smartstay.dto.Admin;

public record UsersData(String userId,
                             String firstName,
                             String lastName,
                             String userName,
                             String mobileNo,
                             String mailId, Integer roleId,
                             String roleName,
                             Long countryId, Boolean email_authentication_status, Boolean sms_authentication_status,
                             Boolean two_step_verification_status,
                             String countryName,
                             String currency, String countryCode, Integer pincode, String city, String houseNo,
                             String landmark, String state, String street, String profilePic,
                        String description) {
}
