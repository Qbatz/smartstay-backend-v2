package com.smartstay.smartstay.responses.dashboard;

import java.util.Date;

public record OverdueInvoice(String invoiceId, String invoiceNumber, String customerName, Double totalAmount,
        Double paidAmount, String dueDate, String status) {
}
