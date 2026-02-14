package com.smartstay.smartstay.dto.customer;

public record InvoiceRefundHistory(String referenceNumber,
                                   String date,
                                   String time,
                                   String paymentMode,
                                   double amount,
                                   String paidBy,
                                   String returnedFrom) {
}
