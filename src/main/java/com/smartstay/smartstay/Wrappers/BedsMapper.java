package com.smartstay.smartstay.Wrappers;

import com.smartstay.smartstay.dao.Beds;
import com.smartstay.smartstay.ennum.BedStatus;
import com.smartstay.smartstay.responses.beds.BedsResponse;
import com.smartstay.smartstay.util.Utils;

import java.util.Date;
import java.util.function.Function;

public class BedsMapper implements Function<Beds, BedsResponse> {
    @Override
    public BedsResponse apply(Beds beds) {

        boolean isOccupied = false;
        boolean onNotice = false;
        boolean isBooked = false;
        String nextAvailableFrom = null;
        if (beds.getStatus().equalsIgnoreCase(BedStatus.BOOKED.name())) {
            isBooked = true;
        }
        if (beds.getStatus().equalsIgnoreCase(BedStatus.OCCUPIED.name())) {
            isOccupied = true;
        }
        if (beds.getStatus().equalsIgnoreCase(BedStatus.NOTICE.name())) {
            onNotice = true;
            if (beds.getFreeFrom() != null) {
                if (Utils.compareWithTwoDates(beds.getFreeFrom(), new Date()) < 0) {
                    nextAvailableFrom = Utils.dateToString(new Date());
                }
            }
        }


        return new BedsResponse(beds.getBedId(),
                beds.getBedName(),
                beds.getRoomId(),
                isOccupied,
                onNotice,
                isBooked,
                nextAvailableFrom,
                beds.getRentAmount());
    }
}
