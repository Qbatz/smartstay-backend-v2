package com.smartstay.smartstay.responses.discount;

public record InvoiceInfo(String invoiceId,
                          String invoiceNumber,
                          String invoiceDate,
                          Double invoiceAmount,
                          Double paidAmount,
                          Double discountAmount,
                          String overDueOn,
                          String overdueDays) {
}
