package com.smartstay.smartstay.responses.dashboard;

import java.util.List;

public record RoomsAndBedInfo(Integer totalRooms,
                              Integer filledRooms,
                              Integer availableRooms,
                              Integer totalBeds,
                              Integer occupiedBeds,
                              Integer availableBeds,
                              List<SharingInfo> sharingInfo) {
}
