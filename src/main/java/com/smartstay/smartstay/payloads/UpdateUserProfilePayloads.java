package com.smartstay.smartstay.payloads;

public record UpdateUserProfilePayloads(String firstName, String lastName, String emailId, String mobile, String houseNo, String street, String landmark, String city, String state, int pincode) {

}
