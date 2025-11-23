package com.smartstay.smartstay.responses.customer;

import java.util.List;

public record RentInfo(Double currentPayableRent,
                       Double currentRentPaid,
                       Integer stayDays,
                       Double currentMonthRent,
                       Double rentPerDay,
                       String currentInvoiceStartDate,
                       String currentInvoiceEndDate,
                       List<RentBreakUp> rentLists) {
}
