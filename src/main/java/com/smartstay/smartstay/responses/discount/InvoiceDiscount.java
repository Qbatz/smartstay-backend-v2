package com.smartstay.smartstay.responses.discount;

public record InvoiceDiscount(String hostelId,
                              InvoiceInfo invoiceInfo,
                              CustomerInfo customerInfo,
                              StayInfo stayInfo) {
}
