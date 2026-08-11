package com.smartstay.smartstay.dto.customer;

public record AddressInfo(String houseNo,
                          String landmark,
                          String city,
                          String state,
                          String street,
                          Integer pincode) {
}
