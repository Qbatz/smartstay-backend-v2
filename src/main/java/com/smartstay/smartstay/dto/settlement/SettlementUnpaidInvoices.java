package com.smartstay.smartstay.dto.settlement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SettlementUnpaidInvoices {
    private String invoiceNo;
    private Double invoiceAmount;
    private String invoiceType;
    private String invoiceId;
    private Double pendingAmount;
}
