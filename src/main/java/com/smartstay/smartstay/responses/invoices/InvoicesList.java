package com.smartstay.smartstay.responses.invoices;

import com.smartstay.smartstay.dto.customer.Deductions;

import java.util.List;

public record InvoicesList(String customerFirstName,
                           String customerLastName,
                           String fullName,
                           String customerId,
                           Double invoiceAmount,
                           String invoiceId,
                           Double cgst,
                           Double sgst,
                           Double gst,
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
