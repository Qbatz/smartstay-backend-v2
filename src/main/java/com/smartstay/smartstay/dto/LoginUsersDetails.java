package com.smartstay.smartstay.dto;

public record LoginUsersDetails(String userId, String firstName,
                                String lastName, String mobileNo, String mailId,
                                int roleId, String roleName, Long countryId, boolean email_authentication_status,
                                boolean sms_authentication_status, boolean two_step_verification_status,
                                String countryName, String currency, String countryCode) {
}
