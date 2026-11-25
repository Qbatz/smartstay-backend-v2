package com.smartstay.smartstay.responses.Reports.invoice;

public record InvoiceConsolidatedResponse(String invoiceNumber,
                                          String name,
                                          String invoiceType,
                                          String invoiceDate,
                                          String dueDate,
                                          String invoiceAmount,
                                          String paidAmount,
                                          String dueAmount,
                                          String status,
                                          String invoiceUrl) {
}
