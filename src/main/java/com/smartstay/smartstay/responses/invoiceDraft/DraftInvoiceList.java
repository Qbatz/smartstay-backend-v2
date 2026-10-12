package com.smartstay.smartstay.responses.invoiceDraft;

import java.util.List;

public record DraftInvoiceList(String hostelId,
                               String billingStartDate,
                               String billingEndDate,
                               String invoiceDate,
                               int totalInvoices,
                               List<InvoiceInfo> invoicesList) {
}
