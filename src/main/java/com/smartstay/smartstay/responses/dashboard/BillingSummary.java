package com.smartstay.smartstay.responses.dashboard;

public record BillingSummary(Integer totalInvoiceGenerated,
                             Double totalAmount,
                             Double totalPaid,
                             Double paidThisMonth,
                             Double totalPending,
                             Double refundedAmount,
                             String collectionRate,
                             String fromLastMonth) {
}
