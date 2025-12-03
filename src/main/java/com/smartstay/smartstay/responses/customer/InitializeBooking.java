package com.smartstay.smartstay.responses.customer;

import com.smartstay.smartstay.dao.Beds;
import com.smartstay.smartstay.dto.bank.BookingBankInfo;
import com.smartstay.smartstay.responses.beds.BedInitializations;
import com.smartstay.smartstay.responses.beds.FreeBeds;

import java.util.List;

public record InitializeBooking(List<BedInitializations> listBeds, List<BookingBankInfo> bankDetails) {
}
