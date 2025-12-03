package com.smartstay.smartstay.responses.recurring;

public record CustomersList(String customerId,
                            String firstName,
                            String lastName,
                            String fullName,
                            String initials,
                            String profilePic,
                            boolean currentStatus) {
}
