package com.smartstay.smartstay.responses.customer;

public record CustomerData(String firstName,
                           String city,
                           String state,
                           String country,
                           String mobile,
                           String currentStatus,
                           String emailId,
                           String profilePic,
                           String bedId,
                           String floorId,
                           String roomId,
                           String customerId,
                           String initials,
                           String expectedJoiningDate,
                           String actualJoining,
                           String bookedAt) {
}
