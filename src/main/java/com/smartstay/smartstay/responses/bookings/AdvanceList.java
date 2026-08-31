package com.smartstay.smartstay.responses.bookings;

import com.smartstay.smartstay.dto.retainer.RetainerSummary;
import com.smartstay.smartstay.filterOptions.bookings.BookingsFilterOptions;

import java.util.List;

public record AdvanceList(int totalNoOfInvoices,
                          int currentPage,
                          int totalPage,
                          int noOfItemsPerPage,
                          BookingsFilterOptions filterOptions,
                          RetainerSummary retainerSummary,
                          List<AdvanceListItems> advanceInvoiceList) {
}
