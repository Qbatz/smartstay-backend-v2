package com.smartstay.smartstay.responses.invoices;

import com.smartstay.smartstay.dto.pagination.PaginationSummary;
import com.smartstay.smartstay.filterOptions.invoice.InvoiceFilterOptions;

import java.util.List;

public record InvoiceWebResponses(String hostelId,
                                  InvoiceFilterOptions filterOptions,
                                  PaginationSummary paginationSummary,
                                  InvoiceSummaryInfo invoiceSummary,
                                  List<InvoicesList> listInvoices) {
}
