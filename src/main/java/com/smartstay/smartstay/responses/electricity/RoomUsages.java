package com.smartstay.smartstay.responses.electricity;

import com.smartstay.smartstay.rooms.EBReadingRoomsInfo;

import java.util.List;

public record RoomUsages(EBReadingRoomsInfo roomInfo,
        List<RoomElectricityList> readings) {
}
