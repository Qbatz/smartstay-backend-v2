package com.smartstay.smartstay.dto.customer;

import java.util.List;

public record OtherInvoicesInfo(Double totalInvoiceAmount,
                                Double totalPaidAmount,
                                Double totalPendingAmount,
                                int totalInvoice,
                                List<OtherInvoices> listOtherInvoices) {
}
