package com.smartstay.smartstay.payloads.invoice;

import java.util.List;

public record ManualInvoiceNew(String invoiceNumber,
                               String invoiceDate,
                               String notes,
                               Boolean isDiscounted,
                               Double discountAmount,
                               Double discountPercentage,
                               List<ItemResponse> invoiceItems) {
}
