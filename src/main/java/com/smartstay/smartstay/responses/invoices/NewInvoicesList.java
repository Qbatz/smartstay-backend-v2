package com.smartstay.smartstay.responses.invoices;

import com.smartstay.smartstay.dto.pagination.PaginationSummary;
import com.smartstay.smartstay.filterOptions.invoice.InvoiceFilterOptions;

import java.util.List;

public record NewInvoicesList( String hostelId,
                               PaginationSummary paginationSummary,
                               boolean hasInvoiceInReview,
                               int reviewInvoiceCount,
                               InvoiceFilterOptions filterOptions,
                              List<InvoicesList> listInvoices) {
}
