package com.smartstay.smartstay.responses.invoices;

import java.util.List;

public record Details(String invoiceNumber,
                      String startDate,
                      String endDate,
                      String dueDate,
                      String invoiceMode,
                      String paymentStatus,
                      Double totalInvoiceAmount,
                      boolean canEdit,
                      List<InvoiceItems> invoiceItems) {
}
