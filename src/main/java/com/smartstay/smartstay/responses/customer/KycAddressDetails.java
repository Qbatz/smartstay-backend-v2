package com.smartstay.smartstay.responses.customer;

public record KycAddressDetails(String houseNo,
                                String streetName,
                                String city,
                                String state,
                                String pinCode,
                                String fullAddress) {
}
