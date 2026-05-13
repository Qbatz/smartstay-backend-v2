package com.smartstay.smartstay.responses.InvoiceRedemption;

public record InvoiceInfo(String invoiceId,
                          String invoiceNumber,
                          String invoiceType,
                          Double invoiceAmount,
                          Double paidAmount,
                          Double availableBalance,
                          String invoiceDate) {
}
