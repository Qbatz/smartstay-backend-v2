package com.smartstay.smartstay.payloads.customer;

public record Guardian(
    String guardianFullName,
    String relationshipToTenant,
    String guardianOccupation,
    String mobileNo
) {
}
