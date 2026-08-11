package com.smartstay.smartstay.responses.retainer;

import com.smartstay.smartstay.dto.customer.AddressInfo;
import com.smartstay.smartstay.dto.customer.StayInfo;

import java.util.List;

public record CustomersList(String customerId,
                            String fullName,
                            String firstName,
                            String lastName,
                            String profilePic,
                            String initials,
                            String mobile,
                            String country,
                            Double availableBalance,
                            Double availableAdvanceBalance,
                            Double availableBookingBalance,
                            String joiningDate,
                            StayInfo stayInfo,
                            AddressInfo addressInfo,
                            List<Guardians> guardiansList) {
}
