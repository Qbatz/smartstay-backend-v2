package com.smartstay.smartstay.dto.beds;

import java.util.Date;

public record Beds(int bedId,
                   String hostelId,
                   boolean isActive,
                   boolean isBooked,
                   Double roomRent,
                   int roomId,
                   Date freeFrom,
                   String bedName,
                   String status,
                   Double currentRent,
                   Date joiningDate,
                   Date leavingDate,
                   String bookingId,
                   String createdBy,
                   Date expectedJoinig,
                   Date cusJoiningDate,
                   String firstName,
                   String lastName,
                   String profilePic,
                   String bookingStatus) {
}
