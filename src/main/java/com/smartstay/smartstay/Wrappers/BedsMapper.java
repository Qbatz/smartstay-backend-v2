package com.smartstay.smartstay.Wrappers;

import com.smartstay.smartstay.dao.Beds;
import com.smartstay.smartstay.dto.beds.FloorNameRoomName;
import com.smartstay.smartstay.dto.booking.BedBookingStatus;
import com.smartstay.smartstay.ennum.BedStatus;
import com.smartstay.smartstay.ennum.BookingStatus;
import com.smartstay.smartstay.responses.beds.BedsResponse;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class BedsMapper implements Function<Beds, BedsResponse> {

    boolean isOccupied = false;
    boolean onNotice = false;

    List<FloorNameRoomName> floorNameRoomNames = null;
    List<BedBookingStatus> listBedBooking = null;

    public BedsMapper(List<FloorNameRoomName> floorNameRoomNames, List<BedBookingStatus> listBookings) {
        this.floorNameRoomNames = floorNameRoomNames;
        this.listBedBooking = listBookings;
    }

    @Override
    public BedsResponse apply(Beds beds) {

        boolean isBooked = false;
        String roomName = null;
        String floorName = null;
        Integer floorId = 0;

        if (listBedBooking != null && !listBedBooking.isEmpty()) {
            onNotice = listBedBooking
                    .stream()
                    .filter(i -> Objects.equals(i.bedId(), beds.getBedId()))
                    .anyMatch(i -> i.currentStatus().equalsIgnoreCase(BookingStatus.NOTICE.name()));
            if (onNotice) {
                isOccupied = true;
            }
        }

        if (!onNotice) {
            if (listBedBooking != null && !listBedBooking.isEmpty()) {
                isOccupied = listBedBooking
                        .stream()
                        .filter(i -> Objects.equals(i.bedId(), beds.getBedId()))
                        .anyMatch(i -> i.currentStatus().equalsIgnoreCase(BookingStatus.CHECKIN.name()));
            }
        }


//        if (beds.getCurrentStatus().equalsIgnoreCase(BedStatus.NOTICE.name())) {
//           isOccupied = true;
//           onNotice = true;
//        }
        if (beds.getCurrentStatus().equalsIgnoreCase(BedStatus.OCCUPIED.name())) {
            isOccupied = true;
        }
        if (beds.getStatus().equalsIgnoreCase(BedStatus.BOOKED.name())) {
            isBooked = true;
        }

        FloorNameRoomName floorNameRoomName = floorNameRoomNames
                .stream()
                .filter(item -> item.bedId() == beds.getBedId())
                .findFirst()
                .orElse(null);
        if (floorNameRoomName != null) {
            roomName = floorNameRoomName.roomName();
            floorName = floorNameRoomName.floorName();
            floorId = floorNameRoomName.floorId();
        }


        return new BedsResponse(beds.getBedId(),
                beds.getBedName(),
                beds.getRoomId(),
                roomName,
                floorName,
                floorId,
                isOccupied,
                onNotice,
                isBooked,
                beds.getRentAmount());
    }
}
