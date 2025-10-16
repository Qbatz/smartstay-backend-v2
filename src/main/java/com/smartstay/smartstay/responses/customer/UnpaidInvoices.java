package com.smartstay.smartstay.responses.customer;

public record UnpaidInvoices(String invoiceNumber,
                             String invoiceId,
                             String type,
                             Double invoiceTotalAmount,
                             Double payableAmount,
                             Double ebAmount,
                             Double amenityAmount) {
}
