package com.smartstay.smartstay.Wrappers.Electricity;

import com.smartstay.smartstay.dao.CustomersBedHistory;
import com.smartstay.smartstay.dto.beds.BedDetails;
import com.smartstay.smartstay.dto.electricity.MissedEbRooms;
import com.smartstay.smartstay.util.Utils;

import java.util.Date;
import java.util.List;
import java.util.function.Function;

/**
 *
 * this is when no reading is added
 */
public class ElectricityRoomEbMapper implements Function<CustomersBedHistory, MissedEbRooms> {
    List<BedDetails> bedInformations = null;
    Date leavingDate = null;

    public ElectricityRoomEbMapper(List<BedDetails> bedInformations, Date leavingDate) {
        this.bedInformations = bedInformations;
        this.leavingDate = leavingDate;
    }

    @Override
    public MissedEbRooms apply(CustomersBedHistory customersBedHistory) {
        String roomName = null;
        String bedName = null;
        String floorName = null;

        String toDate = null;

        if (bedInformations != null) {
            BedDetails bedDetails = bedInformations
                    .stream()
                    .filter(i -> i.getBedId().equals(customersBedHistory.getBedId()))
                    .findFirst()
                    .orElse(null);
            if (bedDetails != null) {
                bedName = bedDetails.getBedName();
                floorName = bedDetails.getFloorName();
                roomName = bedDetails.getRoomName();
            }
        }

        if (customersBedHistory.getEndDate() != null) {
            toDate = Utils.dateToString(customersBedHistory.getEndDate());
        }
        else {
            toDate = Utils.dateToString(leavingDate);
        }

        return new MissedEbRooms(customersBedHistory.getRoomId(),
                roomName,
                bedName,
                floorName,
                Utils.dateToString(customersBedHistory.getStartDate()),
                toDate,
                0.0,
                null);
    }
}
