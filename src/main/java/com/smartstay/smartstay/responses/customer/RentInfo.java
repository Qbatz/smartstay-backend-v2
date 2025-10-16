package com.smartstay.smartstay.responses.customer;

public record RentInfo(Double currentPayableRent,
                       Double currentRentPaid,
                       Integer stayDays,
                       Double currentMonthRent) {
}
