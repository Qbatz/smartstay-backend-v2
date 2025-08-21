package com.smartstay.smartstay.Wrappers;

import com.smartstay.smartstay.dao.Address;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.payloads.account.AddAdminPayload;

import java.util.Date;
import java.util.function.Function;

public class AddAdminUsersMapper implements Function<AddAdminPayload, Users> {
    private Users users;

    public AddAdminUsersMapper(Users users) {
        this.users = users;
    }

    @Override
    public Users apply(AddAdminPayload addAdminPayload) {
        users.setRoleId(2);
        users.setFirstName(addAdminPayload.firstName());
        users.setLastName(addAdminPayload.lastName());
        users.setMobileNo(addAdminPayload.mobile());
        users.setEmailId(addAdminPayload.mailId());
        users.setCountry(1L);
        users.setTwoStepVerificationStatus(false);
        users.setEmailAuthenticationStatus(false);
        users.setSmsAuthenticationStatus(false);
        users.setActive(true);
        users.setDeleted(false);
        users.setCreatedAt(new Date());
        users.setLastUpdate(new Date());

        Address address = new Address();
        address.setCity(addAdminPayload.city());
        address.setState(addAdminPayload.state());
        address.setStreet(addAdminPayload.street());
        address.setHouseNo(addAdminPayload.houseNo());
        address.setPincode(addAdminPayload.pincode());
        address.setLandMark(addAdminPayload.landmark());


        address.setUser(users);
        users.setAddress(address);

        return users;
    }
}
