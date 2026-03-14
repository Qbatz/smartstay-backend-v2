package com.smartstay.smartstay.responses.dashboard;

public record SharingInfo(String shareType,
                          Integer totalRooms,
                          Integer availableRooms,
                          Integer totalBeds,
                          Integer occupiedBeds,
                          Integer availableBeds,
                          Double occupancyRatio) {
}
