package com.smartstay.smartstay.Wrappers;

import com.smartstay.smartstay.dao.Beds;
import com.smartstay.smartstay.ennum.BedStatus;
import com.smartstay.smartstay.responses.beds.BedsResponse;
import com.smartstay.smartstay.util.Utils;

import java.util.Date;
import java.util.function.Function;

public class BedsMapper implements Function<Beds, BedsResponse> {

    boolean isOccupied = false;
    boolean onNotice = false;

    public BedsMapper(boolean onNotice) {
        this.onNotice = onNotice;
    }

    @Override
    public BedsResponse apply(Beds beds) {

        boolean isBooked = false;

        if (beds.getCurrentStatus().equalsIgnoreCase(BedStatus.NOTICE.name())) {
            if (beds.getStatus().equalsIgnoreCase(BedStatus.BOOKED.name())) {
                isBooked = true;
            }
        }
        if (beds.getCurrentStatus().equalsIgnoreCase(BedStatus.OCCUPIED.name())) {
            isOccupied = true;
        }
        if (beds.getStatus().equalsIgnoreCase(BedStatus.BOOKED.name())) {
            isBooked = true;
        }


        return new BedsResponse(beds.getBedId(),
                beds.getBedName(),
                beds.getRoomId(),
                isOccupied,
                onNotice,
                isBooked,
                beds.getRentAmount());
    }
}
