package com.smartstay.smartstay.responses.customer;

public record RentBreakUp(String startDate,
                          String endDate,
                          long noOfDays,
                          Double rentPerDay,
                          Double rent,
                          Double totalRent,
                          String bedName,
                          String roomName,
                          String floorName) {
}
