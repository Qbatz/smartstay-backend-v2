package com.smartstay.smartstay.responses.electricity;

public record CustomersElectricityHistory(Long id,
                                          String startDate,
                                          String endDate,
                                          String roomName,
                                          String floorName,
                                          String bedName,
                                          Double amount,
                                          Double consumption) {
}
