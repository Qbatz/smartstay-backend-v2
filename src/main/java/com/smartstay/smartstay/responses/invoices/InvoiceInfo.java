package com.smartstay.smartstay.responses.invoices;

import java.util.List;

public record InvoiceInfo(Double subTotal,
                          Double taxAmount,
                          Double taxPercentage,
                          Double totalAmount,
                          Double paidAmount,
                          Double balanceAmount,
                          String invoicePeriod,
                          String invoiceMonth,
                          String paymentStatus,
                          List<InvoiceItems> invoiceItems) {
}
