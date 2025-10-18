package com.smartstay.smartstay.responses.invoices;

public record InvoiceItems(String invoiceNo,
                           String description,
                           Double amount) {
}
