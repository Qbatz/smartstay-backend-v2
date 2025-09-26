package com.smartstay.smartstay.Wrappers.Electricity;

import com.smartstay.smartstay.dao.ElectricityReadings;
import com.smartstay.smartstay.responses.electricity.ElectricityUsage;
import com.smartstay.smartstay.responses.rooms.RoomInfoForEB;
import com.smartstay.smartstay.util.Utils;

import java.util.function.Function;

public class ElectricityHostelBasedMapper implements Function<RoomInfoForEB, ElectricityUsage> {
    private int size = 1;
    private String hostelId;
    private ElectricityReadings electricityReadings;

    public ElectricityHostelBasedMapper(int size, String hostelId, ElectricityReadings readings) {
        this.size = size;
        this.hostelId = hostelId;
        this.electricityReadings = readings;
    }

    @Override
    public ElectricityUsage apply(RoomInfoForEB roomInfoForEB) {
        double previousReading = 0;
        double consumption = 0;
        double currentReading = 0;
        double totalPrice = 0;
        if (electricityReadings != null) {
            if (electricityReadings.getPreviousReading() != null) {
                previousReading = electricityReadings.getPreviousReading() / size;
            }
            if (electricityReadings.getConsumption() != null) {
                consumption = electricityReadings.getConsumption() / size;
            }
            if (electricityReadings.getCurrentReading() != null) {
                currentReading = electricityReadings.getCurrentReading() / size;
            }
            if (electricityReadings.getCurrentUnitPrice() != null) {
                totalPrice = consumption * electricityReadings.getCurrentUnitPrice();
            }
        }

        return new ElectricityUsage(hostelId,
                electricityReadings.getId(),
                consumption,
                roomInfoForEB.roomId(),
                roomInfoForEB.floorId(),
                roomInfoForEB.roomName(),
                roomInfoForEB.floorName(),
                Utils.dateToString(electricityReadings.getEntryDate()),
                electricityReadings.getCurrentUnitPrice(),
                previousReading,
                currentReading,
                totalPrice,
                roomInfoForEB.noOfTenants().intValue());
    }
}
