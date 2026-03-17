package com.smartstay.smartstay.dto.invoices;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceAggregateDto {
    private Long count;
    private Double totalAmount;
    private Double paidAmount;
    private Double refundAmount;
    private Double cancelledAmount;
    private Double refundedAmount;
}
