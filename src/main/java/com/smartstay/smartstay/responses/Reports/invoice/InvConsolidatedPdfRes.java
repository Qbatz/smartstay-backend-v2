package com.smartstay.smartstay.responses.Reports.invoice;

import com.smartstay.smartstay.responses.Reports.FooterInfo;
import com.smartstay.smartstay.responses.filterOptions.ColumnFiltersRes;
import com.smartstay.smartstay.responses.hostel.HostelInfoRes;
import com.smartstay.smartstay.responses.pagination.PaginationDataRes;

import java.util.List;

public record InvConsolidatedPdfRes(boolean includePaidInvoice,
                                    boolean includeEmptyBeds,
                                    boolean includeSubTotals,
                                    boolean includeGrandTotal,
                                    boolean includeHostelName,
                                    HostelInfoRes hostelInformation,
                                    InvoiceHeader invoiceHeader,
                                    FooterInfo footerInfo,
                                    List<InvFloorsRes> floors,
                                    List<InvRoomRes> rooms,
                                    InvConsolidatedFiltersRes filters,
                                    PaginationDataRes paginationData,
                                    List<ColumnFiltersRes> tableColumns,
                                    List<String> tableHeaders,
                                    List<Object> columnData) {
}
