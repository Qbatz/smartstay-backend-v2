package com.smartstay.smartstay.responses.customer;

public record CustomerDetails(String customerId,
                              String firstName,
                              String lastName,
                              String fullName,
                              String emailId,
                              String mobileNo,
                              String countryCode,
                              String initials,
                              String profilePic,
                              CustomerAddress address,
                              HostelInformation hostelInfo,
                              KycInformations kycInfo) {
}
