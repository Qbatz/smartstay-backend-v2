package com.smartstay.smartstay.responses.invoices;

import com.smartstay.smartstay.dto.customer.Deductions;

import java.util.List;

public record InvoiceInfo(Double subTotal,
                          Double taxAmount,
                          Double taxPercentage,
                          Double totalAmount,
                          Double paidAmount,
                          Double balanceAmount,
                          Double discountAmount,
                          Double discountPercentage,
                          String invoicePeriod,
                          String invoiceMonth,
                          String paymentStatus,
                          boolean isCancelled,
                          boolean isDiscounted,
                          double totalDeduction,
                          List<InvoiceItems> invoiceItems,
                          List<Deductions> listDeductions) {
}
