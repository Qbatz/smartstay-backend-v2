package com.smartstay.smartstay.responses.invoiceDraft;

import java.util.List;

public record InvoiceInfo(Long invoiceId,
                          Double invoiceAmount,
                          String invoiceStartDate,
                          String invoiceEndDate,
                          boolean isEdited,
                          List<InvoiceItems> invoiceItems,
                          CustomerInfo customerInfo,

                          StayInfo stayInfo) {
}
