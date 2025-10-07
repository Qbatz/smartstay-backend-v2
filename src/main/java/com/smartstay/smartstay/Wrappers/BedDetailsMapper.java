package com.smartstay.smartstay.Wrappers;

import com.smartstay.smartstay.dto.beds.Beds;
import com.smartstay.smartstay.ennum.BookingStatus;
import com.smartstay.smartstay.ennum.CustomerStatus;
import com.smartstay.smartstay.responses.beds.BedDetails;
import com.smartstay.smartstay.util.Utils;

import java.util.Date;
import java.util.function.Function;

public class BedDetailsMapper implements Function<Beds, BedDetails> {

    boolean isOnNotice = false;
    boolean isOccupied = false;

    Date oldTenantLeavingOn = null;
    Date currentTenentJoiningDate = null;

    Beds previousTenantBed = null;
    String tag = null;


    public BedDetailsMapper(Date oldTenantLeavingOn, Date currentTenentJoiningDate, Beds previousTenant, String tag) {
        this.oldTenantLeavingOn = oldTenantLeavingOn;
        this.currentTenentJoiningDate = currentTenentJoiningDate;
        this.previousTenantBed = previousTenant;
        this.tag = tag;
    }

    @Override
    public BedDetails apply(Beds beds) {
        String oldTenantLeaving = "";
        String expectedJoiningDate = null;
        String freeFrom = null;
        String jd = Utils.dateToString(beds.joiningDate());
        if (oldTenantLeavingOn != null) {
            oldTenantLeaving = Utils.dateToString(oldTenantLeavingOn);
            isOnNotice = true;
            isOccupied = true;
            jd = Utils.dateToString(currentTenentJoiningDate);

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

        String currentTenantFirstName = null;
        String currentTenantLastName = null;
        StringBuilder currentTenantFullName = new StringBuilder();
        String currentTenantProfilePic = null;
        StringBuilder currentTenantInitials = new StringBuilder();
        String currentTenantMobile = null;
        String currentTenantCustomerId = null;

        if (previousTenantBed != null) {
            currentTenantFirstName = previousTenantBed.firstName();
            currentTenantLastName = previousTenantBed.lastName();
            currentTenantInitials.append(previousTenantBed.firstName().toUpperCase().charAt(0));
            currentTenantFullName.append(previousTenantBed.firstName());
            currentTenantProfilePic = previousTenantBed.profilePic();
            currentTenantMobile = previousTenantBed.mobile();
            currentTenantCustomerId = previousTenantBed.customerId();

            if (previousTenantBed.lastName() != null && !previousTenantBed.lastName().equalsIgnoreCase("")) {
                currentTenantFullName.append(" ");
                currentTenantFullName.append(previousTenantBed.lastName().toUpperCase().charAt(0));

                currentTenantInitials.append(previousTenantBed.lastName().toUpperCase().charAt(0));
            }
            else {
                if (previousTenantBed.firstName().length() > 1) {
                    currentTenantInitials.append(previousTenantBed.firstName().toUpperCase().charAt(1));
                }

            }
        }

        if (tag != null && tag.equalsIgnoreCase("Current")) {
            if (beds.bookingStatus() != null && beds.bookingStatus().equalsIgnoreCase(BookingStatus.BOOKED.name())) {
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
                        jd,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        beds.floorId(),
                        beds.floorName(),
                        beds.roomName(),
                        beds.countryCode(),
                        beds.firstName(),
                        beds.lastName(),
                        fullName.toString(),
                        beds.profilePic(),
                        initials.toString(),
                        beds.mobile(),
                        beds.customerId());
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
                    jd,
                    beds.firstName(),
                    beds.lastName(),
                    beds.profilePic(),
                    fullName.toString(),
                    initials.toString(),
                    beds.mobile(),
                    beds.customerId(),
                    beds.floorId(),
                    beds.floorName(),
                    beds.roomName(),
                    beds.countryCode(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null);
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
                jd,
                currentTenantFirstName,
                currentTenantLastName,
                currentTenantProfilePic,
                currentTenantFullName.toString(),
                currentTenantInitials.toString(),
                currentTenantMobile,
                currentTenantCustomerId,
                beds.floorId(),
                beds.floorName(),
                beds.roomName(),
                beds.countryCode(),
                beds.firstName(),
                beds.lastName(),
                fullName.toString(),
                beds.profilePic(),
                initials.toString(),
                beds.mobile(),
                beds.customerId());
    }
}
