package com.smartstay.smartstay.responses.bookings;

public record AdvanceInfo(Double advanceAmount,
                          Double advanceBalanceAmount,
                          String invoiceDate,
                          String invoiceType,
                          String advanceInvoiceId,
                          String advanceInvoiceNumber,
                          boolean status,
                          String message,
                          String paymentStatus) {
}
