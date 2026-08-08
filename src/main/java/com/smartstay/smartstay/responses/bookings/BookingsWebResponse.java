package com.smartstay.smartstay.responses.bookings;

import com.smartstay.smartstay.dao.ColumnFilters;
import com.smartstay.smartstay.dto.retainer.RetainerSummary;
import com.smartstay.smartstay.filterOptions.bookings.BookingsFilterOptions;

import java.util.List;

public record BookingsWebResponse(int totalBookings,
                                  int currentPage,
                                  int totalPage,
                                  int itemPerPage,
                                  RetainerSummary retainerSummary,
                                  BookingsFilterOptions filterOptions,
                                  List<String> tableHeaders,
                                  List<ColumnFilters> columnList,
                                  List<List<Object>> bookingsList) {
}
