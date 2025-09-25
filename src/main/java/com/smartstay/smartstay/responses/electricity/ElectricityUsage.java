package com.smartstay.smartstay.responses.electricity;

public record ElectricityUsage(String hostelId,
                               Integer readingId,
                               Double consumption,
                               Integer roomId,
                               Integer floorId,
                               String roomName,
                               String floorName,
                               String entryDate,
                               Double unitPrice,
                               Double previousReading,
                               Double currentReading,
                               Double totalPrice,
                               Integer noOfTenants) {
}
