package com.smartstay.smartstay.responses.Reports.invoice;

public record InvoiceHeader(String startDate,
                            String endDate,
                            String totalInvoices,
                            String totalTenants,
                            String overdueInvoicesCount,
                            String totalAmount,
                            String paidAmount,
                            String bookingAmount,
                            String outstandingAmount,
                            String refundedBookingAmount,
                            String cancelledAmount) {
}
