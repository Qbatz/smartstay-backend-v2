package com.smartstay.smartstay.Wrappers;


import com.smartstay.smartstay.dto.beds.FreeBeds;
import com.smartstay.smartstay.util.Utils;

import java.util.function.Function;

public class FreeBedsMapper implements Function<FreeBeds, com.smartstay.smartstay.responses.beds.FreeBeds> {

    @Override
    public com.smartstay.smartstay.responses.beds.FreeBeds apply(FreeBeds freeBeds) {
        long noOfDaysAvailable = 0;
        boolean shouldShowError = false;
        String errorMessage = null;

        if (freeBeds.getLeavingDate() != null) {
            shouldShowError = true;
            errorMessage = "This bed is availabe from " + Utils.dateToString(freeBeds.getLeavingDate());
            if (freeBeds.getExpectedJoiningDate() != null) {
                noOfDaysAvailable = Utils.findNumberOfDays(freeBeds.getLeavingDate(), freeBeds.getExpectedJoiningDate());
                errorMessage = "This bed is available between " + Utils.dateToString(freeBeds.getLeavingDate()) + " and " + Utils.dateToString(freeBeds.getExpectedJoiningDate());
            }

            return new com.smartstay.smartstay.responses.beds.FreeBeds(
                    freeBeds.getBedId(),
                    freeBeds.getRoomId(),
                    freeBeds.getFloorId(),
                    freeBeds.getBedName(),
                    freeBeds.getRentAmount(),
                    freeBeds.getBedStatus(),
                    Utils.dateToString(freeBeds.getExpectedJoiningDate()),
                    Utils.dateToString(freeBeds.getLeavingDate()),
                    freeBeds.getRoomName(),
                    freeBeds.getFloorName(),
                    shouldShowError,
                    errorMessage,
                    noOfDaysAvailable
            );
        }

        if (freeBeds.getExpectedJoiningDate() != null) {
            shouldShowError = true;
            errorMessage = "This bed is available till " +  Utils.dateToString(freeBeds.getExpectedJoiningDate());

            return new com.smartstay.smartstay.responses.beds.FreeBeds(
                    freeBeds.getBedId(),
                    freeBeds.getRoomId(),
                    freeBeds.getFloorId(),
                    freeBeds.getBedName(),
                    freeBeds.getRentAmount(),
                    freeBeds.getBedStatus(),
                    Utils.dateToString(freeBeds.getExpectedJoiningDate()),
                    Utils.dateToString(freeBeds.getLeavingDate()),
                    freeBeds.getRoomName(),
                    freeBeds.getFloorName(),
                    shouldShowError,
                    errorMessage,
                    noOfDaysAvailable
            );
        }

        return new com.smartstay.smartstay.responses.beds.FreeBeds(freeBeds.getBedId(),
                freeBeds.getRoomId(),
                freeBeds.getFloorId(),
                freeBeds.getBedName(),
                freeBeds.getRentAmount(),
                freeBeds.getBedStatus(),
                null,
                null,
                freeBeds.getRoomName(),
                freeBeds.getFloorName(),
                false,
                null,
                0);
    }
}
