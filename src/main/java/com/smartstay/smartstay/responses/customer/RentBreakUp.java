package com.smartstay.smartstay.responses.customer;

public record RentBreakUp(String startDate,
                          String endDate,
                          long noOfDays,
                          Double rent,
                          String bedName,
                          String roomName,
                          String floorName) {
}
