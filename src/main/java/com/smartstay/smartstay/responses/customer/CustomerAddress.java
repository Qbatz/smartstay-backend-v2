package com.smartstay.smartstay.responses.customer;

public record CustomerAddress(String streetName,
                              String houseNo,
                              String landmark,
                              Integer pincode,
                              String city,
                              String state) {
}
