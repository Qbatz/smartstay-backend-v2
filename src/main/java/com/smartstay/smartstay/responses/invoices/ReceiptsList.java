package com.smartstay.smartstay.responses.invoices;

public record ReceiptsList(String firstName,
                           String lastName,
                           String fullName,
                           String transactionId,
                           String referenceNumber,
                           String transactionNumber,
                           String invoiceNumber,
                           String paymentStatus,
                           String paidAt,
                           Double paidAmount,
                           String invoiceType,
                           String invoiceMode,
                           String invoiceId,
                           String customerId,
                           String bankName,
                           String bankId,
                           String profilePic,
                           String initials) {
}
