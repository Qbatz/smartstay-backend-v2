package com.smartstay.smartstay.dto.settlement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrentRentBreakUp {
    private String bedName;
    private String roomName;
    private String floorName;
    private Date fromDate;
    private Date toDate;
    private Double rentPerDay;
    //collected rent
    private Double rent;
    private boolean isFullRentCollected;
}
