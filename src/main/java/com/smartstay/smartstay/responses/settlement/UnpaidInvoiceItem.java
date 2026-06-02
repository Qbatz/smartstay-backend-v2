package com.smartstay.smartstay.responses.settlement;

public record UnpaidInvoiceItem(String invoiceNumber,
                                Double invoiceAmount,
                                Double paidAmount,
                                Double pendingAmount) {
}
