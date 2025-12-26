package com.smartstay.smartstay.Wrappers.Electricity;

import com.smartstay.smartstay.dao.ElectricityReadings;
import com.smartstay.smartstay.dto.electricity.ElectricityReadingForRoom;
import com.smartstay.smartstay.responses.electricity.RoomElectricityList;
import com.smartstay.smartstay.util.Utils;

import java.util.Date;
import java.util.function.Function;

public class ElectricityRoomMapper implements Function<ElectricityReadings, RoomElectricityList> {

    ElectricityReadings ebForRoom = null;

    public ElectricityRoomMapper(ElectricityReadings ebForRoom) {
        this.ebForRoom = ebForRoom;
    }

    @Override
    public RoomElectricityList apply(ElectricityReadings electricityReadingForRoom) {
        String startDate = null;
        String endDate = null;
        Double amount = 0.0;

        if (ebForRoom != null) {
            Date dateStartDate = Utils.addDaysToDate(ebForRoom.getEntryDate(), 1);
            startDate = Utils.dateToString(dateStartDate);
        }
        else {
            startDate = Utils.dateToString(electricityReadingForRoom.getBillStartDate());
        }

        endDate = Utils.dateToString(electricityReadingForRoom.getEntryDate());

        if (electricityReadingForRoom.getConsumption() != null) {
            amount = Utils.roundOffWithTwoDigit(electricityReadingForRoom.getConsumption() * electricityReadingForRoom.getCurrentUnitPrice());
        }

        if (electricityReadingForRoom.isFirstEntry()) {
            amount = 0.0;
        }

        return new RoomElectricityList(electricityReadingForRoom.getId(),
                electricityReadingForRoom.getCurrentUnitPrice(),
                electricityReadingForRoom.getHostelId(),
                electricityReadingForRoom.getRoomId(),
                startDate,
                endDate,
                electricityReadingForRoom.getConsumption(),
                electricityReadingForRoom.getCurrentReading(),
                endDate,
                amount);
    }
}
