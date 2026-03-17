package com.smartstay.smartstay.dto.customer;

import java.util.Date;

public record ReassignRent(String oldInvoiceId, String newInvoiceId, double balanceAmount, Date invoiceDate) {
}
