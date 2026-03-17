package com.smartstay.smartstay.responses.invoices;

import com.smartstay.smartstay.filterOptions.invoice.InvoiceFilterOptions;

import java.util.List;

public record NewInvoicesList( String hostelId, InvoiceFilterOptions filterOptions,
                              List<InvoicesList> listInvoices) {
}
