package com.smartstay.smartstay.responses.InvoiceRedemption;

import java.util.List;

public record AvailableInvoices(CustomerInfo customerInfo,
                                List<InvoiceInfo> listInvoices) {
}
