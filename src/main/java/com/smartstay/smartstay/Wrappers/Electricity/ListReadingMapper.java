package com.smartstay.smartstay.Wrappers.Electricity;

import com.smartstay.smartstay.dto.electricity.ElectricityReaddings;
import com.smartstay.smartstay.responses.electricity.ElectricityUsage;
import com.smartstay.smartstay.util.Utils;

import java.util.function.Function;

public class ListReadingMapper implements Function<ElectricityReaddings, ElectricityUsage> {
    @Override
    public ElectricityUsage apply(ElectricityReaddings electricityReaddings) {
        StringBuilder fullName = new StringBuilder();
        if (electricityReaddings.getFirstName() != null) {
            fullName.append(electricityReaddings.getFirstName());
        }
        if (electricityReaddings.getLastName() != null && !electricityReaddings.getLastName().equalsIgnoreCase(" ")) {
            fullName.append(" ");
            fullName.append(electricityReaddings.getLastName());
        }

        double totalPrice = 0.0;
        if (!electricityReaddings.getConsumption().isNaN()) {
            totalPrice = electricityReaddings.getConsumption() * electricityReaddings.getUnitPrice();
        }
        return new ElectricityUsage(electricityReaddings.getHostelId(),
                electricityReaddings.getId(),
                electricityReaddings.getConsumption(),
                electricityReaddings.getRoomId(),
                fullName.toString(),
                electricityReaddings.getFloorId(),
                electricityReaddings.getRoomName(),
                electricityReaddings.getFloorName(),
                Utils.dateToString(electricityReaddings.getEntryDate()),
                electricityReaddings.getUnitPrice(),
                electricityReaddings.getPreviousReadings(),
                electricityReaddings.getCurrentReading(),
                totalPrice);
    }
}
