package com.smartstay.smartstay.Wrappers.beds;

import com.smartstay.smartstay.dao.BookingsV1;
import com.smartstay.smartstay.dto.beds.InitializeBooking;
import com.smartstay.smartstay.ennum.BedStatus;
import com.smartstay.smartstay.responses.beds.BedInitializations;
import com.smartstay.smartstay.util.Utils;

import java.util.List;
import java.util.function.Function;

public class BedInitializationMapper implements Function<InitializeBooking, BedInitializations>  {

    List<BookingsV1> listBookings = null;

    public BedInitializationMapper(List<BookingsV1> listBookings) {
        this.listBookings = listBookings;
    }

    @Override
    public BedInitializations apply(InitializeBooking initializeBooking) {

        boolean shouldShowError = false;
        String errorMessage = null;

        if (initializeBooking.getIsBooked()) {
            BookingsV1 booking = listBookings
                    .stream()
                    .filter(i -> initializeBooking.getBedId().equals(i.getBedId()))
                    .findFirst()
                    .orElse(null);

            if (booking != null) {
                String joiningDate = Utils.dateToString(booking.getExpectedJoiningDate());
                shouldShowError = true;
                errorMessage = "This bed is available till " + joiningDate;
            }
        }
        else if (initializeBooking.getCurrentStatus().equalsIgnoreCase(BedStatus.NOTICE.name())) {
            BookingsV1 booking = listBookings
                    .stream()
                    .filter(i -> initializeBooking.getBedId().equals(i.getBedId()))
                    .findFirst()
                    .orElse(null);

            if (booking != null) {
                String leavingDate = Utils.dateToString(booking.getLeavingDate());
                shouldShowError = true;
                errorMessage = "This bed is available from " + leavingDate;
            }
        }

        return new BedInitializations(initializeBooking.getBedId(), initializeBooking.getRoomId(),
                initializeBooking.getFloorId(),
                initializeBooking.getBedName(),
                initializeBooking.getFloorName(),
                initializeBooking.getRoomName(),
                initializeBooking.getRentAmount(),
                initializeBooking.getCurrentStatus(),
                shouldShowError,
                errorMessage) ;
    }
}
