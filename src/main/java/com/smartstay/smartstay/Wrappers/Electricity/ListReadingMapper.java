package com.smartstay.smartstay.Wrappers.Electricity;

import com.smartstay.smartstay.dto.electricity.ElectricityReadings;
import com.smartstay.smartstay.responses.electricity.ElectricityUsage;
import com.smartstay.smartstay.util.Utils;

import java.util.function.Function;

public class ListReadingMapper implements Function<ElectricityReadings, ElectricityUsage> {
    @Override
    public ElectricityUsage apply(ElectricityReadings electricityReaddings) {

        double totalPrice = 0.0;
        double consumption = 0.0;
        if (electricityReaddings.getConsumption() != null) {
            consumption = electricityReaddings.getConsumption();
        }
        double previousReading = electricityReaddings.getCurrentReading() - consumption ;
        totalPrice = Utils.roundOfDouble(consumption * electricityReaddings.getUnitPrice());
        return new ElectricityUsage(electricityReaddings.getHostelId(),
                electricityReaddings.getId(),
                consumption,
                electricityReaddings.getRoomId(),
                electricityReaddings.getFloorId(),
                electricityReaddings.getRoomName(),
                electricityReaddings.getFloorName(),
                Utils.dateToString(electricityReaddings.getEntryDate()),
                electricityReaddings.getUnitPrice(),
                previousReading,
                electricityReaddings.getCurrentReading(),
                totalPrice,
                electricityReaddings.getNoOfTenants(),
                Utils.dateToString(electricityReaddings.getStartDate()),
                Utils.dateToString(electricityReaddings.getEndDate()));
    }
}
