package com.smartstay.smartstay.responses.customer;

import com.smartstay.smartstay.dto.settlement.CurrentMonthOtherItems;

import java.util.List;

public record RentInfo(Double currentPayableRent,
                       Double currentRentPaid,
                       Integer stayDays,
                       Double currentMonthRent,
                       Double rentPerDay,
                       String currentInvoiceStartDate,
                       String currentInvoiceEndDate,
                       Double otherItemAmount,
                       List<CurrentMonthOtherItems> currentMonthOtherItems,
                       List<RentBreakUp> rentLists) {
}
