package com.smartstay.smartstay.responses.dashboard;

import java.util.Date;

public record OverdueInvoice(String invoiceId,
                             String invoiceNumber,
                             String customerName,
                             String customerId,
                             String profilePic,
                             String initials,
                             Double totalAmount,
                             Double paidAmount,
                             String dueDate,
                             String invoiceDate,
                             Double dueAmount,
                             String status) {
}
