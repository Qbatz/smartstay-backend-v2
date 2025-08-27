package com.smartstay.smartstay.payloads.customer;

public record UpdateCustomerInfo(
        String firstName,
        String lastName,
        String mailId,
        String mobile,
        String houseNo,
        String street,
        String landmark,
        Integer pincode,
        String city,
        String state
) {
}
