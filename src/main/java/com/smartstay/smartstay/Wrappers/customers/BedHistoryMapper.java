package com.smartstay.smartstay.Wrappers.customers;

import com.smartstay.smartstay.dao.CustomersBedHistory;
import com.smartstay.smartstay.ennum.CustomersBedType;
import com.smartstay.smartstay.responses.customer.BedHistory;
import com.smartstay.smartstay.util.Utils;

import java.util.function.Function;

public class BedHistoryMapper implements Function<com.smartstay.smartstay.dto.customer.BedHistory, BedHistory> {

    @Override
    public BedHistory apply(com.smartstay.smartstay.dto.customer.BedHistory bedHistory) {
        String type = null;
        String endDate = null;
        String startDate = null;

        if (bedHistory.getEndDate() == null) {
            endDate = "Till date";
        }
        else {
            endDate = Utils.dateToString(bedHistory.getEndDate());
        }

        if (bedHistory.getStartDate() != null) {
            startDate = Utils.dateToString(bedHistory.getStartDate());
        }


        if (bedHistory.getType().equalsIgnoreCase(CustomersBedType.BOOKED.name())) {
            type = "Booked";
            endDate = "NA";
        }
        else if(bedHistory.getType().equalsIgnoreCase(CustomersBedType.CHECK_IN.name())) {
            type = "Checked in";
        }
        else if(bedHistory.getType().equalsIgnoreCase(CustomersBedType.REASSIGNED.name())) {
            type = "Reassigned";
        }
        else if (bedHistory.getType().equalsIgnoreCase(CustomersBedType.MAINTENANCE.name())) {
            type = "Maintenance";
        }

        return new BedHistory(bedHistory.getHistoryId(),
                bedHistory.getBedId(),
                bedHistory.getBedName(),
                bedHistory.getRoomName(),
                startDate,
                endDate,
                bedHistory.getReason(),
                bedHistory.getRent(),
                type);
    }
}
