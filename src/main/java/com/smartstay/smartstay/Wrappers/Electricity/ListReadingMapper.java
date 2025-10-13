package com.smartstay.smartstay.Wrappers.Electricity;

import com.smartstay.smartstay.dto.electricity.ElectricityReadings;
import com.smartstay.smartstay.responses.electricity.ElectricityUsage;
import com.smartstay.smartstay.util.Utils;

import java.util.function.Function;

public class ListReadingMapper implements Function<ElectricityReadings, ElectricityUsage> {
    @Override
    public ElectricityUsage apply(ElectricityReadings electricityReaddings) {

        double totalPrice = 0.0;
        double previousReading = electricityReaddings.getCurrentReading() - electricityReaddings.getConsumption()  ;
        if (!electricityReaddings.getConsumption().isNaN()) {
            totalPrice = electricityReaddings.getConsumption() * electricityReaddings.getUnitPrice();
        }
        return new ElectricityUsage(electricityReaddings.getHostelId(),
                electricityReaddings.getId(),
                electricityReaddings.getConsumption(),
                electricityReaddings.getRoomId(),
                electricityReaddings.getFloorId(),
                electricityReaddings.getRoomName(),
                electricityReaddings.getFloorName(),
                Utils.dateToString(electricityReaddings.getEntryDate()),
                electricityReaddings.getUnitPrice(),
                previousReading,
                electricityReaddings.getCurrentReading(),
                totalPrice,
                electricityReaddings.getNoOfTenants());
    }
}
