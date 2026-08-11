package com.smartstay.smartstay.dto.settlement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RetainerItems {
    String invoiceId;
    String invoiceNo;
    String invoiceDate;
    Double totalAmount;
    //available amount
    //applied amount = totalAmount - amount
    Double amount;
}
