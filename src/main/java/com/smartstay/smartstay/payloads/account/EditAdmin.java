package com.smartstay.smartstay.payloads.account;

public record EditAdmin(String firstName,
                        String lastName,
                        String mobile,
                        String mailId,
                        String houseNo,
                        String street,
                        String landmark,
                        String city,
                        Integer pincode,
                        String state) {
}
