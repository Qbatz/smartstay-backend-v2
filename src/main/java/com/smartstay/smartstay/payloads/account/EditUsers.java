package com.smartstay.smartstay.payloads.account;

public record EditUsers(String name,
                        String emailId,
                        String mobile,
                        Integer role,
                        String description) {
}
