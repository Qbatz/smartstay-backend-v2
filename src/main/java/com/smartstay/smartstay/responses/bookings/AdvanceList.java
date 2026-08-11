package com.smartstay.smartstay.responses.bookings;

import com.smartstay.smartstay.dto.retainer.RetainerSummary;

import java.util.List;

public record AdvanceList(int totalNoOfInvoices,
                          int currentPage,
                          int totalPage,
                          int noOfItemsPerPage,
                          RetainerSummary retainerSummary,
                          List<AdvanceListItems> advanceInvoiceList) {
}
