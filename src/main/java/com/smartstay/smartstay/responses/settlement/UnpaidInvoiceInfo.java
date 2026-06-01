package com.smartstay.smartstay.responses.settlement;

import java.util.List;

public record UnpaidInvoiceInfo(int noOfUnpaidInvoices,
                                Double unpaidInvoiceTotalAmount,
                                List<UnpaidInvoiceItem> unpaidInvoiceItems) {
}
