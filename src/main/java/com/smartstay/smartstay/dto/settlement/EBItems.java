package com.smartstay.smartstay.dto.settlement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EBItems {
    //from EB readings
    private Integer readingId;
    //from customer eb history
    private Long customerEBId;
    private Date fromDate;
    private Date toDate;
    private Double totalAmount;
    //same as units in customer eb history
    private Double consumption;
}
