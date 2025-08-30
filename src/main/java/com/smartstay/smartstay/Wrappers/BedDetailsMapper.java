package com.smartstay.smartstay.Wrappers;

import com.smartstay.smartstay.dto.beds.Beds;
import com.smartstay.smartstay.ennum.CustomerStatus;
import com.smartstay.smartstay.responses.beds.BedDetails;
import com.smartstay.smartstay.util.Utils;

import java.util.Date;
import java.util.function.Function;

public class BedDetailsMapper implements Function<Beds, BedDetails> {

    boolean isOnNotice = false;
    boolean isOccupied = false;

    Date oldTenantLeavingOn = null;


    public BedDetailsMapper(Date oldTenantLeavingOn) {
        this.oldTenantLeavingOn = oldTenantLeavingOn;
    }

    @Override
    public BedDetails apply(Beds beds) {
        String oldTenantLeaving = "";
        String expectedJoiningDate = null;
        String freeFrom = null;
        if (oldTenantLeavingOn != null) {
            oldTenantLeaving = Utils.dateToString(oldTenantLeavingOn);
        }
        if (beds.bookingStatus() != null && beds.bookingStatus().equalsIgnoreCase(CustomerStatus.BOOKED.name())) {
            expectedJoiningDate = Utils.dateToString(beds.expectedJoinig());
        }

        if (beds.bookingStatus() != null && beds.bookingStatus().equalsIgnoreCase(CustomerStatus.ON_NOTICE.name())) {
            freeFrom = Utils.dateToString(beds.freeFrom());
        }

        StringBuilder initials = new StringBuilder();
        StringBuilder fullName = new StringBuilder();
        if (beds.firstName() != null) {
            initials.append(beds.firstName().toUpperCase().charAt(0));
            fullName.append(beds.firstName());
        }
        if (beds.lastName() != null && !beds.lastName().equalsIgnoreCase("")) {
            fullName.append(" ");
            fullName.append(beds.lastName());
            initials.append(beds.lastName().toUpperCase().charAt(0));
        }
        else {
            if (beds.firstName() != null)
                initials.append(beds.firstName().toUpperCase().charAt(1));
        }
        return new BedDetails(beds.bedName(),
                beds.bedId(),
                beds.hostelId(),
                beds.isBooked(),
                isOnNotice,
                isOccupied,
                beds.roomRent(),
                beds.roomId(),
                freeFrom,
                beds.currentRent(),
                oldTenantLeaving,
                beds.bookingId(),
                expectedJoiningDate,
                Utils.dateToString(beds.joiningDate()),
                beds.firstName(),
                beds.lastName(),
                beds.profilePic(),
                fullName.toString(),
                initials.toString(),
                beds.mobile(),
                beds.floorId(),
                beds.floorName(),
                beds.roomName(),
                beds.countryCode(),
                beds.customerId());
    }
}
