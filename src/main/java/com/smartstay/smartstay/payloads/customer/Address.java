package com.smartstay.smartstay.payloads.customer;

public record Address(
    String flat,
    String house,
    String building,
    String company,
    String apartment,
    String area,
    String street,
    String sector,
    String village,
    String landmark,
    String pincode,
    String city,
    String state
) {
}
