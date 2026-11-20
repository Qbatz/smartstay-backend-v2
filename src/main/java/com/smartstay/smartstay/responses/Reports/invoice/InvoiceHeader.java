package com.smartstay.smartstay.responses.Reports.invoice;

public record InvoiceHeader(String startDate,
                            String endDate,
                            String totalInvoices,
                            String totalAmount,
                            String paidAmount,
                            String bookingAmount,
                            String outstandingAmount,
                            String refundedBookingAmount,
                            String cancelledAmount) {
}
