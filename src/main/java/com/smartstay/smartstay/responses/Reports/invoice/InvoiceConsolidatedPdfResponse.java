package com.smartstay.smartstay.responses.Reports.invoice;

import com.smartstay.smartstay.responses.Reports.FooterInfo;
import com.smartstay.smartstay.responses.filterOptions.ColumnFiltersResponse;
import com.smartstay.smartstay.responses.hostel.HostelInfoResponse;
import com.smartstay.smartstay.responses.pagination.PaginationDataResponse;

import java.util.List;

public record InvoiceConsolidatedPdfResponse(boolean includePaidInvoice,
                                             boolean includeEmptyBeds,
                                             boolean includeSubTotals,
                                             boolean includeGrandTotal,
                                             boolean includeHostelName,
                                             HostelInfoResponse hostelInformation,
                                             InvoiceHeader invoiceHeader,
                                             FooterInfo footerInfo,
                                             List<InvoiceFloorsResponse> floors,
                                             List<InvoiceRoomResponse> rooms,
                                             InvoiceConsolidatedFiltersResponse filters,
                                             PaginationDataResponse paginationData,
                                             List<ColumnFiltersResponse> tableColumns,
                                             List<String> tableHeaders,
                                             List<Object> columnData) {
}
