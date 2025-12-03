package com.smartstay.smartstay.responses.beds;

public record BedDetails(String bedName,
                         int bedId,
                         String hostelId,
                         boolean isBooked,
                         boolean isOnNotice,
                         boolean isOccupied,
                         Double rentAmount,
                         int roomId,
                         String freeFrom,
                         Integer floorId,
                         String floorName,
                         String roomName,
                         TenantInfo currentTenantInfo,
                         TenantInfo newTenantInfo) {
}
