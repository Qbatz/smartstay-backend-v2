package com.smartstay.smartstay.responses.dashboard;

import java.util.List;

public record RoomsAndBedInfo(Integer totalRooms,
                              Integer filledRooms,
                              Integer totalBeds,
                              List<SharingInfo> sharingInfo) {
}
