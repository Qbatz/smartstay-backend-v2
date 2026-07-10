package com.smartstay.smartstay.responses.invoices;

public record InvoiceBasicList(String customerName,
                               String invoiceId,
                               String profilePic,
                               String initials,
                               String invoiceNumber,
                               String status,
                               Double invoiceAmount,
                               String invoiceDate) {
}
