package com.smartstay.smartstay.responses.bookings;

import java.util.List;

public record AdvanceList(int totalNoOfInvoices,
                          int currentPage,
                          int totalPage,
                          int noOfItemsPerPage,
                          List<AdvanceListItems> advanceInvoiceList) {
}
