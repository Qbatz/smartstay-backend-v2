package com.smartstay.smartstay.payloads.hostel;

public record UpdatePg(String houseNo,
                       String street,
                       String landmark,
                       Integer pincode,
                       String city,
                       String state,
                       String hostelName,
                       String mobile) {
}
