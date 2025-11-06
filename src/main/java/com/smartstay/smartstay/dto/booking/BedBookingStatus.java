package com.smartstay.smartstay.dto.booking;

import java.util.Date;

public record BedBookingStatus(Integer bedId,
                               String currentStatus,
                               Date joiningDate,
                               Date leavingDate,
                               String customerId) {
}
