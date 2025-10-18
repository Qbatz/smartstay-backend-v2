package com.smartstay.smartstay.responses.invoices;

import java.util.List;

public record InvoiceInfo(Double subTotal,
                          Double taxAmount,
                          Double taxPercentage,
                          Double totalAmount,
                          Double paidAmount,
                          Double balanceAmount,
                          List<InvoiceItems> invoiceItems) {
}
