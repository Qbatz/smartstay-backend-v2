package com.smartstay.smartstay.dto.electricity;

public record HostelInfo(String hostelName,
                         String hostelImage,
                         String initials,
                         long noOfOccupants,
                         String billingMonth,
                         Double previousEntry,
                         Double consumption,
                         Double totalAmount) {
}
