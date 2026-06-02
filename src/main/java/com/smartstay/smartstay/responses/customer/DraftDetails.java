package com.smartstay.smartstay.responses.customer;

import com.smartstay.smartstay.dto.beds.BedDetails;
import com.smartstay.smartstay.dto.customer.BookingInfo;
import com.smartstay.smartstay.dto.customer.Deductions;
import com.smartstay.smartstay.payloads.customer.Address;
import com.smartstay.smartstay.payloads.customer.Booking;
import com.smartstay.smartstay.payloads.customer.Guardian;
import com.smartstay.smartstay.payloads.customer.IdProof;
import com.smartstay.smartstay.payloads.customer.JobDetails;

import java.util.List;

public record DraftDetails(
    String customerId,
    String hostelId,
    String firstName,
    String lastName,
    String fullName,
    String emailId,
    String mobileNo,
    String countryCode,
    String initials,
    String profilePic,
    String customerCurrentStatus,
    HostelInformation hostelInfo,
    BookingInfo bookingInfo,
    BedDetails bedDetails,
    IdProof idProof,
    Address address,
    Booking booking,
    JobDetails jobDetails,
    List<Guardian> guardians,
    String panPic,
    String aadharPic,
    List<Deductions> deductions
) {
}
