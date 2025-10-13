package com.smartstay.smartstay.responses.electricity;


import java.util.List;

public record ElectricitySingleCustomer(String firstName,
                                        String lastName,
                                        String initials,
                                        String profilePic,
                                        String customerId,
                                        String bedName,
                                        String floorName,
                                        String roomName,
                                        String kycStatus,
                                        boolean isKYCCompleted,
                                        List<CustomersElectricityHistory> electricityHistory) {
}
