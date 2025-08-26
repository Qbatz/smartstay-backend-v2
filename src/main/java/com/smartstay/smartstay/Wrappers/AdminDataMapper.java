package com.smartstay.smartstay.Wrappers;

import com.smartstay.smartstay.dto.Admin.UsersData;

import java.util.function.Function;

public class AdminDataMapper implements Function<UsersData, com.smartstay.smartstay.responses.user.UsersData> {
    @Override
    public com.smartstay.smartstay.responses.user.UsersData apply(UsersData usersData) {

        StringBuilder initials = new StringBuilder();
        initials.append(usersData.firstName().toUpperCase().charAt(0));
        if (usersData.lastName() != null && !usersData.lastName().equalsIgnoreCase("")) {
            initials.append(usersData.lastName().toUpperCase().charAt(0));
        }
        else {
            initials.append(usersData.firstName().toUpperCase().charAt(1));
        }


        return new com.smartstay.smartstay.responses.user.UsersData(
                usersData.userId(),
                usersData.firstName(),
                usersData.lastName(),
                usersData.mobileNo(),
                usersData.mailId(),
                usersData.roleId(),
                usersData.roleName(),
                usersData.countryId(),
                usersData.email_authentication_status(),
                usersData.sms_authentication_status(),
                usersData.two_step_verification_status(),
                usersData.countryName(),
                usersData.currency(),
                usersData.countryCode(),
                usersData.pincode(),
                usersData.city(),
                usersData.houseNo(),
                usersData.landmark(),
                usersData.state(),
                usersData.street(),
                usersData.profilePic(),
                initials.toString(),
                usersData.firstName() + " " + usersData.lastName(),
                usersData.description()
        );
    }
}
