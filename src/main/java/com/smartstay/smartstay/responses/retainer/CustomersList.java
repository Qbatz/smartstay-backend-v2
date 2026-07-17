package com.smartstay.smartstay.responses.retainer;

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
                            List<Guardians> guardiansList) {
}
