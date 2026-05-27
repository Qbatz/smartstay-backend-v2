package com.smartstay.smartstay.responses.customer;

import java.util.Date;

public record RentBreakUp(String startDate,
                          String endDate,
                          Date dStartDate,
                          Date dEndDate,
                          long noOfDays,
                          Double rentPerDay,
                          Double rent,
                          Double totalRent,
                          String bedName,
                          String roomName,
                          String floorName) {
}
