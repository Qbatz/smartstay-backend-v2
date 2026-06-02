package com.smartstay.smartstay.responses.settlement;

public record InvoiceInfo(String invoiceNo,
                          String invoiceDate,
                          String dueDate,
                          String joiningDate,
                          String rentalPeriod,
                          Double subTotal,
                          Double deductionAmount,
                          Double unpaidInvoiceAmount,
                          Double electricityAmount,
                          Double finalAmount,
                          boolean isNewPattern,
                          String status) {
}
