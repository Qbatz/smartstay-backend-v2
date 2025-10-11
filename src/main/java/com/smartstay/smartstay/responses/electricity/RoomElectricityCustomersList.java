package com.smartstay.smartstay.responses.electricity;

public record RoomElectricityCustomersList(String customerId,
                                           String firstName,
                                           String lastName,
                                           String fullName,
                                           String profilePic,
                                           String initials,
                                           String billingDate,
                                           String startDate,
                                           String endDate,
                                           String bedName,
                                           Double totalUnits,
                                           Double totalAmount) {
}
