package com.smartstay.smartstay.Wrappers.Electricity;

import com.smartstay.smartstay.dao.CustomersBedHistory;
import com.smartstay.smartstay.dao.ElectricityReadings;
import com.smartstay.smartstay.dto.beds.BedDetails;
import com.smartstay.smartstay.dto.electricity.MissedEbRooms;
import com.smartstay.smartstay.util.Utils;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

/**
 *
 * this is when reading is missing
 *
 */

public class ElectricityRoomIdEbMapper implements Function<ElectricityReadings, MissedEbRooms> {

    List<BedDetails> bedInformations = null;
    List<CustomersBedHistory> listBedHistory = null;

    Date leavingDate = null;

    public ElectricityRoomIdEbMapper(List<BedDetails> bedInformations, List<CustomersBedHistory> listCustomerBedHistory, Date leavingDate) {
        this.bedInformations = bedInformations;
        this.listBedHistory = listCustomerBedHistory;
        this.leavingDate = leavingDate;
    }

    @Override
    public MissedEbRooms apply(ElectricityReadings electricityReadings) {
        String roomName = null;
        String bedName = null;
        String floorName = null;
        String fromDate = null;
        String toDate = null;

        if (bedInformations != null) {
            BedDetails bedDetails = bedInformations
                    .stream()
                    .filter(i -> i.getRoomId().equals(electricityReadings.getRoomId()))
                    .findFirst()
                    .orElse(null);
            if (bedDetails != null) {
                roomName = bedDetails.getRoomName();
                bedName = bedDetails.getBedName();
                floorName = bedDetails.getFloorName();
            }
        }
        if (listBedHistory != null) {
            CustomersBedHistory bedHistory = listBedHistory
                    .stream()
                    .filter(i -> electricityReadings.getRoomId().equals(i.getRoomId()))
                    .max(Comparator.comparing(CustomersBedHistory::getEndDate))
                    .orElse(null);
            if (bedHistory != null) {
                if (Utils.compareWithTwoDates(electricityReadings.getEntryDate(), bedHistory.getStartDate()) > 0) {
                    fromDate = Utils.dateToString(electricityReadings.getEntryDate());
                    if (bedHistory.getEndDate() != null) {
                        toDate = Utils.dateToString(bedHistory.getEndDate());
                    }
                    else {
                        toDate = Utils.dateToString(leavingDate);
                    }
                }
                else {
                    fromDate = Utils.dateToString(bedHistory.getStartDate());
                    if (bedHistory.getEndDate() != null) {
                        toDate = Utils.dateToString(bedHistory.getEndDate());
                    }
                    else {
                        toDate = Utils.dateToString(leavingDate);
                    }

                }
            }
        }
        return new MissedEbRooms(electricityReadings.getRoomId(),
                roomName,
                bedName,
                floorName,
                fromDate,
                toDate);
    }
}
