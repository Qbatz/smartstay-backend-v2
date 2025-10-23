package com.smartstay.smartstay.payloads.invoice;

import java.util.List;

public record InvoiceResponse(
        String invoiceId,
        String invoiceNumber,
        String invoiceType,
        String paymentStatus,
        Double totalAmount,
        Double dueAmount,
        Double paidAmount,
        String dueDate,
        String invoiceGeneratedDate,
        List<ItemResponse> items
) {
}



