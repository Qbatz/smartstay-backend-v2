package com.smartstay.smartstay.responses.settlement;

import java.util.List;

public record UnpaidInvoices(int unpaidInvoiceCount,
                             double unpaidAmount,
                             double invoiceTotalAmount,
                             double paidAmount,
                             List<com.smartstay.smartstay.responses.customer.UnpaidInvoices> listUnpaidInvoices) {
}
