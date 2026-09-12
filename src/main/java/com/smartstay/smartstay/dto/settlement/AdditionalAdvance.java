package com.smartstay.smartstay.dto.settlement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdditionalAdvance {
    private String invoiceNumber;
    private String invoiceId;
    private Double paidAmount;
    private Double invoiceAmount;
    private Double pendingAmount;
    private Double invoiceBalance;
}
