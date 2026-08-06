package com.smartstay.smartstay.responses.retainer;

import com.smartstay.smartstay.responses.InvoiceRedemption.CustomerInfo;
import com.smartstay.smartstay.responses.InvoiceRedemption.InvoiceInfo;
import com.smartstay.smartstay.responses.InvoiceRedemption.SelectedInvoiceInfo;

import java.util.List;

public record AvailableRetainerInvoices(CustomerInfo customerInfo,
                                        List<InvoiceInfo> advanceInfo,
                                        SelectedInvoiceInfo currentInvoiceInfo) {
}
