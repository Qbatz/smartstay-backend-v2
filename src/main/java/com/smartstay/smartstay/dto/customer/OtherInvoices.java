package com.smartstay.smartstay.dto.customer;

import java.util.List;

public record OtherInvoices(String invoiceNumber,
                            String invoiceDate,
                            String invoiceId,
                            Double invoiceAmount,
                            Double paidAmount,
                            Double pendingAmount,
                            List<InvoiceDescription> invoiceDescriptions) {
}
