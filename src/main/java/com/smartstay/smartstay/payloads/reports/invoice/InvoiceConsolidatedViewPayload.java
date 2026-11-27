package com.smartstay.smartstay.payloads.reports.invoice;

import com.smartstay.smartstay.payloads.tables.CustomersTablesColumn;

import java.util.List;

public record InvoiceConsolidatedViewPayload(String search,
                                             List<String> paymentStatus,
                                             List<String> invoiceModes,
                                             List<String> invoiceTypes,
                                             List<String> createdBy,
                                             String period,
                                             Double minPaidAmount,
                                             Double maxPaidAmount,
                                             Double minOutstandingAmount,
                                             Double maxOutstandingAmount,
                                             String startDate,
                                             String endDate,
                                             List<Integer> floorIds,
                                             Boolean includePaidInvoice,
                                             Boolean includeEmptyBeds,
                                             String groupBy,
                                             Boolean includeSubTotals,
                                             Boolean includeGrandTotal,
                                             Boolean includeHostelName,
                                             Boolean isPaginated,
                                             Integer page,
                                             Integer size,
                                             List<CustomersTablesColumn> tableColumns) {

    public InvoiceConsolidatedViewPayload {
        page = (page == null || page < 0) ? 1 : page;
        size = (size == null || size <= 0) ? 10 : size;
    }
}
