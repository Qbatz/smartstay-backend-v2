package com.smartstay.smartstay.responses.InvoiceRedemption;

public record SelectedInvoiceInfo(String invoiceId,
                                  String invoiceNumber,
                                  Double paidAmount,
                                  Double pendingAmount,
                                  Double totalAmount,
                                  String paymentStatus,
                                  String invoiceType,
                                  String invoiceDate) {
}
