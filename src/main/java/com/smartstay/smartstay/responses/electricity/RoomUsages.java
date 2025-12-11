package com.smartstay.smartstay.responses.electricity;

import com.smartstay.smartstay.rooms.EBReadingRoomsInfo;

import java.util.List;

public record RoomUsages(EBReadingRoomsInfo roomInfo,
        String hostelId,
        List<RoomElectricityList> readings,
                         List<RoomElectricityCustomersList> customers) {
}
