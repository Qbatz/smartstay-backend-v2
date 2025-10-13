package com.smartstay.smartstay.responses.customer;

import com.smartstay.smartstay.dto.customer.Deductions;

import java.util.List;

public record CustomerInformations(String customerId,
                                   String firstName,
                                   String lastName,
                                   String fullName,
                                   String profilePic,
                                   String initials,
                                   String joiningDate,
                                   Double advanceAmount,
                                   Double rentAmount,
                                   List<Deductions> listDeductions) {
}
