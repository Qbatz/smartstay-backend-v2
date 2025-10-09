package com.smartstay.smartstay.dto.electricity;

import java.util.Date;

public record CustomersBookings(String customerId,
                                Date joiningDate,
                                Date leavingDate) {
}
