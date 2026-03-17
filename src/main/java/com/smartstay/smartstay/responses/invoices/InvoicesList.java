package com.smartstay.smartstay.responses.invoices;

import com.smartstay.smartstay.dto.customer.Deductions;

import java.util.List;

public record InvoicesList(String firstName,
                           String lastName,
                           String fullName,
                           String customerId,
                           String initials,
                           String profilePic,
                           boolean isRefundable,
                           double invoiceAmount,
                           double baseAmount,
                           String invoiceId,
                           double paidAmount,
                           double dueAmount,
                           Double cgst,
                           Double sgst,
                           Long gst,
                           String createdAt,
                           String createdBy,
                           String hostelId,
                           String invoiceDate,
                           String dueDate,
                           String invoiceType,
                           String invoiceMode,
                           String paymentStatus,
                           String updatedAt,
                           String invoiceNumber,
                           boolean isCancelled,
                           List<Deductions> listDeductions) {
}
