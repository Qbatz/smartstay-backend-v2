package com.smartstay.smartstay.payloads;

import jakarta.persistence.Lob;

public record UpdateUserProfilePayloads(String firstName, String lastName, String emailId, String mobile, String houseNo, String street, String landmark, String city, String state, int pincode) {

}
