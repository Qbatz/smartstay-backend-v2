package com.smartstay.smartstay.responses.customer;

import com.smartstay.smartstay.dto.customer.Deductions;

import java.util.List;

public record CustomerInformations(String customerId,
                                   String firstName,
                                   String lastName,
                                   String fullName,
                                   String profilePic,
                                   String initials,
                                   String countryCode,
                                   String mobile,
                                   String joiningDate,
                                   Double advanceAmount,
                                   Double rentAmount,
                                   boolean isAdvancePaid,
                                   Double advancePaidAmount,
                                   Double bookingAmount,
                                   List<Deductions> listDeductions) {
}
