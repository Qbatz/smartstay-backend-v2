package com.smartstay.smartstay.responses.Reports.invoice;

public record InvConsolidatedRes(String invoiceNumber,
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
