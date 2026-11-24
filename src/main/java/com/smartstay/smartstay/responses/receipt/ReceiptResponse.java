package com.smartstay.smartstay.responses.receipt;

import com.smartstay.smartstay.dao.ColumnFilters;
import com.smartstay.smartstay.responses.invoices.ReceiptsList;

import java.util.List;

public record ReceiptResponse(String hostelId,
                              int totalReceipt,
                              Double totalAmount,
                              int totalPages,
                              int currentPage,
                              int size,
                              Double paidAmount,
                              Double refundAmount,
                              ReceiptFilterOptions filterOptions,
                              List<String> tableHeaders,
                              List<ColumnFilters> columnList,
                              List<ReceiptsList> listReceipts) {
}
