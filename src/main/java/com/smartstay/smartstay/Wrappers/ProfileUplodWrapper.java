package com.smartstay.smartstay.Wrappers;

import com.smartstay.smartstay.dao.Address;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.payloads.UpdateUserProfilePayloads;

import java.util.Date;
import java.util.Calendar;
import java.util.function.Function;

public class ProfileUplodWrapper implements Function<UpdateUserProfilePayloads, Users> {

    private Users users;
    public ProfileUplodWrapper(Users user) {
        this.users = user;
    }
    @Override
    public Users apply(UpdateUserProfilePayloads updateUserProfilePayloads) {
        Address address = users.getAddress();
        address.setCity(updateUserProfilePayloads.city());
        address.setPincode(updateUserProfilePayloads.pincode());
        address.setState(updateUserProfilePayloads.state());
        address.setStreet(updateUserProfilePayloads.street());
        address.setHouseNo(updateUserProfilePayloads.houseNo());
        address.setLandMark(updateUserProfilePayloads.landmark());
        address.setUser(users);

        if (updateUserProfilePayloads.firstName() != null && !updateUserProfilePayloads.firstName().equalsIgnoreCase("")) {
            users.setFirstName(updateUserProfilePayloads.firstName());
        }

        if (updateUserProfilePayloads.lastName() != null && !updateUserProfilePayloads.lastName().equalsIgnoreCase("")) {
            users.setLastName(updateUserProfilePayloads.lastName());
        }

        if (updateUserProfilePayloads.emailId() != null && !updateUserProfilePayloads.emailId().equalsIgnoreCase("")) {
            users.setEmailId(updateUserProfilePayloads.emailId());
        }

        if (updateUserProfilePayloads.mobile() != null && !updateUserProfilePayloads.mobile().equalsIgnoreCase("")) {
            users.setMobileNo(updateUserProfilePayloads.mobile());
        }
        users.setLastUpdate(Calendar.getInstance().getTime());

        return users;
    }
}
