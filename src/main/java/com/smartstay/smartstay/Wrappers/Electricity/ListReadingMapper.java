package com.smartstay.smartstay.Wrappers.Electricity;

import com.smartstay.smartstay.dto.electricity.ElectricityReaddings;
import com.smartstay.smartstay.responses.electricity.ElectricityUsage;
import com.smartstay.smartstay.util.Utils;

import java.util.function.Function;

public class ListReadingMapper implements Function<ElectricityReaddings, ElectricityUsage> {
    @Override
    public ElectricityUsage apply(ElectricityReaddings electricityReaddings) {

        double totalPrice = 0.0;
        double previousReading = electricityReaddings.getConsumption() - electricityReaddings.getCurrentReading() ;
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
