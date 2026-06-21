package com.smartstay.smartstay.responses.bookings;

public record InitializeInvoiceItems(String invoiceType,
                                     String invoiceNumber,
                                     String dueDate,
                                     String invoiceDate,
                                     double invoiceAmount,
                                     double pendingAmount,
                                     String invoiceId,
                                     String paymentMode,
                                     Double latestPaidAmount,
                                     String latestPaidDate) {
}
