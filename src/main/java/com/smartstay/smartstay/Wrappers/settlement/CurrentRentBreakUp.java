package com.smartstay.smartstay.Wrappers.settlement;

import com.smartstay.smartstay.responses.customer.RentBreakUp;
import com.smartstay.smartstay.util.Utils;

import java.util.Date;
import java.util.function.Function;

public class CurrentRentBreakUp implements Function<com.smartstay.smartstay.dto.settlement.CurrentRentBreakUp, RentBreakUp> {
    @Override
    public RentBreakUp apply(com.smartstay.smartstay.dto.settlement.CurrentRentBreakUp currentRentBreakUp) {
        Date startDate = null;
        Date endDate = null;
        long noOfDays = 0;
        String bedName = null;
        String roomName = null;
        String floorName = null;
        double rentPerDay = 0.0;
        double rent = 0.0;
        if (currentRentBreakUp.getFromDate() != null) {
            startDate = currentRentBreakUp.getFromDate();
        }
        if (currentRentBreakUp.getToDate() != null) {
            endDate = currentRentBreakUp.getToDate();
        }
        if (startDate != null && endDate != null) {
            noOfDays = Utils.findNumberOfDays(startDate, endDate);
        }
        if (currentRentBreakUp.getRentPerDay() != null) {
            rentPerDay = currentRentBreakUp.getRentPerDay();
        }
        if (currentRentBreakUp.getRent() != null) {
            rent = currentRentBreakUp.getRent();
        }
        if (currentRentBreakUp.getBedName() != null) {
            bedName = currentRentBreakUp.getBedName();
        }
        if (currentRentBreakUp.getFloorName() != null) {
            floorName = currentRentBreakUp.getFloorName();
        }
        if (currentRentBreakUp.getRoomName() != null) {
            roomName = currentRentBreakUp.getRoomName();
        }


        return new RentBreakUp(Utils.dateToString(startDate),
                Utils.dateToString(endDate),
                startDate,
                endDate,
                noOfDays,
                rentPerDay,
                rent,
                rent,
                bedName,
                roomName,
                floorName);
    }
}
