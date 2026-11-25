package com.smartstay.smartstay.responses.hostel;

public record HostelInfoResponse(String hostelName,
                                 String fullAddress,
                                 String mobile,
                                 String city,
                                 String state,
                                 int pincode,
                                 String houseNo,
                                 String street) {
}
