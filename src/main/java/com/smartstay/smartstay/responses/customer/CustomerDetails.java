package com.smartstay.smartstay.responses.customer;

import com.smartstay.smartstay.dto.customer.CheckoutInfo;
import com.smartstay.smartstay.payloads.invoice.InvoiceResponse;

import java.util.List;

public record CustomerDetails(String customerId,
                              String firstName,
                              String lastName,
                              String fullName,
                              String emailId,
                              String mobileNo,
                              String countryCode,
                              String initials,
                              String profilePic,
                              String customerCurrentStatus,
                              CustomerAddress address,
                              HostelInformation hostelInfo,
                              KycInformations kycInfo,
                              AdvanceInfo advanceInfo,
                              CheckoutInfo checkoutInfo,
                              List<InvoiceResponse> invoiceResponseList,
                              List<BedHistory> bedHistory,
                              List<Amenities> amenities) {
}
