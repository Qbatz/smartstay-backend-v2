package com.smartstay.smartstay.responses.customer;

import com.smartstay.smartstay.dto.amenity.AmenityRequestDTO;
import com.smartstay.smartstay.dto.customer.BookingInfo;
import com.smartstay.smartstay.dto.customer.CheckoutInfo;
import com.smartstay.smartstay.payloads.invoice.InvoiceResponse;

import java.util.List;

public record CustomerDetails(String customerId,
                              String hostelId,
                              String firstName,
                              String lastName,
                              String fullName,
                              String emailId,
                              String mobileNo,
                              String countryCode,
                              String initials,
                              String profilePic,
                              String bookingId,
                              String customerCurrentStatus,
                              CustomerAddress address,
                              HostelInformation hostelInfo,
                              KycInformations kycInfo,
                              AdvanceInfo advanceInfo,
                              CheckoutInfo checkoutInfo,
                              BookingInfo bookingInfo,
                              List<InvoiceResponse> invoiceResponseList,
                              List<BedHistory> bedHistory,
                              List<TransactionDto> transactionList,
                              List<Amenities> assignedAmenities,
                              List<AmenityRequestDTO> requestedAmenities) {
}
