package com.smartstay.smartstay.Wrappers.beds;

import com.smartstay.smartstay.dao.Beds;
import com.smartstay.smartstay.dao.BookingsV1;
import com.smartstay.smartstay.dto.beds.BedDetails;
import com.smartstay.smartstay.ennum.BedStatus;
import com.smartstay.smartstay.ennum.BookingStatus;
import com.smartstay.smartstay.responses.beds.FreeBeds;
import com.smartstay.smartstay.util.Utils;

import java.util.Date;
import java.util.List;
import java.util.function.Function;

public class InitializeBedsMapper implements Function<Beds, FreeBeds>  {

    List<BedDetails> listBedDetails = null;
    List<BookingsV1> listOfBooking = null;

    Date joiningDate = null;

    public InitializeBedsMapper(List<BedDetails> listBedDetails, List<BookingsV1> listOfBooking, Date joiningDate) {
        this.listBedDetails = listBedDetails;
        this.listOfBooking = listOfBooking;
        this.joiningDate = joiningDate;
    }

    @Override
    public FreeBeds apply(Beds beds) {
        String bedName = null;
        String floorName = null;
        String roomName = null;
        Integer bedId = 0;
        Integer floorId = 0;
        Integer roomId = 0;
        String currentStatus = null;
        String newTenantJoiningDate = null;
        String currentTenantLeavingDate = null;
        boolean showWarning = false;
        String warningMessage = null;
        long noOfDaysAvailable = 0;

        if (beds.getCurrentStatus().equalsIgnoreCase(BedStatus.VACANT.name())) {
            currentStatus = "Vacant";
        }
        else if (beds.getCurrentStatus().equalsIgnoreCase(BedStatus.OCCUPIED.name())) {
            currentStatus = "Occupied";
        }
        else if (beds.getCurrentStatus().equalsIgnoreCase(BedStatus.NOTICE.name())) {
            currentStatus = "Notice";
        }

        BedDetails bedDetails = listBedDetails.stream()
                .filter(i -> i.getBedId().equals(beds.getBedId()))
                .findFirst()
                .orElse(null);
        if (bedDetails != null) {
            roomName = bedDetails.getRoomName();
            floorName = bedDetails.getFloorName();
            floorId = bedDetails.getFloorId();
        }


        List<BookingsV1> listBookingsExceptCheckedIn = listOfBooking
                .stream()
                .filter(i -> !i.getCurrentStatus().equalsIgnoreCase(BookingStatus.CHECKIN.name()))
                .toList();

        BookingsV1 currentBooking = listBookingsExceptCheckedIn
                .stream()
                .filter(i ->i.getBedId() ==beds.getBedId())
                .findFirst()
                .orElse(null);

        if (currentBooking != null) {
            if (beds.getCurrentStatus().equalsIgnoreCase(BedStatus.VACANT.name()) && beds.isBooked()) {
                newTenantJoiningDate = Utils.dateToString(currentBooking.getExpectedJoiningDate());
                showWarning = true;
                noOfDaysAvailable = Utils.findNumberOfDays(joiningDate, currentBooking.getExpectedJoiningDate());
                if (noOfDaysAvailable < 0) {
                    noOfDaysAvailable = noOfDaysAvailable * -1;
                }
                warningMessage = "This bed will be available for " + noOfDaysAvailable;

            }
            else if (beds.getCurrentStatus().equalsIgnoreCase(BedStatus.OCCUPIED.name()) && !beds.isBooked()) {
                currentTenantLeavingDate = Utils.dateToString(currentBooking.getLeavingDate());
                showWarning = true;
                noOfDaysAvailable = Utils.findNumberOfDays(joiningDate, currentBooking.getLeavingDate());
//                if (noOfDaysAvailable < 0) {
//                    noOfDaysAvailable = noOfDaysAvailable * -1;
//                }
                warningMessage = "This bed will be available from " + currentTenantLeavingDate;
            }
            else if (beds.getCurrentStatus().equalsIgnoreCase(BedStatus.NOTICE.name()) && !beds.isBooked()) {
                currentTenantLeavingDate = Utils.dateToString(currentBooking.getLeavingDate());
                showWarning = true;
//                noOfDaysAvailable = Utils.findNumberOfDays(joiningDate, currentBooking.getLeavingDate());
//                if (noOfDaysAvailable < 0) {
//                    noOfDaysAvailable = noOfDaysAvailable * -1;
//                }
                warningMessage = "This bed will be available from " + currentTenantLeavingDate;
            }
        }

        return new FreeBeds(beds.getBedId(),
                beds.getRoomId(),
                floorId,
                beds.getBedName(),
                beds.getRentAmount(),
                currentStatus,
                newTenantJoiningDate,
                currentTenantLeavingDate,
                roomName,
                floorName,
                showWarning,
                warningMessage,
                noOfDaysAvailable);
    }
}
