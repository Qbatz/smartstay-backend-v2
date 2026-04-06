package com.smartstay.smartstay.dto.invoices;

public record InvoiceDiscountDto(double invoicePercentage,
                                 double invoiceDiscountAmount,
                                 double oldInvoiceDiscount,
                                 double invoiceDifference) {
}
