package com.smartstay.smartstay.responses.customer;

import com.smartstay.smartstay.dao.Beds;
import com.smartstay.smartstay.dto.bank.BookingBankInfo;

import java.util.List;

public record InitializeBooking(List<com.smartstay.smartstay.dto.beds.InitializeBooking> listBeds, List<BookingBankInfo> bankDetails) {
}
