package com.smartstay.smartstay.responses.invoices;

import com.smartstay.smartstay.dto.customer.Deductions;

import java.util.List;

public record InvoicesList(String firstName,
                           String lastName,
                           String fullName,
                           String customerId,
                           Long invoiceAmount,
                           Long baseAmount,
                           String invoiceId,
                           Long paidAmount,
                           Long dueAmount,
                           Double cgst,
                           Double sgst,
                           Long gst,
                           String createdAt,
                           String createdBy,
                           String hostelId,
                           String invoiceDate,
                           String dueDate,
                           String invoiceType,
                           String paymentStatus,
                           String updatedAt,
                           String invoiceNumber,
                           List<Deductions> listDeductions) {
}
