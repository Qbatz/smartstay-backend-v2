package com.smartstay.smartstay.Wrappers;

import com.smartstay.smartstay.dao.Beds;
import com.smartstay.smartstay.ennum.BedStatus;
import com.smartstay.smartstay.responses.beds.BedsResponse;
import com.smartstay.smartstay.util.Utils;

import java.util.Date;
import java.util.function.Function;

public class BedsMapper implements Function<Beds, BedsResponse> {

    boolean isOccupied = false;

    public BedsMapper(boolean isOccupied) {
        this.isOccupied = isOccupied;
    }

    @Override
    public BedsResponse apply(Beds beds) {

        boolean onNotice = false;
        boolean isBooked = false;
        if (beds.getStatus().equalsIgnoreCase(BedStatus.BOOKED.name())) {
            isBooked = true;
        }
        if (beds.getStatus().equalsIgnoreCase(BedStatus.OCCUPIED.name())) {
            isOccupied = true;
        }
        if (beds.getStatus().equalsIgnoreCase(BedStatus.NOTICE.name())) {
            onNotice = true;
            isBooked = beds.isBooked();
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
