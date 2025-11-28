package com.smartstay.smartstay.Wrappers.Electricity;

import com.smartstay.smartstay.dto.electricity.ElectricityReadingForRoom;
import com.smartstay.smartstay.responses.electricity.RoomElectricityList;
import com.smartstay.smartstay.util.Utils;

import java.util.Date;
import java.util.function.Function;

public class ElectricityRoomMapper implements Function<ElectricityReadingForRoom, RoomElectricityList> {

    ElectricityReadingForRoom ebForRoom = null;

    public ElectricityRoomMapper(ElectricityReadingForRoom ebForRoom) {
        this.ebForRoom = ebForRoom;
    }

    @Override
    public RoomElectricityList apply(ElectricityReadingForRoom electricityReadingForRoom) {
        String startDate = null;
        String endDate = null;

        if (ebForRoom != null) {
            Date dateStartDate = Utils.addDaysToDate(ebForRoom.getEntryDate(), 1);
            startDate = Utils.dateToString(dateStartDate);
        }
        else {
            startDate = Utils.dateToString(electricityReadingForRoom.getStartDate());
        }

        endDate = Utils.dateToString(electricityReadingForRoom.getEntryDate());
        return new RoomElectricityList(electricityReadingForRoom.getId(),
                electricityReadingForRoom.getUnitPrice(),
                electricityReadingForRoom.getHostelId(),
                electricityReadingForRoom.getRoomId(),
                startDate,
                endDate,
                electricityReadingForRoom.getConsumption(),
                electricityReadingForRoom.getCurrentReading(),
                endDate);
    }
}
